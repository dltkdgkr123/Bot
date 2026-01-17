package com.sh.bdt.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

@Configuration
public class RedisConfig {

    @Bean
    public RedisScript<Long> likeAggregateScript() {
        return RedisScript.of(new ClassPathResource("scripts/like_aggregate.lua"), Long.class);
    }

    @Bean
    public RedisScript<List> likeBatchProgressScript() {
        return RedisScript.of(new ClassPathResource("scripts/like_batch_progress.lua"), List.class);
    }

    @Bean
    public RedisScript<Boolean> likeBatchAckScript() {
        Resource scriptSource = new ClassPathResource("scripts/like_batch_ack.lua");
        return RedisScript.of(scriptSource, Boolean.class);
    }

    @Bean
    public StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
