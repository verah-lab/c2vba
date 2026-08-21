package de.heuboe.tls.receiver.test.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import de.heuboe.tls.receiver.interfaces.SystemMessageManagement;
import de.heuboe.tls.receiver.test.MockedCfgGetterChg;

@ComponentScan( basePackageClasses = {
        de.heuboe.now.receiveconverter.InitAllInit.class
        } )

@Component
        public class ConfChg /* implements INotificationToApp */ {
    
    @Autowired
    MockedCfgGetterChg mockedCfgGetterChg;

    @Bean
    public SystemMessageManagement getMessageManagement() {
        return new Vmis2SystemMessageManagement();
    }

}
