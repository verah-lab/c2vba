/*
 * Copyright (c) 2018, HeuBoe
 */

package de.heuboe.vmis2.jprotoc.protopojo;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.text.StringEscapeUtils;

import com.google.common.base.Strings;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;

import de.heuboe.vmis2.jprotoc.PluginVersion;
import de.heuboe.vmis2.jprotoc.api.JProtocPlugin;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Generates the pojos for the protos.
 */
public class ProtoPojoGenerator implements JProtocPlugin {

    private static final String POJO_SUBPACKAGE = "pojo";
    private static final String PROTO_PREFIX = ".google.protobuf.";
    private static final String WRAPPER_POSTFIX = "Value";
    private static final String TIMESTAMP_NAME = PROTO_PREFIX + "Timestamp";
    private static final String ANY_NAME = PROTO_PREFIX + "Any";

    private static final String BOOLEAN_TYPE_NAME = "Boolean";
    private static final String STRING_TYPE_NAME = "String";
    private static final String BYTE_STRING_TYPE_NAME = "ByteString";
    private static final String DOUBLE_TYPE_NAME = "Double";
    private static final String FLOAT_TYPE_NAME = "Float";
    private static final String LONG_TYPE_NAME = "Long";
    private static final String INTEGER_TYPE_NAME = "Integer";
    
    private static final String BOOLEAN_PRIMITIVE_NAME = "boolean";
    private static final String DOUBLE_PRIMITIVE_NAME = "double";
    private static final String FLOAT_PRIMITIVE_NAME = "float";
    private static final String LONG_PRIMITIVE_NAME = "long";
    private static final String INTEGER_PRIMITIVE_NAME = "int";

    private static final boolean DEBUG_CONTEXT_PROPERTY = false;

    private Template template;

    /**
     * Creates a new JProtocPlugin that can be used to generate the ProtoPojos.
     */
    public ProtoPojoGenerator() {
        final Configuration cfg = new Configuration(Configuration.VERSION_2_3_27);
        cfg.setClassForTemplateLoading(ProtoPojoGenerator.class, "/templates/protopojo");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        try {
            this.template = cfg.getTemplate("Proto2Pojo.ftl");
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }

    }

    @Override
    public Stream<PluginProtos.CodeGeneratorResponse.File> generate(final PluginProtos.CodeGeneratorRequest request) {
        final PojoProtoTypeMap protoTypeMap = PojoProtoTypeMap.of(request.getProtoFileList(), POJO_SUBPACKAGE);

        return request.getProtoFileList().stream()
                .filter(protoFile -> request.getFileToGenerateList().contains(protoFile.getName()))
                .flatMap(f -> extractContext(protoTypeMap, f))
                .map(this::buildFile);
    }

    private Stream<ContextFile> extractContext(final PojoProtoTypeMap protoTypeMap,
            final DescriptorProtos.FileDescriptorProto proto) {

        final Map<List<Integer>, CommentInfo> commentInfoMap = mapSourceInfo(proto.getSourceCodeInfo());

        final String inputfile = proto.getName();

        final Stream<ContextFile> messageStream =
                io.vavr.collection.Stream.ofAll(proto.getMessageTypeList())
                        .zipWithIndex()
                        .map(t2 -> extractFileContext(protoTypeMap, t2._1, inputfile, commentInfoMap,
                                proto.getPackage(),
                                io.vavr.collection.List
                                        .of(DescriptorProtos.FileDescriptorProto.MESSAGE_TYPE_FIELD_NUMBER)
                                        .append(t2._2).asJava()))
                        .toJavaStream();

        final Stream<ContextFile> enumStream =
                io.vavr.collection.Stream.ofAll(proto.getEnumTypeList())
                        .zipWithIndex()
                        .map(t2 -> extractEnumFileContext(protoTypeMap, t2._1, inputfile, commentInfoMap,
                                proto.getPackage(),
                                io.vavr.collection.List.of(DescriptorProtos.FileDescriptorProto.ENUM_TYPE_FIELD_NUMBER)
                                        .append(t2._2).asJava()))
                        .toJavaStream();

        return Stream.concat(enumStream, messageStream)
                .map(ctx -> {
                    ctx.protoPackageName = extractPackageName(proto);
                    ctx.packageName = ctx.protoPackageName + "." + POJO_SUBPACKAGE;
                    return ctx;
                })
                .map(ctx -> {
                    ctx.protoName = proto.getName();
                    return ctx;
                });
    }

    private String extractPackageName(final DescriptorProtos.FileDescriptorProto proto) {
        final DescriptorProtos.FileOptions options = proto.getOptions();
        if (options != null) {
            final String javaPackage = options.getJavaPackage();
            if (!Strings.isNullOrEmpty(javaPackage)) {
                return javaPackage;
            }
        }

        return Strings.nullToEmpty(proto.getPackage());
    }

    private ContextFile extractFileContext(
            final PojoProtoTypeMap protoTypeMap,
            final DescriptorProtos.DescriptorProto messageProto, final String inputfile,
            final Map<List<Integer>, CommentInfo> commentInfoMap,
            final String aPackage, final List<Integer> path) {
        final ContextFile ctx = new ContextFile();
        ctx.message = extractMessageContext(protoTypeMap, messageProto, false, aPackage, commentInfoMap, path);
        ctx.fileName = ctx.message.className + ".java";
        ctx.inputfileName = inputfile;
        return ctx;
    }

    private ContextMessage extractMessageContext(
            final PojoProtoTypeMap protoTypeMap,
            final DescriptorProtos.DescriptorProto messageProto, final boolean inner, final String nestedPath,
            final Map<List<Integer>, CommentInfo> commentInfoMap,
            final List<Integer> path) {
        final ContextMessage ctx = new ContextMessage();
        ctx.className = messageProto.getName();
        ctx.isEnum = false;
        ctx.isNested = inner;
        // Don't throw an exception here, if the iid is of the wrong type
        // The incompatibility with the IIDContainer, will fail the compile.
        ctx.hasIid = messageProto.getFieldList().stream().anyMatch(field -> field.getName().equals("iid"));

        ctx.commentInfo = commentInfoMap.get(path);

        final List<ContextProperty> list = io.vavr.collection.Stream.ofAll(messageProto.getFieldList())
                .zipWithIndex()
                .map(t2 -> extractContextProperty(protoTypeMap, t2._1, commentInfoMap,
                        io.vavr.collection.List.ofAll(path)
                                .append(
                                        DescriptorProtos.DescriptorProto.FIELD_FIELD_NUMBER)
                                .append(t2._2).asJava()))
                .asJava();
        ctx.properties.addAll(list);

        ctx.nestedMessages.addAll(
                io.vavr.collection.Stream.ofAll(messageProto.getEnumTypeList())
                        .zipWithIndex()
                        .map(t2 -> extractEnumContext(protoTypeMap, t2._1, true,
                                addNestedPath(nestedPath, ctx.className), commentInfoMap,
                                io.vavr.collection.List.ofAll(path)
                                        .append(DescriptorProtos.DescriptorProto.ENUM_TYPE_FIELD_NUMBER)
                                        .append(t2._2).asJava()))
                        .asJava());

        ctx.nestedMessages.addAll(
                io.vavr.collection.Stream.ofAll(messageProto.getNestedTypeList())
                        .zipWithIndex()
                        .map(t2 -> extractMessageContext(protoTypeMap, t2._1, true,
                                addNestedPath(nestedPath, ctx.className), commentInfoMap,
                                io.vavr.collection.List.ofAll(path)
                                        .append(DescriptorProtos.DescriptorProto.NESTED_TYPE_FIELD_NUMBER)
                                        .append(t2._2)
                                        .asJava()))
                        .asJava());

        final PojoProtoTypeMap.JavaInfo javaInfo = protoTypeMap.toJavaType(addNestedPath(nestedPath, ctx.className));
        ctx.protoPackageName = javaInfo.protoPackage;
        ctx.javaPackage = javaInfo.javaPackage;
        ctx.protoPath = nestedPath.isEmpty() ? ctx.protoPackageName : nestedPath; //nestedPath now contains the package
        return ctx;

    }

    private String addNestedPath(final String nestedPath, final String className) {
        return nestedPath.isEmpty() ? className : nestedPath + "." + className;
    }

    private ContextFile extractEnumFileContext(
            final PojoProtoTypeMap protoTypeMap,
            final DescriptorProtos.EnumDescriptorProto enumProto, final String inputfile,
            final Map<List<Integer>, CommentInfo> commentInfoMap,
            final String aPackage, final List<Integer> path) {
        final ContextFile ctx = new ContextFile();
        ctx.inputfileName = inputfile;
        ctx.message = extractEnumContext(protoTypeMap, enumProto, false, aPackage, commentInfoMap, path);
        ctx.fileName = ctx.message.className + ".java";
        return ctx;
    }

    private ContextMessage extractEnumContext(
            final PojoProtoTypeMap protoTypeMap, final DescriptorProtos.EnumDescriptorProto enumProto,
            final boolean inner,
            final String nestedPath,
            final Map<List<Integer>, CommentInfo> commentInfoMap,
            final List<Integer> path) {
        final ContextMessage ctx = new ContextMessage();
        ctx.className = enumProto.getName();
        ctx.isEnum = true;
        ctx.commentInfo = commentInfoMap.get(path);

        final List<ContextEnumValue> list = io.vavr.collection.Stream.ofAll(enumProto.getValueList())
                .zipWithIndex()
                .map(t2 -> extractContextEnumValue(t2._1, commentInfoMap,
                        io.vavr.collection.List.ofAll(path)
                                .append(
                                        DescriptorProtos.EnumDescriptorProto.VALUE_FIELD_NUMBER)
                                .append(t2._2)
                                .asJava()))
                .asJava();
        ctx.enumValues.addAll(list);
        ctx.isNested = inner;

        final PojoProtoTypeMap.JavaInfo javaInfo = protoTypeMap.toJavaType(addNestedPath(nestedPath, ctx.className));
        ctx.protoPackageName = javaInfo.protoPackage;
        ctx.javaPackage = javaInfo.javaPackage;
        ctx.protoPath = nestedPath.isEmpty() ? ctx.protoPackageName : nestedPath; //nestedPath now contains the package

        return ctx;
    }

    private ContextProperty extractContextProperty( //NOSONAR
            final PojoProtoTypeMap protoTypeMap, final DescriptorProtos.FieldDescriptorProto fdp,
            final Map<List<Integer>, CommentInfo> commentInfoMap,
            final List<Integer> path) {
        final ContextProperty cp = new ContextProperty();
        //JsonName seems to do the right conversion for JAVA to. If there are exceptions we need a better conversion routine here
        cp.propertyName = fdp.getJsonName();
        cp.commentInfo = commentInfoMap.get(path);
        final Optional<ScalarTypeInfo> typeInfoOptional = toJavaType(fdp);
        cp.isList = isRepeated(fdp.getLabel());
        if (typeInfoOptional.isPresent()) {
            final ScalarTypeInfo scalarTypeInfo = typeInfoOptional.get();
            cp.isScalar = true;
            if (BOOLEAN_PRIMITIVE_NAME.equals(scalarTypeInfo.javaType)) {
                cp.booleanProperty = true;
            }
            if (STRING_TYPE_NAME.equals(scalarTypeInfo.javaType)
                    || BYTE_STRING_TYPE_NAME.equals(scalarTypeInfo.javaType)) { // bytes?
                cp.nullPossibleProperty = true;
            }
            if (cp.isList || scalarTypeInfo.wrapper) {
                cp.type = scalarTypeInfo.boxedType;
                cp.nullPossibleProperty = true;
                if (scalarTypeInfo.wrapper) {
                    cp.isWrapper = true;
                    cp.wrapperType = scalarTypeInfo.wrapperType;
                }
            } else {
                cp.type = scalarTypeInfo.javaType;

            }
        } else {
            cp.nullPossibleProperty = true;
            if (TIMESTAMP_NAME.equals(fdp.getTypeName())) {
                cp.isDate = true; //Timestamp is handled in a special way.
                final PojoProtoTypeMap.JavaInfo ji = protoTypeMap.toJavaType(fdp.getTypeName());
                cp.type = ji.className;
            } else if (ANY_NAME.equals((fdp.getTypeName()))) {
                cp.isAny = true;
                final PojoProtoTypeMap.JavaInfo ji = protoTypeMap.toJavaType(fdp.getTypeName());
                cp.type = ji.className;
            } else { // need to detect enum here
                cp.isDate = false;
                final PojoProtoTypeMap.JavaInfo ji = protoTypeMap.toJavaType(fdp.getTypeName());
                cp.type = ji.className;
                cp.protoPackage = ji.protoPackage;
                cp.javaPackage = ji.javaPackage;
                cp.isEnum = ji.isEnum;
            }
        }

        // pojo 2 proto conversion
        cp.returnTypeOfPojoGet = cp.type;
        cp.toProtConvIntro = "";
        cp.toProtConvOutro = "";
        cp.streamProcessing = "";
        String propertyName = cp.getPropertyName();
        if (propertyName == null || propertyName.isEmpty()) {
            throw new IllegalStateException("propertyName is null or blank.");
        }
        String capFirstPropertyName = capFirst(propertyName);
        if (cp.isList()) { // List of something
            cp.protoSetterName = "addAll" + capFirstPropertyName;
            cp.pojoGetterName = "get" + capFirstPropertyName + "List()";
            if (cp.isScalar()) {
                if (cp.isWrapper()) {
                    throw new IllegalArgumentException("Wrapper are not allowed in Lists");
                } else {
                    cp.returnTypeOfPojoGet = "List<" + cp.type + ">";
                    cp.streamProcessing = "";
                }
            } else if (cp.isDate) {
                cp.returnTypeOfPojoGet = "List<Instant>";
                cp.streamProcessing = ".stream().map(m->DateUtils.fromInstantUtc(m)).collect(Collectors.toList())";
            } else {
                cp.returnTypeOfPojoGet = "List<" + cp.javaPackage + ".P" + cp.type + ">";
                cp.streamProcessing =
                        ".stream().map(m->" + cp.javaPackage + ".P" + cp.type + ".to(m)).collect(Collectors.toList())";
            }
        } else {
            cp.protoSetterName = "set" + capFirstPropertyName;
            cp.pojoGetterName = "get" + capFirstPropertyName + "()";
            if (cp.isScalar()) {
                if (cp.isBooleanProperty()) {
                    if (propertyName.startsWith("is")) {
                        cp.pojoGetterName = propertyName + "()";
                    } else {
                        cp.pojoGetterName = "is" + capFirstPropertyName + "()";
                    }
                }
                if (cp.isWrapper()) {
                    cp.pojoGetterName = "get" + capFirstPropertyName + "()";
                    cp.toProtConvIntro = cp.wrapperType + ".newBuilder().setValue(";
                    cp.toProtConvOutro = ").build()";
                }
            } else {
                cp.toProtConvOutro = ")";
                if (cp.isDate) {
                    cp.returnTypeOfPojoGet = "Instant";
                    cp.toProtConvIntro = "DateUtils.fromInstantUtc(";
                } else {
                    cp.returnTypeOfPojoGet = cp.javaPackage + ".P" + cp.type;
                    cp.toProtConvIntro = cp.javaPackage + ".P" + cp.type + ".to(";

                }
            }
        }

        if (DEBUG_CONTEXT_PROPERTY) {
            System.out.println(cp.toString()); //NOSONAR: Using Logger in Generator is critical
        }

        return cp;
    }

    private static String capFirst(final String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private ContextEnumValue extractContextEnumValue(
            final DescriptorProtos.EnumValueDescriptorProto edp,
            final Map<List<Integer>, CommentInfo> commentInfoMap,
            final List<Integer> path) {
        final ContextEnumValue cev = new ContextEnumValue();
        cev.name = edp.getName();
        cev.number = edp.getNumber();
        cev.commentInfo = commentInfoMap.get(path);
        return cev;
    }

    private boolean isRepeated(final DescriptorProtos.FieldDescriptorProto.Label label) {
        return DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED.equals(label);
    }

    private Optional<ScalarTypeInfo> toJavaType(final DescriptorProtos.FieldDescriptorProto fdp) { //NOSONAR
        final DescriptorProtos.FieldDescriptorProto.Type type = fdp.getType();
        switch (type) {
            case TYPE_DOUBLE:
                return Optional.of(new ScalarTypeInfo(type, DOUBLE_PRIMITIVE_NAME, DOUBLE_TYPE_NAME, false, null));
            case TYPE_FLOAT:
                return Optional.of(new ScalarTypeInfo(type, FLOAT_PRIMITIVE_NAME, FLOAT_TYPE_NAME, false, null));

            case TYPE_INT64:
            case TYPE_FIXED64:
            case TYPE_UINT64:
            case TYPE_SINT64:
            case TYPE_SFIXED64:
                return Optional.of(new ScalarTypeInfo(type, LONG_PRIMITIVE_NAME, LONG_TYPE_NAME, false, null));

            case TYPE_INT32:
            case TYPE_FIXED32:
            case TYPE_UINT32:
            case TYPE_SFIXED32:
            case TYPE_SINT32:
                return Optional.of(new ScalarTypeInfo(type, INTEGER_PRIMITIVE_NAME, INTEGER_TYPE_NAME, false, null));

            case TYPE_BOOL:
                return Optional.of(new ScalarTypeInfo(type, BOOLEAN_PRIMITIVE_NAME, BOOLEAN_TYPE_NAME, false, null));

            case TYPE_STRING:
                return Optional.of(new ScalarTypeInfo(type, STRING_TYPE_NAME, STRING_TYPE_NAME, false, null));

            case TYPE_BYTES:
                return Optional.of(new ScalarTypeInfo(type, BYTE_STRING_TYPE_NAME, BYTE_STRING_TYPE_NAME, false, null));

            case TYPE_GROUP:
            case TYPE_ENUM:
                return Optional.empty();

            case TYPE_MESSAGE:
                return toWrapper(fdp);

        }

        return Optional.empty();
    }

    private Optional<ScalarTypeInfo> toWrapper(final DescriptorProtos.FieldDescriptorProto fdp) {
        if (fdp.getTypeName().startsWith(PROTO_PREFIX) && fdp.getTypeName().endsWith(WRAPPER_POSTFIX)) {
            final String wrapperType =
                    fdp.getTypeName().substring(PROTO_PREFIX.length(), fdp.getTypeName().indexOf(WRAPPER_POSTFIX));
            final String wrapper = wrapperType + WRAPPER_POSTFIX;
            if (wrapperType.equals(FLOAT_TYPE_NAME)) {
                return Optional.of(new ScalarTypeInfo(DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT, FLOAT_PRIMITIVE_NAME,
                        FLOAT_TYPE_NAME, true, wrapper));
            }
            if (wrapperType.equals(DOUBLE_TYPE_NAME)) {
                return Optional.of(new ScalarTypeInfo(DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE, DOUBLE_PRIMITIVE_NAME,
                        DOUBLE_TYPE_NAME, true, wrapper));
            }
            if (wrapperType.endsWith("32")) {
                return Optional
                        .of(new ScalarTypeInfo(getTypeOfInt32Wrapper(wrapperType), INTEGER_PRIMITIVE_NAME, INTEGER_TYPE_NAME, true, wrapper));
            }
            if (wrapperType.endsWith("64")) {
                return Optional
                        .of(new ScalarTypeInfo(getTypeOfInt64Wrapper(wrapperType), LONG_PRIMITIVE_NAME, LONG_TYPE_NAME, true, wrapper));
            }
            if ("Bool".equals(wrapperType)) {
                return Optional.of(new ScalarTypeInfo(DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL,
                        BOOLEAN_PRIMITIVE_NAME, BOOLEAN_TYPE_NAME, true, wrapper));
            }
            if (wrapperType.equals(STRING_TYPE_NAME)) {
                return Optional.of(new ScalarTypeInfo(DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING,
                        STRING_TYPE_NAME, STRING_TYPE_NAME, true, wrapper));
            }
            if ("Bytes".equals(wrapperType)) {
                return Optional.of(new ScalarTypeInfo(DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES,
                        BYTE_STRING_TYPE_NAME, BYTE_STRING_TYPE_NAME, true, wrapper));
            }
            return Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    private DescriptorProtos.FieldDescriptorProto.Type getTypeOfInt64Wrapper(final String wrapperType) {
        switch (wrapperType) {
            case "UInt64Value":
                return DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64;
            case "Int64Value":
            default:
                return DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32;
        }
    }

    private DescriptorProtos.FieldDescriptorProto.Type getTypeOfInt32Wrapper(final String wrapperType) {
        switch (wrapperType) {
            case "UInt32Value":
                return DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT32;
            case "Int32Value":
            default:
                return DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32;
        }
    }

    private String absoluteFileName(final ContextFile ctx) {
        final String dir = ctx.packageName.replace('.', '/');
        if (Strings.isNullOrEmpty(dir)) {
            return "P" + ctx.fileName;
        } else {
            return dir + "/P" + ctx.fileName;
        }
    }

    private PluginProtos.CodeGeneratorResponse.File buildFile(final ContextFile context) {
        if (null == context.inputfileName) {
            context.inputfileName = "-no file-";
        }
        final Writer out = new StringWriter();
        final Map<String, Object> root = new HashMap<>();
        root.put("ctx", context);
        try {
            this.template.process(root, out);
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
        final String content = out.toString();
        return PluginProtos.CodeGeneratorResponse.File
                .newBuilder()
                .setName(absoluteFileName(context))
                .setContent(content)
                .build();
    }

    private Map<List<Integer>, CommentInfo> mapSourceInfo(final DescriptorProtos.SourceCodeInfo sci) {
        return sci.getLocationList()
                .stream()
                .filter(this::locNotEmpty)
                .map(this::loc2CommentInfo)
                .collect(Collectors.toMap(CommentInfo::getPath, Function
                        .identity()));
    }

    private CommentInfo loc2CommentInfo(final DescriptorProtos.SourceCodeInfo.Location loc) {
        final CommentInfo ci = new CommentInfo();

        ci.path.addAll(loc.getPathList());

        if (loc.hasLeadingComments()) {
            final String[] split = loc.getLeadingComments().split("\\n");
            ci.leadingComments.addAll(escapeHtml(split));
        }
        if (loc.hasTrailingComments()) {
            final String[] split = loc.getTrailingComments().split("\\n");
            ci.trailingComments.addAll(escapeHtml(split));
        }
        if (!loc.getLeadingDetachedCommentsList().isEmpty()) {
            loc.getLeadingDetachedCommentsList().forEach(s -> {
                final String[] split = s.split("\\n");
                ci.detachedLeadingComments.addAll(escapeHtml(split));
                ci.detachedLeadingComments.add("\\n");
            });
        }

        return ci;
    }

    private List<String> escapeHtml(final String[] split) {
        return Arrays.stream(split).map(StringEscapeUtils::escapeHtml4).collect(Collectors.toList());
    }

    private boolean locNotEmpty(final DescriptorProtos.SourceCodeInfo.Location loc) {
        return loc.hasLeadingComments() || loc.hasTrailingComments() || !loc.getLeadingDetachedCommentsList().isEmpty();
    }

    @ToString
    @AllArgsConstructor
    private class ScalarTypeInfo {
        DescriptorProtos.FieldDescriptorProto.Type protoFieldType;
        String javaType;
        String boxedType;
        boolean wrapper;
        String wrapperType;
        CommentInfo commentInfo;

        ScalarTypeInfo(final DescriptorProtos.FieldDescriptorProto.Type protoFieldType, final String javaType,
                final String boxedType,
                final boolean wrapper, final String wrapperType) {
            this.protoFieldType = protoFieldType;
            this.javaType = javaType;
            this.boxedType = boxedType;
            this.wrapper = wrapper;
            this.wrapperType = wrapperType;

        }
    }

    /**
     * Class to be used in Freemarker
     */
    @Getter
    @ToString
    public static class ContextFile {
        String protoName;
        String inputfileName;
        String fileName;
        String packageName;
        String protoPackageName;
        PluginVersion version = new PluginVersion();
        String generationTime = genTime();
        ContextMessage message = new ContextMessage();

        /**
         * generate generation time
         *
         * @return generation time as string
         */
        public static String genTime() {
            final GregorianCalendar cal = new GregorianCalendar();
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z (z)");
            sdf.setTimeZone(cal.getTimeZone());
            return sdf.format(cal.getTime());
        }
    }

    /**
     * Class to be used in Freemarker
     */
    @Getter
    @ToString
    public static class ContextMessage {
        String className;
        boolean isEnum;
        boolean isNested;
        boolean hasIid = false;
        String protoPackageName;
        String javaPackage;
        String protoPath;
        String javaPath;
        final List<ContextProperty> properties = new ArrayList<>();
        final List<ContextEnumValue> enumValues = new ArrayList<>();
        final List<ContextMessage> nestedMessages = new ArrayList<>();
        CommentInfo commentInfo;
    }

    /**
     * Backing class for template.
     */
    @Getter
    @ToString
    public static class ContextProperty { //NOSONAR: used in Freemarker
        String returnTypeOfPojoGet;
        String propertyName;
        String type;
        boolean isList;
        boolean isScalar;
        boolean isDate;
        boolean isAny;
        boolean isWrapper;
        boolean booleanProperty = false;
        boolean nullPossibleProperty = false;
        boolean isEnum = false;
        String protoPackage;
        String javaPackage;
        String wrapperType;

        String protoSetterName;
        String protoGetterName;
        String pojtoSetterName;
        String pojoGetterName;
        String streamProcessing;
        String toProtConvOutro;
        String toProtConvIntro;
        CommentInfo commentInfo;

        /**
         * Checks if the {@link #type} is a primitive Java data type.
         * @return {@code true} if the type is a Java primitive data type, otherwise {@code false}
         */
        public boolean isPrimitive() {
            switch (type) {
                case BOOLEAN_PRIMITIVE_NAME:
                case DOUBLE_PRIMITIVE_NAME:
                case FLOAT_PRIMITIVE_NAME:
                case LONG_PRIMITIVE_NAME:
                case INTEGER_PRIMITIVE_NAME:
                    return true;
                default:
                    return false;
            }
        }

        /**
         * Gets the boxed type in case the {@link #type} is a primitive Java data type. The boxed
         * type is the corresponding class type of a primitive Java type.
         * 
         * @return the corresponding boxed type of a primitive Java type; if the {@link #type} is
         *         not primitive Java data type the method will return {@code null}
         */
        public String getBoxedType() {
            switch (type) {
                case BOOLEAN_PRIMITIVE_NAME:
                    return BOOLEAN_TYPE_NAME;
                case DOUBLE_PRIMITIVE_NAME:
                    return DOUBLE_TYPE_NAME;
                case FLOAT_PRIMITIVE_NAME:
                    return FLOAT_TYPE_NAME;
                case LONG_PRIMITIVE_NAME:
                    return LONG_TYPE_NAME;
                case INTEGER_PRIMITIVE_NAME:
                    return INTEGER_TYPE_NAME;
                default:
                    return null;
            }
        }
    }

    /**
     * Class to be used in Freemarker
     */
    @Getter
    @ToString
    public static class ContextEnumValue {
        // CHECKSTYLE DISABLE VisibilityModifier FOR 2 LINES
        String name;
        int number;
        CommentInfo commentInfo;
    }

    /**
     * Class to be used in Freemarker
     */
    @Getter
    @ToString
    public static class CommentInfo {
        final List<Integer> path = new ArrayList<>();
        final List<String> leadingComments = new ArrayList<>();
        final List<String> trailingComments = new ArrayList<>();
        final List<String> detachedLeadingComments = new ArrayList<>();
    }

}
