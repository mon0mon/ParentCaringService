package com.lumanlab.parentcaringservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ParentCaringServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParentCaringServiceApplication.class, args);
    }

}
