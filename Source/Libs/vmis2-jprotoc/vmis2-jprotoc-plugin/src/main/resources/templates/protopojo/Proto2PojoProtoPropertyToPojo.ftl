<#macro assignProtoPropertyToPojoPoperty ctx property indent>
<#local spc>${""?left_pad(indent * 4)}</#local>
${spc}    {
<#if property.any == false>
    <#if property.list == true>            
        <#if property.scalar == true>
${spc}        builder.${property.propertyName}List(proto.get${property.propertyName?cap_first}List());
        <#elseif property.date == true>
${spc}        List<Instant> list = proto.get${property.propertyName?cap_first}List().stream()
${spc}                                            .map(m -> DateUtils.toInstant(m))
${spc}                                            .collect(Collectors.toList());
${spc}        builder.${property.propertyName}List(list);
        <#else>
${spc}        List<${property.javaPackage}.P${property.type}> list = proto.get${property.propertyName?cap_first}List().stream()
${spc}                                            .map(m -> ${property.javaPackage}.P${property.type}.from(m))
${spc}                                            .collect(Collectors.toList());
${spc}        builder.${property.propertyName}List(list);
        </#if>
    <#else>                    
        <#if property.scalar == true>
            <#if property.wrapper == true>
${spc}        if( proto.has${property.propertyName?cap_first}()) {
${spc}            builder.${property.propertyName}(proto.get${property.propertyName?cap_first}().getValue());
${spc}        }
            <#else>
${spc}        builder.${property.propertyName}(proto.get${property.propertyName?cap_first}());
            </#if>
        <#elseif property.date == true>
${spc}        if( proto.has${property.propertyName?cap_first}()) {
${spc}            builder.${property.propertyName}(DateUtils.toInstant(proto.get${property.propertyName?cap_first}()));
${spc}        }
        <#else>    
            <#if property.enum == false>
${spc}        if( proto.has${property.propertyName?cap_first}()) {
${spc}            builder.${property.propertyName}(${property.javaPackage}.P${property.type}.from(proto.get${property.propertyName?cap_first}()));
${spc}        }
            <#else>
${spc}        builder.${property.propertyName}(${property.javaPackage}.P${property.type}.from(proto.get${property.propertyName?cap_first}()));
            </#if>
        </#if>                
    </#if>
<#else>
        <#if property.list == true >
        <#else>
${spc}        if( proto.has${property.propertyName?cap_first}()) {
${spc}          builder.${property.propertyName}Any(proto.get${property.propertyName?cap_first}());
${spc}        }
        </#if>
</#if>
${spc}    }
</#macro>