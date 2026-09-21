package de.heuboe.asfinag.control.base.services;


import org.springframework.context.annotation.Bean;

public class RuntimeBeanFactory {

    @Bean
    public Runner createRuntimeBean(Runner.CallbackInterface callback, boolean lastForEachKey) {
        return new Runner(callback, lastForEachKey);
    }
}
