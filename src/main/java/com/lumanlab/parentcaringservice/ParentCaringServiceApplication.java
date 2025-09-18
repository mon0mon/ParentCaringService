package com.lumanlab.parentcaringservice;

import com.lumanlab.parentcaringservice.security.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties({JwtProperties.class})
public class ParentCaringServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParentCaringServiceApplication.class, args);
    }

}
