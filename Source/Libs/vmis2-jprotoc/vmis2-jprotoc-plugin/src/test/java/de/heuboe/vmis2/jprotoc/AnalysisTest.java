package de.heuboe.vmis2.jprotoc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;

import io.vavr.Tuple;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class AnalysisTest {

    @Test
    public void analyseGeneratorRequest() throws Exception {
        PluginProtos.CodeGeneratorRequest cgr = GeneratorTestUtils.loadResource("/meinTestInput.protoB");
        assertFalse(cgr.getProtoFileList().isEmpty());

        List<DescriptorProtos.FileDescriptorProto> fileDescriptorProtos = cgr.getProtoFileList()
                                                                             .stream()
                                                                             .filter(fdp -> cgr.getFileToGenerateList().contains(fdp.getName()))
                                                                             .collect(Collectors.toList());

        assertEquals(fileDescriptorProtos.size(), cgr.getFileToGenerateList().size());


        Map<String, Map<List<Integer>, CommentInfo>> commentInfos = fileDescriptorProtos
            .stream()
            .map(fdp -> Tuple.of(fdp.getName(), mapSourceInfo(fdp.getSourceCodeInfo())))
            .collect(Collectors.toMap(t -> t._1, t -> t._2));

        assertEquals(4, commentInfos.size());

//        commentInfos.forEach((s, listCommentInfoMap) -> {
//            log.info(s);
//            listCommentInfoMap.forEach((path, commentInfo) -> log.info("    {} -> {}", path, commentInfo));
//        });

        List<JavaInfo> javaInfos = fileDescriptorProtos.stream()
                                                             .flatMap(fdp -> mapJavaInfo(fdp, commentInfos.get(fdp.getName())).stream())
                                                             .collect(Collectors.toList());

        assertFalse(javaInfos.isEmpty());

        javaInfos.forEach(ji -> log.info(ji.toString()));


    }

    private List<JavaInfo> mapJavaInfo(
        DescriptorProtos.FileDescriptorProto fdp,
        Map<List<Integer>, CommentInfo> listCommentInfoMap
    ) {
        io.vavr.collection.List<JavaInfo> javaInfos1 = Stream.ofAll(fdp.getEnumTypeList())
                                          .zipWithIndex()
                                          .flatMap(t2 -> mapEnum(listCommentInfoMap, t2._1, fdp.getPackage(),
                                                                 Arrays.asList(DescriptorProtos.FileDescriptorProto.ENUM_TYPE_FIELD_NUMBER, t2._2)))
                                          .toList();

        io.vavr.collection.List<JavaInfo> javaInfos2 = Stream.ofAll(fdp.getMessageTypeList())
                                          .zipWithIndex()
                                          .flatMap(t2 -> mapMessage(listCommentInfoMap, t2._1, fdp.getPackage(),
                                                                    Arrays.asList(DescriptorProtos.FileDescriptorProto.MESSAGE_TYPE_FIELD_NUMBER, t2._2)))
                                          .toList();

        return javaInfos1.appendAll(javaInfos2).asJava();

//        List<JavaInfo> enumInfos = fdp.getEnumTypeList().stream().map(edp -> mapEnum(edp, fdp.getPackage())).collect(Collectors.toList());
    }

    private List<JavaInfo> mapMessage(
        Map<List<Integer>, CommentInfo> listCommentInfoMap, DescriptorProtos.DescriptorProto dp, String aPackage, List<Integer> path
    ) {
        String newPackage = aPackage + "." + dp.getName();

        Option<JavaInfo> messageInfo = Option.none();
        io.vavr.collection.List<JavaInfo> messageAttrs = io.vavr.collection.List.empty();
        if (listCommentInfoMap.containsKey(path)) {
            messageInfo = Option.of(JavaInfo.builder()
                                            .javaName(newPackage)
                                            .commentInfo(listCommentInfoMap.get(path))
                                            .build());

            messageAttrs = Stream.ofAll(dp.getFieldList())
                                 .zipWithIndex()
                                 .map(t2 -> mapField(listCommentInfoMap, t2._1, newPackage,
                                                     io.vavr.collection.List.ofAll(path)
                                                                            .append(DescriptorProtos.DescriptorProto.FIELD_FIELD_NUMBER)
                                                                            .append(t2._2)
                                                                            .asJava()))
                                 .filter(o -> o.isDefined())
                                 .map(o -> o.get())
                                 .toList();
        }

        io.vavr.collection.List<JavaInfo> enumInfos = Stream.ofAll(dp.getEnumTypeList())
                                                            .zipWithIndex()
                                                            .flatMap(t2 -> mapEnum(listCommentInfoMap, t2._1, newPackage,
                                                                                   io.vavr.collection.List.ofAll(path)
                                                                                                          .append(DescriptorProtos.DescriptorProto.ENUM_TYPE_FIELD_NUMBER)
                                                                                                          .append(t2._2)
                                                                                                          .asJava()))
                                                            .toList();

        io.vavr.collection.List<JavaInfo> messageInfos = Stream.ofAll(dp.getNestedTypeList())
                                                               .zipWithIndex()
                                                               .flatMap(t2 -> mapMessage(listCommentInfoMap, t2._1, newPackage,
                                                                                         io.vavr.collection.List.ofAll(path)
                                                                                         .append(DescriptorProtos.DescriptorProto.NESTED_TYPE_FIELD_NUMBER)
                                                                                         .append(t2._2)
                                                                                         .asJava()))
                                                               .toList();

        return enumInfos.appendAll(messageInfos).prependAll(messageAttrs).prependAll(messageInfo.iterator()).asJava();

    }

    private Option<JavaInfo> mapField(
        Map<List<Integer>, CommentInfo> listCommentInfoMap, DescriptorProtos.FieldDescriptorProto fdp, String newPackage, List<Integer> path
    ) {
        if (listCommentInfoMap.containsKey(path)) {
            return Option.of(
                JavaInfo.builder()
                        .javaName(newPackage + "." + fdp.getName())
                        .commentInfo(listCommentInfoMap.get(path))
                        .build()
            );
        } else {
            return Option.none();
        }

    }


    private List<JavaInfo> mapEnum(
        Map<List<Integer>, CommentInfo> listCommentInfoMap,
        DescriptorProtos.EnumDescriptorProto edp, String aPackage, List<Integer> path
    ) {
        String newPackage = aPackage + "." + edp.getName();
        Option<JavaInfo> enumInfo = Option.none();
        if (listCommentInfoMap.containsKey(path)) {
            enumInfo = Option.of(JavaInfo.builder()
                                         .javaName(newPackage)
                                         .commentInfo(listCommentInfoMap.get(path))
                                         .build());
        }

        io.vavr.collection.List<JavaInfo> enumAttrs = io.vavr.collection.Stream.ofAll(edp.getValueList())
                                                                               .zipWithIndex()
                                                                               .map(t2 -> mapEnumAttr(listCommentInfoMap, t2._1, newPackage,
                                                                                                      io.vavr.collection.List.ofAll(path)
                                                                                                                             .append(
                                                                                                                                 DescriptorProtos.EnumDescriptorProto.VALUE_FIELD_NUMBER)
                                                                                                                             .append(t2._2)
                                                                                                                             .asJava()))
                                                                               .filter(o -> o.isDefined())
                                                                               .map(o -> o.get())
                                                                               .toList();

        return enumAttrs.prependAll(enumInfo.iterator()).asJava();
    }

    private Option<JavaInfo> mapEnumAttr(
        Map<List<Integer>, CommentInfo> listCommentInfoMap, DescriptorProtos.EnumValueDescriptorProto evdp, String newPackage, List<Integer> path
    ) {

        List<Integer> newPath = io.vavr.collection.List.ofAll(path)
                                                       .append(DescriptorProtos.EnumDescriptorProto.VALUE_FIELD_NUMBER)
                                                       .append(evdp.getNumber())
                                                       .asJava();

        if (listCommentInfoMap.containsKey(newPath)) {
            return Option.of(
                JavaInfo.builder()
                        .javaName(newPackage + "." + evdp.getName())
                        .commentInfo(listCommentInfoMap.get(newPath))
                        .build()
            );
        } else {
            return Option.none();
        }
    }

    private Map<List<Integer>, CommentInfo> mapSourceInfo(DescriptorProtos.SourceCodeInfo sci) {
        return sci.getLocationList()
                  .stream()
                  .filter(loc -> locNotEmpty(loc))
                  .map(loc -> loc2CommentInfo(loc))
                  .collect(Collectors.toMap(CommentInfo::getPath, Function
                      .identity()));
    }

    private CommentInfo loc2CommentInfo(DescriptorProtos.SourceCodeInfo.Location loc) {
        CommentInfo.CommentInfoBuilder infoBuilder = CommentInfo.builder();

        infoBuilder.path(loc.getPathList());

        if (loc.hasLeadingComments()) {
            String[] split = loc.getLeadingComments().split("\\n");
            infoBuilder.leadingComments(Arrays.asList(split));
        }
        if (loc.hasTrailingComments()) {
            String[] split = loc.getTrailingComments().split("\\n");
            infoBuilder.trailingComments(Arrays.asList(split));
        }
        if (!loc.getLeadingDetachedCommentsList().isEmpty()) {
            loc.getLeadingDetachedCommentsList().forEach(s -> {
                String[] split = s.split("\\n");
                infoBuilder.detachedLeadingComments(Arrays.asList(split));
                infoBuilder.detachedLeadingComment("\n");
            });
        }

        return infoBuilder.build();
    }


    private boolean locNotEmpty(DescriptorProtos.SourceCodeInfo.Location loc) {
        return loc.hasLeadingComments() || loc.hasTrailingComments() || !loc.getLeadingDetachedCommentsList().isEmpty();
    }

    @Value
    @Builder(toBuilder = true)
    private static class CommentInfo {
        @Singular("path")
        List<Integer> path;
        @Singular
        List<String> leadingComments;
        @Singular
        List<String> trailingComments;
        @Singular
        List<String> detachedLeadingComments;
    }

    @Value
    @Builder(toBuilder = true)
    private static class JavaInfo {
        String javaName;
        CommentInfo commentInfo;
    }
}
