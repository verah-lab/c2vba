<#macro addComment ci spc>
<#if ci??>
    <#if ci.leadingComments?has_content || ci.trailingComments?has_content>
${spc}    /**
    </#if>
    <#list ci.leadingComments as leadingComment>
${spc}    * <p>${leadingComment}</p>
    </#list>
    <#if ci.leadingComments?has_content && ci.trailingComments?has_content>
${spc}    *
    </#if>
        <#list ci.trailingComments as trailingComment>
${spc}    * <p>${trailingComment}</p>
        </#list>
    <#if ci.leadingComments?has_content || ci.trailingComments?has_content>
${spc}    */
    </#if>
</#if>
</#macro>
<#macro addMessageComment ci spc>
<#if ci??>
    <#if ci.leadingComments?has_content || ci.trailingComments?has_content>
${spc} *
    </#if>
    <#list ci.leadingComments as leadingComment>
${spc} * <p>${leadingComment}</p>
    </#list>
    <#if ci.leadingComments?has_content && ci.trailingComments?has_content>
${spc} *
    </#if>
        <#list ci.trailingComments as trailingComment>
${spc} * <p>${trailingComment}</p>
        </#list>
    <#if ci.leadingComments?has_content || ci.trailingComments?has_content>
${spc} *
    </#if>
</#if>
</#macro>