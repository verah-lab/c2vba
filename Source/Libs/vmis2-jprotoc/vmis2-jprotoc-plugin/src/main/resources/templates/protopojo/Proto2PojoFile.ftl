<#import "Proto2PojoEnum.ftl" as pe>
<#import "Proto2PojoPojoPropertyToProto.ftl" as pojoPropToProto>
<#import "Proto2PojoProtoPropertyToPojo.ftl" as protoPropToPojo>
<#import "Proto2PojoAddComment.ftl" as ac>
<#import "Proto2PojoConstructor.ftl" as con>
<#macro createFile ctx indent=0>
<#local spc>${""?left_pad(indent * 4)}</#local>
<#--  -->
<#if ctx.nested == false>
<#-- 
<#assign theClassName = "P" + ctx.className>
 -->
<#local theClassName>P${ctx.className}</#local>
<#else>
<#-- 
<#assign theClassName = ctx.className>
 -->
<#local theClassName>${ctx.className}</#local>
</#if>
${spc}/**
${spc} * Protopojo type {@link ${theClassName} ${theClassName} (Pojo)}
${spc} * for protobuf type {@link ${ctx.protoPath}.${ctx.className} ${ctx.className} (Proto)}.
    <#if ctx.commentInfo??>
        <@ac.addMessageComment ctx.commentInfo spc/>
    </#if>
${spc} */
${spc}@Value
${spc}@Builder(toBuilder=true, builderClassName="${theClassName}Builder")
${spc}@org.springframework.data.mongodb.core.mapping.Document("${ctx.className}")
<#-- Annotation remove and generated directly to place @PersistenceConstructor annotation.
${spc}@AllArgsConstructor
 -->
<#if ctx.properties?has_content>
${spc}@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
${spc}@ToString(doNotUseGetters = true) //don't use getter to not trigger lazy PAny initialization
</#if>
<#if ctx.nested == false>
${spc}public class ${theClassName} implements de.heuboe.vmis2.jprotoc.transferinterface.HbProtoBufPojo<#if ctx.hasIid>, IIDContainer</#if> {
<#else>
${spc}public static class ${theClassName} implements de.heuboe.vmis2.jprotoc.transferinterface.HbProtoBufPojo<#if ctx.hasIid>, IIDContainer</#if> {
</#if>

<#list ctx.nestedMessages as msg>
    <#if msg.enum>
        <@pe.createEnum msg indent+1/>
    <#else>
        <@createFile msg indent+1/>
    </#if>
</#list>

<#local hasAny = false/>
<#list ctx.properties as property>
    <#if property.commentInfo??>
        <@ac.addComment property.commentInfo spc/>
    </#if>
<#if property.list == true>
    <#if property.date>
${spc}    @Builder.Default @NonFinal List<Instant> ${property.propertyName}List = java.util.Collections.emptyList();
    <#elseif property.scalar == false>
${spc}    @Builder.Default @NonFinal List<${property.javaPackage}.P${property.type}> ${property.propertyName}List = java.util.Collections.emptyList();
    <#else>
${spc}    @Builder.Default @NonFinal List<${property.type}> ${property.propertyName}List = java.util.Collections.emptyList();
    </#if>
<#else>
    <#if property.date>
${spc}    Instant ${property.propertyName};
    <#elseif property.any>
        <#local hasAny = true/>
${spc}    //special Any handling
${spc}    @NonFinal @Transient Any ${property.propertyName}Any;
${spc}    @NonFinal
${spc}    @org.springframework.data.annotation.AccessType(org.springframework.data.annotation.AccessType.Type.PROPERTY)
${spc}    HbProtoBufPojo ${property.propertyName};
    <#elseif property.scalar == false>
${spc}    ${property.javaPackage}.P${property.type} ${property.propertyName};
    <#else>
        <#if property.propertyName == "iid">
${spc}    @org.springframework.data.annotation.Id
        </#if>
${spc}    ${property.type} ${property.propertyName};
    </#if>
</#if>
</#list>

${spc}    /**
${spc}     * All Args Constructor (without Any)
${spc}     *
<#list ctx.properties as property>
    <#if property.list == true>
${spc}     * @param ${property.propertyName}List see getter 
    <#else>
${spc}     * @param ${property.propertyName} see getter 
    </#if>
</#list>
${spc}     */
${spc}    @PersistenceConstructor
${spc}    public ${theClassName}(
    <@con.addConstructor ctx spc false/>

<#if hasAny == true>
${spc}    private ${theClassName}(
    <@con.addConstructor ctx spc true/>
</#if>

<#list ctx.properties as property>
    <#if property.any == true>
        <#if property.list == true>
        <#else>
${spc}    public HbProtoBufPojo get${property.propertyName?cap_first}() {
${spc}        if( ${property.propertyName} == null ) {
${spc}            ${property.propertyName} = PAny.unpack(${property.propertyName}Any);
${spc}        }
${spc}        return ${property.propertyName};
${spc}    }

${spc}    public Any get${property.propertyName?cap_first}Any() {
${spc}        if( ${property.propertyName}Any == null ) {
${spc}            ${property.propertyName}Any = PAny.pack(${property.propertyName});
${spc}        }
${spc}        return ${property.propertyName}Any;
${spc}    }

        </#if>
    </#if>
</#list>

${spc}    /**
${spc}     * Converts the given protobuf instance to an instance of this pojo class.
${spc}     *
${spc}     * @param proto The protobuf instance to convert.
${spc}     * @return The converted pojo instance.
${spc}     */
${spc}    public static ${theClassName} from(${ctx.protoPath}.${ctx.className} proto) {
${spc}        ${theClassName}Builder builder = ${theClassName}.builder();
<#list ctx.properties as property>
              <@protoPropToPojo.assignProtoPropertyToPojoPoperty ctx property indent+1/>
</#list>
${spc}        return builder.build();
${spc}    }

${spc}    /**
${spc}     * Converts the given pojo to an instance of the related protobuf class.
${spc}     *
${spc}     * @param pojo The pojo to convert.
${spc}     * @return The converted protobuf instance.
${spc}     */
${spc}    public static ${ctx.protoPath}.${ctx.className} to(${theClassName} pojo) {

${spc}        ${ctx.protoPath}.${ctx.className}.Builder builder = ${ctx.protoPath}.${ctx.className}.newBuilder();
<#list ctx.properties as property>
              <@pojoPropToProto.assignPojoPropertyToProtoPoperty ctx property indent+1/>
</#list>
${spc}        return builder.build();
${spc}    }

${spc}    /**
${spc}     * Serializes the given pojo to a byte array.
${spc}     *
${spc}     * @param pojo The pojo to convert.
${spc}     * @return The byte array representing the given pojo.
${spc}     */
${spc}    public static byte[] toBytes(${theClassName} pojo) {
${spc}        return to(pojo).toByteArray();
${spc}    }

${spc}    /**
${spc}     * Deserializes the given input bytes to a pojo class.
${spc}     *
${spc}     * @param input The input bytes to convert.
${spc}     * @return The pojo represented by the given byte array.
${spc}     * @throws InvalidProtocolBufferException If the given input data do not match those required for a
${spc}     *         pojo.
${spc}     */
${spc}    public static ${theClassName} fromBytes( byte[] input ) throws InvalidProtocolBufferException {
${spc}        try {
${spc}            return from( ${ctx.protoPath}.${ctx.className}.parseFrom(input) );
${spc}        } catch (IllegalArgumentException|NullPointerException e) {
${spc}            throw new InvalidProtocolBufferException("When converting pojo from byte[]", new IOException(e));
${spc}        }
${spc}    }

${spc}    /**
${spc}     * Returns the transfer instance that provides methods for easy conversion from and to pojo and
${spc}     * protobuf instances or plain bytes.
${spc}     *
${spc}     * @return The transfer instance.
${spc}     */
${spc}    public static Transfer transfer() {
${spc}        return Transfer.INSTANCE;
${spc}    }

${spc}    /**
${spc}     * The transfer class belonging to the {@link ${theClassName}}.
${spc}     */
${spc}    public static final class Transfer implements de.heuboe.vmis2.jprotoc.transferinterface.PojoTransfer<${ctx.protoPath}.${ctx.className}, ${theClassName}> {

${spc}        /**
${spc}         * The singleton instance of this transfer class.
${spc}         */
${spc}        public static final Transfer INSTANCE = new Transfer();

${spc}        private Transfer() {}

${spc}        @Override
${spc}        public Class<${ctx.protoPath}.${ctx.className}> protoClass() {
${spc}            return ${ctx.protoPath}.${ctx.className}.class;
${spc}        }

${spc}        @Override
${spc}        public Class<${theClassName}> pojoClass() {
${spc}            return ${theClassName}.class;
${spc}        }

${spc}        @Override
${spc}        public com.google.protobuf.Descriptors.Descriptor getDescriptor() {
${spc}            return ${ctx.protoPath}.${ctx.className}.getDescriptor();
${spc}        }

${spc}        @Override
${spc}        public ${theClassName} fromProto(${ctx.protoPath}.${ctx.className} proto) {
${spc}            return ${theClassName}.from(proto);
${spc}        }

${spc}        @Override
${spc}        public ${ctx.protoPath}.${ctx.className} toProto(${theClassName} pojo) {
${spc}            return ${theClassName}.to(pojo);
${spc}        }

${spc}        @Override
${spc}        public byte[] toBytes(${theClassName} pojo) {
${spc}            return to(pojo).toByteArray();
${spc}        }

${spc}        @Override
${spc}        public ${theClassName} fromBytes(byte[] input) throws InvalidProtocolBufferException {
${spc}            return from(protoFromBytes(input));
${spc}        }

${spc}        @Override
${spc}        public ${ctx.protoPath}.${ctx.className} protoFromBytes(byte[] input) throws InvalidProtocolBufferException {
${spc}            return ${ctx.protoPath}.${ctx.className}.parseFrom(input);
${spc}        }

${spc}        @Override
${spc}        public int hashCode() {
${spc}            return getClass().getName().hashCode();
${spc}        }

${spc}        @Override
${spc}        public boolean equals(Object obj) {
${spc}            return obj instanceof ${theClassName}.Transfer;
${spc}        }

${spc}        @Override
${spc}        public String toString() {
${spc}            return getClass().getName() + " (Pojo)";
${spc}        }

${spc}    }

${spc}}
</#macro>

