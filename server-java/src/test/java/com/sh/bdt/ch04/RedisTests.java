package com.sh.bdt.ch04;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.redis.test.autoconfigure.DataRedisTest;
import org.springframework.data.redis.connection.RedisConnectionCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

@Tag("ch04")
@DataRedisTest
public class RedisTests {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void ping() {
        // given & when & then
        String result = Assertions.assertDoesNotThrow(
            () -> redisTemplate.execute(RedisConnectionCommands::ping));

        // then
        assertThat(result).isEqualTo("PONG");
    }
}
