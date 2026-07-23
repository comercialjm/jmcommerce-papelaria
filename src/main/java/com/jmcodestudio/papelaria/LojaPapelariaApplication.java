package com.jmcodestudio.papelaria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LojaPapelariaApplication {

    public static void main(String[] args) {
        SpringApplication.run(LojaPapelariaApplication.class, args);
    }

}
