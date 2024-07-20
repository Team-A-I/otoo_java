package com.project.otoo_java;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;


@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class OtooJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(OtooJavaApplication.class, args);
    }

}
