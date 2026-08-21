package de.heuboe.asfinag.vmis2.tls.rcv.cfgmock.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import de.heuboe.asfinag.vmis2.tls.rcv.cfgmock.MockedCfgGetterChg;
import de.heuboe.tls.receiver.interfaces.SystemMessageManagement;

@ComponentScan( basePackageClasses = {
        de.heuboe.tls.receiveconverter.InitAllInit.class
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
