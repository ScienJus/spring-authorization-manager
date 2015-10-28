package com.scienjus.authorization.manager.impl;

import com.scienjus.authorization.exception.MethodNotSupportException;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
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

    public void setRedis(JedisConnectionFactory redis) {
        this.redis = new StringRedisTemplate(redis);
    }

    @Override
    public void delRelationshipByKey(String key) {
        if (!singleSignOn) {
            throw new MethodNotSupportException("非单点登录时无法调用该方法");
        }
        String token = getToken(key);
        if (token != null) {
            redis.delete(formatKey(key));
            redis.delete(formatToken(token));
        }
    }

    @Override
    public void delRelationshipByToken(String token) {
        if (singleSignOn) {
            String key = getKey(token);
            redis.delete(formatKey(key));
        }
        redis.delete(formatToken(token));
    }

    @Override
    public void createRelationship(String key, String token) {
        if (singleSignOn) {
            redis.boundValueOps(formatKey(key)).set(token, tokenExpireSeconds, TimeUnit.SECONDS);
        }
        redis.boundValueOps(formatToken(token)).set(key, tokenExpireSeconds, TimeUnit.SECONDS);
    }

    @Override
    public String getKey(String token) {
        if (token == null) {
            return null;
        }
        String key = redis.boundValueOps(formatToken(token)).get();
        if (key != null) {
            if (singleSignOn) {
                redis.expire(formatKey(key), tokenExpireSeconds, TimeUnit.SECONDS);
            }
            if (flushExpireAfterOperate) {
                redis.expire(formatToken(token), tokenExpireSeconds, TimeUnit.SECONDS);
            }
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
