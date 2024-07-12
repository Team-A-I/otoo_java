package com.project.otoo_java.redis.entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.concurrent.TimeUnit;


@NoArgsConstructor
@Getter
@RedisHash("refreshToken")
public class RedisRefreshToken {
    @Id
    private String id;

    private String refreshToken;

    @TimeToLive(unit = TimeUnit.SECONDS)
    private Long expiration;

    public RedisRefreshToken(String email, String refreshToken, Long expiration) {
        this.id = email;
        this.refreshToken = refreshToken;
        this.expiration = expiration;
    }

    public RedisRefreshToken updateToken(String refreshToken, Long expiration){
        this.refreshToken = refreshToken;
        this.expiration = expiration;
        return this;
    }
}
