package com.box_chat.identity_service.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
public class RedisConfiguration {
    @Value("${spring.redis.host}")
    String REDIS_HOST;
    @Value("${spring.redis.port}")
    int REDIS_PORT;
    @Bean
    LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration =
            new RedisStandaloneConfiguration(REDIS_HOST, REDIS_PORT);
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        redisTemplate.afterPropertiesSet();
        redisTemplate.keys("*").stream().forEach(redisTemplate::delete);
        return redisTemplate;
    }

    @Bean
    ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ISO_DATE_TIME));
        simpleModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE_TIME));
        simpleModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ISO_DATE));
        simpleModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ISO_DATE));
        objectMapper.registerModule(simpleModule);
        return objectMapper;
    }

    public static String generateKey(String key, Map<String, String> params) throws NullPointerException {
        return key + "--" + Objects.requireNonNull(params)
            .keySet()
            .stream()
            .map(item -> item + ":" + params.get(item))
            .collect(Collectors.joining("-"));
    }
}
