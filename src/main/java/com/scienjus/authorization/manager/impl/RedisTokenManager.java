package com.scienjus.authorization.manager.impl;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @author XieEnlong
 * @date 2015/10/26.
 */
public class RedisTokenManager extends AbstractTokenManager {

    private static final String REDIS_KEY_PREFIX = "AUTHORIZATION_KEY_";
    private static final String REDIS_TOKEN_PREFIX = "AUTHORIZATION_TOKEN_";

    protected StringRedisTemplate redis;

    public void setRedis(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void delRelationshipByKey(String key) {
        String token = getToken(key);
        redis.delete(formatKey(key));
        redis.delete(formatToken(token));
    }

    @Override
    public void delRelationshipByToken(String token) {
        String key = getKey(token);
        redis.delete(formatKey(key));
        redis.delete(formatToken(token));
    }

    @Override
    public void createRelationship(String key, String token) {
        redis.boundValueOps(formatKey(key)).set(token, tokenExpireSeconds, TimeUnit.SECONDS);
        redis.boundValueOps(formatToken(token)).set(key, tokenExpireSeconds, TimeUnit.SECONDS);
    }

    @Override
    public String getKey(String token) {
        if (token == null) {
            return null;
        }
        String key = redis.boundValueOps(formatToken(token)).get();
        if (key != null) {
            redis.expire(formatKey(key), tokenExpireSeconds, TimeUnit.SECONDS);
            redis.expire(formatToken(token), tokenExpireSeconds, TimeUnit.SECONDS);
        }
        return key;
    }

    private String getToken(String key) {
        return redis.boundValueOps(formatKey(key)).get();
    }

    private String formatKey(String key) {
        return REDIS_KEY_PREFIX.concat(key);
    }

    private String formatToken(String token) {
        return REDIS_TOKEN_PREFIX.concat(token);
    }
}
