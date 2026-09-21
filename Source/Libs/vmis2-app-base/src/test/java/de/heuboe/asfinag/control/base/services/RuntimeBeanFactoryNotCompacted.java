package de.heuboe.asfinag.control.base.services;


import org.springframework.context.annotation.Bean;

public class RuntimeBeanFactoryNotCompacted {

    @Bean
    public RunnerNotCompacted createRuntimeBean(RunnerNotCompacted.CallbackInterface callback, boolean lastForEachKey) {
        return new RunnerNotCompacted(callback, lastForEachKey);
    }
}
