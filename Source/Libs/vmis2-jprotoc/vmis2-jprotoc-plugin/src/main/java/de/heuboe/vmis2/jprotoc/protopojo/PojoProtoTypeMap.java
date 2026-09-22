/*
 *  Copyright (c) 2017, salesforce.com, inc.
 *  All rights reserved.
 *  Licensed under the BSD 3-Clause license.
 *  For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package de.heuboe.vmis2.jprotoc.protopojo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.DescriptorProtos;

import lombok.Getter;

/**
 * {@code ProtoTypeMap} maintains a dictionary for looking up Java type names when given proto types.
 */
public final class PojoProtoTypeMap {

    /**
     * Info of types in GenerationRequest
     */
    @Getter
    static class JavaInfo {
        String protoName;
        String protoPackage;
        String className;
        String javaPackage;
        String nestedPath;
        boolean isEnum = false;
    }

    private final ImmutableMap<String, JavaInfo> types;

    private PojoProtoTypeMap(@Nonnull ImmutableMap<String, JavaInfo> types) {
        Preconditions.checkNotNull(types, "types");

        this.types = types;
    }

    /**
     * Returns an instance of {@link PojoProtoTypeMap} based on the given FileDescriptorProto instances.
     *
     * @param fileDescriptorProtos the full collection of files descriptors from the code generator request
     * @param pojoSubPackage sub package for generated pojo classes
     * @return map of type to info
     */
    public static PojoProtoTypeMap of(@Nonnull Collection<DescriptorProtos.FileDescriptorProto> fileDescriptorProtos, @Nonnull String pojoSubPackage) {
        Preconditions.checkNotNull(fileDescriptorProtos, "fileDescriptorProtos");
        Preconditions.checkArgument(!fileDescriptorProtos.isEmpty(), "fileDescriptorProtos.isEmpty()");

        Map<String, JavaInfo> javaInfoMap = new HashMap<>();
        Joiner dotJointer = Joiner.on(".").skipNulls(); //NOSONAR: String join does not support 'skipNulls'
        for (final DescriptorProtos.FileDescriptorProto fileDescriptor : fileDescriptorProtos) {
            List<JavaInfo> javaInfos = new ArrayList<>();
            final DescriptorProtos.FileOptions fileOptions = fileDescriptor.getOptions();

            final String protoPackageName = Strings.emptyToNull(fileDescriptor.hasPackage() ? "." + fileDescriptor.getPackage() : "");
            final String protoPackage = Strings.emptyToNull(
                fileOptions.hasJavaPackage() ?
                    fileOptions.getJavaPackage() :
                    fileDescriptor.getPackage());
            final String javaPackage = Strings.emptyToNull(
                protoPackage != null ? protoPackage + "." + pojoSubPackage : "");



            javaInfos.addAll(toEnumList(fileDescriptor.getEnumTypeList(),  protoPackage, javaPackage,  ""));
            javaInfos.addAll(toMessageList(fileDescriptor.getMessageTypeList(), protoPackage, javaPackage, ""));

            Map<String, JavaInfo> m = javaInfos.stream().collect(
                Collectors.toMap(
                    ji -> dotJointer.join(
                        protoPackageName!=null&&protoPackageName.startsWith(".")?protoPackageName.substring(1):protoPackageName,
                        Strings.emptyToNull(ji.getNestedPath()),
                        ji.getProtoName()
                    ), Function.identity())
            );

            javaInfoMap.putAll(m);

        }

        return new PojoProtoTypeMap(ImmutableMap.copyOf(javaInfoMap));
    }


    private static List<JavaInfo> toEnumList(List<DescriptorProtos.EnumDescriptorProto> enums, final String protoPackage, final String javaPackage, String nestedPath) {
        return enums.stream().map(edp -> {
            JavaInfo ji = new JavaInfo();
            ji.protoName = edp.getName();
            ji.className = toFullClassName(nestedPath, edp.getName());
            ji.javaPackage = javaPackage;
            ji.protoPackage = protoPackage;
            ji.nestedPath = nestedPath;
            ji.isEnum = true;
            return ji;
        }).collect(Collectors.toList());
    }

    private static String toFullClassName(String nestedPath, String name) {
        if( nestedPath == null || nestedPath.isEmpty() ) {
            return name;
        }

        return nestedPath + "." + name;
    }

    private static List<JavaInfo> toMessageList(List<DescriptorProtos.DescriptorProto> messages, final String protoPackage, final String javaPackage, String nestedPath) {
        List<JavaInfo> javaInfos = new ArrayList<>();
        messages.forEach(
            dp -> {
                javaInfos.addAll(toEnumList(dp.getEnumTypeList(), protoPackage, javaPackage, nestedPath.isEmpty()?dp.getName():nestedPath + "." + dp.getName()));
                javaInfos.addAll(toMessageList(dp.getNestedTypeList(), protoPackage, javaPackage, nestedPath.isEmpty()?dp.getName():nestedPath + "." + dp.getName()));
                JavaInfo ji = new JavaInfo();
                ji.protoName = dp.getName();
                ji.className = toFullClassName(nestedPath, dp.getName());
                ji.protoPackage = protoPackage;
                ji.javaPackage = javaPackage;
                ji.nestedPath = nestedPath;
                javaInfos.add(ji);
            }
        );

        return javaInfos;
    }

    /**
     * Returns the full Java type name for the given proto type.
     *
     * @param protoTypeName the proto type to be converted to a Java type
     * @return java info for protoTypeName
     */
    public JavaInfo toJavaType(@Nonnull String protoTypeName) {
        Preconditions.checkNotNull(protoTypeName, "protoTypeName");
        String name = protoTypeName;
        if( name.startsWith(".") ) {
            name = name.substring(1);
        }
        return types.get(name);
    }

}
