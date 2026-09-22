<#macro createEnum ctx indent=0>
<#import "Proto2PojoAddComment.ftl" as ac>
<#local spc>${""?left_pad(indent * 4)}</#local>
<#--
<#local theClassName>${""?left_pad(indent * 4)}</#local>
  -->
<#--  -->
<#if ctx.nested == false>
<#assign theClassName = "P" + ctx.className>
<#else>
<#assign theClassName = ctx.className>
</#if>
${spc}/**
${spc} * Protopojo type {@link ${theClassName} ${theClassName} (Pojo)}
${spc} * for protobuf type {@link ${ctx.protoPath}.${ctx.className} ${ctx.className} (Proto)}.
    <#if ctx.commentInfo??>
        <@ac.addMessageComment ctx.commentInfo spc/>
    </#if>
${spc} */
${spc}public enum ${theClassName} implements de.heuboe.vmis2.jprotoc.transferinterface.HbProtoBufJavaEnum {
<#--  -->
<#list ctx.enumValues as enumValue>
    <#if enumValue.commentInfo??>
        <@ac.addComment enumValue.commentInfo spc/>
    </#if>
${spc}    ${enumValue.name}(${enumValue.number?c}),
</#list>
${spc}    UNRECOGNIZED(-1);

${spc}    private int value;

${spc}    private ${theClassName}(int value) {
${spc}        this.value = value;
${spc}    }

${spc}    /**
${spc}     * Gets the enums's numeric value as defined in the .proto file.
${spc}     *
${spc}     * @return The enum's numeric value.
${spc}     */
${spc}    @Override
${spc}    public final int getNumber() {
${spc}        if (this == UNRECOGNIZED) {
${spc}            throw new java.lang.IllegalArgumentException(
${spc}                "Can't get the number of an unknown enum value.");
${spc}        }
${spc}        return value;
${spc}    }

${spc}    /**
${spc}     * Converts the given proto enum value to its corresponding pojo instance.
${spc}     *
${spc}     * @param proto The proto enum value to convert.
${spc}     * @return The pojo enum value.
${spc}     */
${spc}    public static ${theClassName} from(${ctx.protoPath}.${ctx.className} proto) {
${spc}        if (${ctx.protoPath}.${ctx.className}.UNRECOGNIZED == proto) {
${spc}            return UNRECOGNIZED;
${spc}        }
${spc}        return forNumber(proto.getNumber());
${spc}    }

${spc}    /**
${spc}     * Converts the given pojo enum value to its corresponding proto instance.
${spc}     *
${spc}     * @param pojo The pojo enum value to convert.
${spc}     * @return The proto enum value.
${spc}     */
${spc}    public static ${ctx.protoPath}.${ctx.className} to(${theClassName} pojo) {
${spc}        if (UNRECOGNIZED == pojo) {
${spc}            return ${ctx.protoPath}.${ctx.className}.UNRECOGNIZED;
${spc}        }
${spc}        return ${ctx.protoPath}.${ctx.className}.forNumber(pojo.getNumber());
${spc}    }

${spc}    /**
${spc}     * Returns the enum instance belonging to the given number based on the definition in the .proto
${spc}     * file.
${spc}     *
${spc}     * @param value The number of the enum to search.
${spc}     * @return The enum instance belonging to the number. Returns the 0-value enum 
${spc}     *         if the number does not belong to any enum instance.
${spc}     */
${spc}    public static ${theClassName} forNumber(int value) {
${spc}        switch (value) {
${spc}            default: // Return default value
<#list ctx.enumValues as enumValue>
${spc}            case ${enumValue.number?c}: return ${enumValue.name};
</#list>
${spc}        }
${spc}    }

${spc}    /**
${spc}     * Returns the transfer instance that provides methods for easy conversion from and to pojo and
${spc}     * protobuf instances.
${spc}     *
${spc}     * @return The transfer instance.
${spc}     */
${spc}    public static Transfer transfer() {
${spc}        return Transfer.INSTANCE;
${spc}    }

${spc}    /**
${spc}     * The transfer class belonging to the {@link ${theClassName}}.
${spc}     */
${spc}    public static final class Transfer implements de.heuboe.vmis2.jprotoc.transferinterface.EnumTransfer<${ctx.protoPath}.${ctx.className}, ${theClassName}> {

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
${spc}        public com.google.protobuf.Descriptors.EnumDescriptor getDescriptor() {
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
${spc}        public ${theClassName} fromNumber(int number) {
${spc}            return ${theClassName}.forNumber(number);
${spc}        }

${spc}        @Override
${spc}        public ${ctx.protoPath}.${ctx.className} protoFromNumber(int number) {
${spc}            return ${ctx.protoPath}.${ctx.className}.forNumber(number);
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
${spc}            return getClass().getName() + " (Enum)";
${spc}        }

${spc}    }

${spc}}
</#macro>

