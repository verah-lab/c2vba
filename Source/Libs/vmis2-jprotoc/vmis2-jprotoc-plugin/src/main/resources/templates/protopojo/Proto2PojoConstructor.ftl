<#macro addConstructor ctx spc addAny>
<#list ctx.properties as property>
    <#if property.list == true>
        <#if property.date>
${spc}      List<Instant> ${property.propertyName}List${property?has_next?then(',', '')}
        <#elseif property.scalar == false>
${spc}      List<${property.javaPackage}.P${property.type}> ${property.propertyName}List${property?has_next?then(',', '')}
        <#else>
${spc}      List<${property.type}> ${property.propertyName}List${property?has_next?then(',', '')}
        </#if>
    <#else>
        <#if property.date>
${spc}      Instant ${property.propertyName}${property?has_next?then(',', '')}
        <#elseif property.any>
            <#if addAny == true>
${spc}      Any ${property.propertyName}Any,
            </#if>
${spc}      HbProtoBufPojo ${property.propertyName}${property?has_next?then(',', '')}
        <#elseif property.scalar == false>
${spc}      ${property.javaPackage}.P${property.type} ${property.propertyName}${property?has_next?then(',', '')}
        <#elseif property.primitive>
${spc}      ${property.boxedType} ${property.propertyName}${property?has_next?then(',', '')}
        <#else>
${spc}      ${property.type} ${property.propertyName}${property?has_next?then(',', '')}
        </#if>
    </#if>
</#list>
${spc}    ){
<#list ctx.properties as property>
    <#if property.list == true>
${spc}        this.${property.propertyName}List = ${property.propertyName}List;
    <#elseif property.primitive>
${spc}        this.${property.propertyName} = ${property.propertyName} == null ? ${property.booleanProperty?then('false','0')} : ${property.propertyName};
    <#else>
        <#if property.any == true>
${spc}        this.${property.propertyName}Any = ${property.propertyName}Any;
        </#if>
${spc}        this.${property.propertyName} = ${property.propertyName};
    </#if>
</#list>
${spc}    }

</#macro>