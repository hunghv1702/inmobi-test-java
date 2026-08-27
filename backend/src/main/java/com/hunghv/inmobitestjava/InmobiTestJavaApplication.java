package com.hunghv.inmobitestjava;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class InmobiTestJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(InmobiTestJavaApplication.class, args);
    }
}
