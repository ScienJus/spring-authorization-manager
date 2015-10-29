package com.scienjus.authorization.manager.impl;

import com.scienjus.authorization.exception.MethodNotSupportException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @author XieEnlong
 * @date 2015/10/26.
 */
public class RedisTokenManager extends AbstractTokenManager {

    private static final String REDIS_KEY_PREFIX = "AUTHORIZATION_KEY_";
    private static final String REDIS_TOKEN_PREFIX = "AUTHORIZATION_TOKEN_";

    protected JedisPool jedisPool;

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    public void delRelationshipByKey(String key) {
        if (!singleSignOn) {
            throw new MethodNotSupportException("非单点登录时无法调用该方法");
        }
        String token = getToken(key);
        if (token != null) {
            delete(formatKey(key), formatToken(token));
        }
    }

    @Override
    public void delRelationshipByToken(String token) {
        if (singleSignOn) {
            String key = getKey(token);
            delete(formatKey(key), formatToken(token));
        }
        delete(formatToken(token));
    }

    @Override
    public void createRelationship(String key, String token) {
        if (singleSignOn) {
            set(formatKey(key), token, tokenExpireSeconds);
        }
        set(formatToken(token), key, tokenExpireSeconds);
    }

    @Override
    public String getKey(String token) {
        if (token == null) {
            return null;
        }
        String key = get(formatToken(token));
        if (key != null) {
            if (singleSignOn) {
                expire(formatKey(key), tokenExpireSeconds);
            }
            if (flushExpireAfterOperate) {
                expire(formatToken(token), tokenExpireSeconds);
            }
        }
        return key;
    }

    private String get(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.get(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    private String set(String key, String value, int expireSeconds) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.setex(key, expireSeconds, value);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    private void expire(String key, int seconds) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.expire(key, seconds);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    private void delete(String... keys) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.del(keys);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
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
