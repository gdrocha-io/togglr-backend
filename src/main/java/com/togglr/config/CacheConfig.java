package com.togglr.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

@Configuration
@EnableCaching
@RequiredArgsConstructor
@Slf4j
public class CacheConfig {
    @Value("${spring.cache.redis.time-to-live:3600000}")
    private long redisTtl;

    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "caffeine", matchIfMissing = true)
    public CacheManager caffeineCacheManager() {
        log.info("Initializing Caffeine cache manager (in-memory cache)");
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("features", "metrics");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .recordStats());
        log.info("Caffeine cache manager initialized successfully");
        return cacheManager;
    }

    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        log.info("Initializing Redis cache manager");

        try {
            redisConnectionFactory.getConnection().ping();
            log.info("Redis connection successful");

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            objectMapper.activateDefaultTyping(
                    objectMapper.getPolymorphicTypeValidator(),
                    ObjectMapper.DefaultTyping.NON_FINAL,
                    com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
            );

            GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

            RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMillis(redisTtl))
                    .serializeKeysWith(fromSerializer(new StringRedisSerializer()))
                    .serializeValuesWith(fromSerializer(serializer))
                    .disableCachingNullValues();

            RedisCacheManager cacheManager = RedisCacheManager.builder(redisConnectionFactory)
                    .cacheDefaults(config)
                    .transactionAware()
                    .build();

            log.info("Redis cache manager initialized successfully");

            return cacheManager;
        } catch (Exception e) {
            log.error("Failed to connect to Redis: {}", e.getMessage());
            throw e;
        }
    }
}
