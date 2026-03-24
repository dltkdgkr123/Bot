package com.sh.bdt.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
    prefix = "redis.keys",
    ignoreInvalidFields = false,
    ignoreUnknownFields = false) // 엄밀한 스캔

/*
 * 레디스 키 컨벤션 - 도메인:목적:타입:식별자
 * 만약, 기존 필드명을 바꾸려면 루아 스크립트에 정의된 필드명도 함께 변경해야 함
 */
public record RedisKeyProperties(
    PostLike postLike
) {

    public record PostLike(
        Batch batch,
        Realtime realtime
    ) {

    }

    public record Batch(
        String metaTime,
        String metaSeq,
        String queueSet,
        String statusPrefix,
        String snapshotPrefix
    ) {

    }

    public record Realtime(
        String statusPrefix
    ) {

    }
}
