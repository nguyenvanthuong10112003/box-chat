package com.box_chat.identity_service.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
public class AppConfiguration {
    @Bean
    Random crateRandom() {
        return new Random();
    }
}
