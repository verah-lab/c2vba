<#macro assignPojoPropertyToProtoPoperty ctx property indent>
<#local spc>${""?left_pad(indent * 4)}</#local>
<#-- 
${spc}    {
<#if property.list == true>
    <#if property.scalar == true>
${spc}        builder.addAll${property.propertyName?cap_first}(pojo.get${property.propertyName?cap_first}List());
    <#elseif property.date == true>
${spc}        List<com.google.protobuf.Timestamp> list = pojo.get${property.propertyName?cap_first}List().stream()
${spc}                                                                         .map(m -> DateUtils.fromInstantUtc(m))
${spc}                                                                         .collect(Collectors.toList());
${spc}        builder.addAll${property.propertyName?cap_first}(list);
    <#else>
${spc}        List<${ctx.protoPath}.${property.type}> list = pojo.get${property.propertyName?cap_first}List().stream()
${spc}                                                                         .map(m -> ${property.javaPackage}.${property.type}.to(m))
${spc}                                                                         .collect(Collectors.toList());
${spc}        builder.addAll${property.propertyName?cap_first}(list);
    </#if>
<#else>    
    <#if property.scalar == true>
        <#if property.booleanProperty == false>
${spc}        builder.set${property.propertyName?cap_first}(pojo.get${property.propertyName?cap_first}());
        <#else>
${spc}        builder.set${property.propertyName?cap_first}(pojo.is${property.propertyName?cap_first}());
        </#if>
    <#elseif property.date == true>
${spc}        builder.set${property.propertyName?cap_first}(DateUtils.fromInstantUtc(pojo.get${property.propertyName?cap_first}()));
    <#else>
${spc}        builder.set${property.propertyName?cap_first}(${property.javaPackage}.${property.type}.to(pojo.get${property.propertyName?cap_first}()));
    </#if>                
</#if>
${spc}    }
-->
<#-- ${spc}{/*Pojo2Proto-new*/}-->
<#if property.any == false>
    <#if property.nullPossibleProperty == true>
${spc}    {
${spc}        ${property.returnTypeOfPojoGet} var = pojo.${property.pojoGetterName};
${spc}        if (null != var) {
        <#if property.enum == true && property.list == false>
${spc}            if (${property.returnTypeOfPojoGet}.UNRECOGNIZED.equals(var)) {
${spc}                builder.${property.protoSetterName}Value(-1);
${spc}            } else {
${spc}                builder.${property.protoSetterName}(
${spc}                    ${property.toProtConvIntro}var${property.streamProcessing}${property.toProtConvOutro}
${spc}                );
${spc}            }
        <#else>
${spc}            builder.${property.protoSetterName}(
${spc}                ${property.toProtConvIntro}var${property.streamProcessing}${property.toProtConvOutro}
${spc}            );
        </#if>
${spc}        }
${spc}    }
    <#else>
${spc}    builder.${property.protoSetterName}(
${spc}        ${property.toProtConvIntro}pojo.${property.pojoGetterName}${property.streamProcessing}${property.toProtConvOutro}
${spc}    );
    </#if>
<#else>
    <#if property.list == true >
    <#else>
${spc}    if( pojo.get${property.propertyName?cap_first}Any() != null ) {
${spc}        builder.set${property.propertyName?cap_first}(pojo.get${property.propertyName?cap_first}Any());
${spc}    }
    </#if>
</#if>
<#--${spc}{/*Pojo2Proto-new*/}-->
</#macro>