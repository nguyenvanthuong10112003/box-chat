package com.box_chat.identity_service.service;

import com.box_chat.identity_service.configuration.RedisConfiguration;
import com.box_chat.identity_service.dto.request.CustomPageRequest;
import com.box_chat.identity_service.dto.response.CustomPage;
import com.box_chat.identity_service.dto.response.UserResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Objects;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class UserRedisService {
    RedisTemplate<String, Object> redisTemplate;
    ObjectMapper objectMapper;

    public UserResponse getMind(String userId) throws JsonProcessingException {
        String key = null;
        try {
            key = RedisConfiguration.generateKey("user-mind", new HashMap<>() {
                {
                    put("id", userId);
                }
            });
        } catch (NullPointerException nullPointerException) {
            return null;
        }
        log.error("key: {}", key);
        String json = (String) redisTemplate.opsForValue().get(key);
        if (Strings.isBlank(json)) return null;
        return objectMapper.readValue(json, new TypeReference<UserResponse>() {});
    }

    public void saveMind(UserResponse userResponse) throws JsonProcessingException {
        String key = null;
        try {
            key = RedisConfiguration.generateKey("user-mind", new HashMap<>() {
                {
                    put("id", userResponse.getId());
                }
            });
        } catch (NullPointerException nullPointerException) {
            return;
        }
        log.error("key: {}", key);
        String json = objectMapper.writeValueAsString(userResponse);
        redisTemplate.opsForValue().set(key, json);
    }

    public void deleteCacheMind(String userId) {
        try {
            redisTemplate.delete(RedisConfiguration.generateKey("user-mind", new HashMap<>() {
                {
                    put("id", userId);
                }
            }));
        } catch (NullPointerException nullPointerException) {}
    }

    public UserResponse getFindUser(String email) throws JsonProcessingException {
        String key = null;
        try {
            key = RedisConfiguration.generateKey("user-find", new HashMap<>() {
                {
                    put("email", email);
                }
            });
        } catch (NullPointerException nullPointerException) {
            return null;
        }
        log.error("key: {}", key);
        String json = (String) redisTemplate.opsForValue().get(key);
        if (Strings.isBlank(json)) return null;
        return objectMapper.readValue(json, new TypeReference<UserResponse>() {});
    }

    public void saveFindUser(UserResponse userResponse) throws JsonProcessingException {
        String key = null;
        try {
            key = RedisConfiguration.generateKey("user-find", new HashMap<>() {
                {
                    put("email", userResponse.getEmail());
                }
            });
        } catch (NullPointerException nullPointerException) {
            return;
        }
        log.error("key: {}", key);
        String json = objectMapper.writeValueAsString(userResponse);
        redisTemplate.opsForValue().set(key, json);
    }

    public void deleteCacheFind(String email) {
        try {
            redisTemplate.delete(RedisConfiguration.generateKey("user-find", new HashMap<>() {
                {
                    put("email", email);
                }
            }));
        } catch (NullPointerException nullPointerException) {}
    }

    public CustomPage<UserResponse> getPage(CustomPageRequest pageRequest) throws JsonProcessingException {
        String key = null;
        try {
            key = RedisConfiguration.generateKey("user-all", new HashMap<>() {
                {
                    put("page_number", String.valueOf(pageRequest.getPageNumber()));
                    put("page_size", String.valueOf(pageRequest.getPageSize()));
                    put("email_match", pageRequest.getKeySearch());
                }
            });
        } catch (NullPointerException nullPointerException) {
            return null;
        }
        log.error("key: {}", key);
        String json = (String) redisTemplate.opsForValue().get(key);
        if (Strings.isBlank(json)) return null;
        return objectMapper.readValue(json, new TypeReference<CustomPage<UserResponse>>() {});
    }

    public void savePage(CustomPageRequest pageRequest, CustomPage<UserResponse> page) throws JsonProcessingException {
        String key = null;
        try {
            key = RedisConfiguration.generateKey("user-all", new HashMap<>() {
                {
                    put("page_number", String.valueOf(pageRequest.getPageNumber()));
                    put("page_size", String.valueOf(pageRequest.getPageSize()));
                    put("email_match", pageRequest.getKeySearch());
                }
            });
        } catch (NullPointerException nullPointerException) {
            return;
        }
        log.error("key: {}", key);
        String json = objectMapper.writeValueAsString(page);
        redisTemplate.opsForValue().set(key, json);
    }

    public void deleteCache() {
        String pattern = "user-all*";
        try {
            Objects.requireNonNull(redisTemplate.keys(pattern))
                .forEach(redisTemplate::delete);
        } catch (NullPointerException nullPointerException) {}
    }
}
