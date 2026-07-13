package com.saas.cloud_storage_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.TimeZone;

@SpringBootApplication
@EnableJpaAuditing
public class CloudStorageAppApplication {

    public static void main(String[] args) {
       
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        SpringApplication.run(CloudStorageAppApplication.class, args);
    }
}