package com.lumanlab.parentcaringservice.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lumanlab.parentcaringservice.security.jwt.domain.JwkData;
import com.lumanlab.parentcaringservice.security.jwt.domain.JwkMetadata;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key serializer
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value serializer with custom ObjectMapper
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper());
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.setDefaultSerializer(jsonSerializer);
        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Java Time 지원
        objectMapper.registerModule(new JavaTimeModule());

        // 타입 정보 포함하여 직렬화/역직렬화 (다형성 지원)
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build(),
                ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        return objectMapper;
    }

    /**
     * JWK 키 데이터 전용 RedisTemplate
     */
    @Bean
    public RedisTemplate<String, JwkData> jwtKeyDataRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, JwkData> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // String serializer for keys
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // JSON serializer for JwtKeyData values
        Jackson2JsonRedisSerializer<JwkData> jsonSerializer =
                new Jackson2JsonRedisSerializer<>(jwtKeyObjectMapper(), JwkData.class);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * JWK 키 메타데이터 전용 RedisTemplate
     */
    @Bean
    public RedisTemplate<String, JwkMetadata> jwtKeyMetadataRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, JwkMetadata> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // String serializer for keys
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // JSON serializer for JwtKeyMetadata values
        Jackson2JsonRedisSerializer<JwkMetadata> jsonSerializer =
                new Jackson2JsonRedisSerializer<>(jwtKeyObjectMapper(), JwkMetadata.class);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * String 값 전용 RedisTemplate (키 ID 저장용)
     */
    @Bean
    public RedisTemplate<String, String> stringLiteralRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * JWK 키 관련 객체를 위한 전용 ObjectMapper
     */
    @Bean
    public ObjectMapper jwtKeyObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // Java Time 지원
        objectMapper.registerModule(new JavaTimeModule());
        // 알 수 없는 속성 무시
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper;
    }
}
