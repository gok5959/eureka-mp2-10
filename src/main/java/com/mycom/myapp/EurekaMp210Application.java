package com.mycom.myapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EurekaMp210Application {

    public static void main(String[] args) {
        SpringApplication.run(EurekaMp210Application.class, args);
    }

}
