package de.heuboe.tls.receiver.test.config;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Component
@ConfigurationProperties( "de.heuboe.tls.receiver.config.alarm.manager" )
@Validated
@Data
@FieldDefaults( level = AccessLevel.PRIVATE )
public class Vmis2SystemMessageManagementProperties {

    @NotNull String topic;
    @Min( 0 ) int   groupId;
    @Min( 0 ) int   groupIdWithoutObjectReference;
    @NotNull String eventSource;

}
