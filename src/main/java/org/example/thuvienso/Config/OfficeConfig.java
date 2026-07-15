package org.example.thuvienso.Config;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.ExternalOfficeManager;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OfficeConfig {

    // start() và stop() được Spring gọi tự động qua initMethod/destroyMethod
    @Bean(initMethod = "start", destroyMethod = "stop")
    public OfficeManager officeManager() {
        return ExternalOfficeManager.builder()
                .hostName("libreoffice")
                .portNumbers(3000)
                .build();
    }

    @Bean
    public DocumentConverter documentConverter(OfficeManager officeManager) {
        return LocalConverter.make(officeManager);
    }
}