package com.autodrive.motors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AutoDriveMotorsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoDriveMotorsApplication.class, args);
    }
}

