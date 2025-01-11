package com.taoziyoyo.files;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class FilesApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FilesApiApplication.class, args);
    }

}
