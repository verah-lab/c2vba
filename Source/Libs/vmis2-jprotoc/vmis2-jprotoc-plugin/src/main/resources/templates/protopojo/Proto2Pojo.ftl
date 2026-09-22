// Generated from file ${ctx.inputfileName} as of ${ctx.generationTime}
// by ${ctx.version.groupId} / ${ctx.version.artifactId} / ${ctx.version.version} built ${ctx.version.buildTime}

<#import "Proto2PojoFile.ftl" as pf>
<#import "Proto2PojoEnum.ftl" as pe>
package ${ctx.packageName};

<#if ctx.message.enum == false>
import lombok.*;
import lombok.experimental.NonFinal;

import java.util.List;
import java.util.stream.Collectors;
import java.time.Instant;
import java.io.IOException;
import de.heuboe.vmis2.jprotoc.utils.DateUtils;
import de.heuboe.vmis2.jprotoc.helpers.PAny;
import de.heuboe.vmis2.jprotoc.transferinterface.HbProtoBufPojo;
import de.heuboe.vmis2.jprotoc.transferinterface.IIDContainer;

import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.ByteString;
import com.google.protobuf.Any;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.BoolValue;
import com.google.protobuf.StringValue;
import com.google.protobuf.BytesValue;


</#if>
<#if ctx.message.enum == true>
    <@pe.createEnum ctx.message/>
<#else>
    <@pf.createFile ctx.message/>
</#if>
