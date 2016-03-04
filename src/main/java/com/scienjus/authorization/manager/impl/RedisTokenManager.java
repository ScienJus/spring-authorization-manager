package com.scienjus.authorization.manager.impl;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 使用Redis存储Token
 * @author ScienJus
 * @date 2015/10/26.
 */
public class RedisTokenManager extends AbstractTokenManager {

    /**
     * Redis中Key的前缀
     */
    private static final String REDIS_KEY_PREFIX = "AUTHORIZATION_KEY_";

    /**
     * Redis中Token的前缀
     */
    private static final String REDIS_TOKEN_PREFIX = "AUTHORIZATION_TOKEN_";

    /**
     * Jedis连接池
     */
    protected JedisPool jedisPool;

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    public void delSingleRelationshipByKey(String key) {
        String token = getToken(key);
        if (token != null) {
            delete(formatKey(key), formatToken(token));
        }
    }

    @Override
    public void delRelationshipByToken(String token) {
        if (singleTokenWithUser) {
            String key = getKey(token);
            delete(formatKey(key), formatToken(token));
        } else {
            delete(formatToken(token));
        }
    }

    @Override
    protected void createSingleRelationship(String key, String token) {
        String oldToken = get(formatKey(key));
        if (oldToken != null) {
            delete(formatToken(oldToken));
        }
        set(formatToken(token), key, tokenExpireSeconds);
        set(formatKey(key), token, tokenExpireSeconds);
    }

    @Override
    protected void createMultipleRelationship(String key, String token) {
        set(formatToken(token), key, tokenExpireSeconds);
    }

    @Override
    public String getKeyByToken(String token) {
        return get(formatToken(token));
    }

    @Override
    protected void flushExpireAfterOperation(String key, String token) {
        if (singleTokenWithUser) {
            expire(formatKey(key), tokenExpireSeconds);
        }
        expire(formatToken(token), tokenExpireSeconds);
    }

    private String get(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }

    private String set(String key, String value, int expireSeconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.setex(key, expireSeconds, value);
        }
    }

    private void expire(String key, int seconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.expire(key, seconds);
        }
    }

    private void delete(String... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(keys);
        }
    }

    private String getToken(String key) {
        return get(formatKey(key));
    }

    private String formatKey(String key) {
        return REDIS_KEY_PREFIX.concat(key);
    }

    private String formatToken(String token) {
        return REDIS_TOKEN_PREFIX.concat(token);
    }
}
