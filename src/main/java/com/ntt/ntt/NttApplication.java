package com.ntt.ntt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class NttApplication {

    public static void main(String[] args) {
        SpringApplication.run(NttApplication.class, args);
    }

}
