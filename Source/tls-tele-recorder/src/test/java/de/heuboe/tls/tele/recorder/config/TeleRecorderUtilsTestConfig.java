package de.heuboe.tls.tele.recorder.config;

import de.heuboe.tls.tele.recorder.utils.TeleRecorderUtils;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.io.File;

@TestConfiguration
public class TeleRecorderUtilsTestConfig {


    @Bean
    public TeleRecorderUtils teleRecorderUtils(TeleRecorderProperties teleRecorderProperties) {
        return new TeleRecorderUtils();
    }
}
