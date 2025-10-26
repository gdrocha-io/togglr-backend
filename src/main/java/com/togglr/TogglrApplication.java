package com.togglr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TogglrApplication {
    public static void main(String[] args) {
        SpringApplication.run(TogglrApplication.class, args);
    }
}
