package com.scienjus.authorization.test;

import com.lordofthejars.nosqlunit.redis.ManagedRedis;
import com.lordofthejars.nosqlunit.redis.RedisRule;
import com.scienjus.authorization.manager.TokenManager;
import com.scienjus.authorization.manager.impl.RedisTokenManager;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author XieEnlong
 * @date 2016/3/1.
 */
public class RedisTokenManagerTest {

    static {
        System.setProperty("REDIS_HOME", "/usr/local/redis");
    }

    @ClassRule
    public static ManagedRedis managedRedis = ManagedRedis.ManagedRedisRuleBuilder.newManagedRedisRule().build();

    @Rule
    public RedisRule redisRule = RedisRule.RedisRuleBuilder.newRedisRule().defaultManagedRedis();

    private static JedisPool jedisPool;

    private TokenManager tokenManager;

    private static final String KEY = "key";

    private static final String TOKEN = "token";

    private static final String ANOTHER_TOKEN = "another_token";

    @BeforeClass
    public static void startup() throws IOException {
        jedisPool = new JedisPool("localhost", 6379);
    }

    @Before
    public void init() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.flushAll();
        }
        RedisTokenManager redisTokenManager = new RedisTokenManager();
        redisTokenManager.setJedisPool(jedisPool);
        this.tokenManager = redisTokenManager;
    }

    @Test
    public void testCreate() {
        tokenManager.createRelationship(KEY, TOKEN);

        assertEquals(KEY, tokenManager.getKey(TOKEN));
    }

    @Test
    public void testDelByKey() {
        tokenManager.createRelationship(KEY, TOKEN);

        tokenManager.delRelationshipByKey(KEY);

        assertNull(tokenManager.getKey(TOKEN));
    }

    @Test
    public void testDelByToken() {
        tokenManager.createRelationship(KEY, TOKEN);

        tokenManager.delRelationshipByToken(TOKEN);

        assertNull(tokenManager.getKey(TOKEN));
    }

    @Test
    public void testSingleRelationship() {
        tokenManager.createRelationship(KEY, TOKEN);

        tokenManager.createRelationship(KEY, ANOTHER_TOKEN);

        assertNull(tokenManager.getKey(TOKEN));

        assertEquals(KEY, tokenManager.getKey(ANOTHER_TOKEN));
    }

    @Test
    public void testMultipleRelationship() {
        ((RedisTokenManager) tokenManager).setSingleTokenWithUser(false);

        tokenManager.createRelationship(KEY, TOKEN);

        tokenManager.createRelationship(KEY, ANOTHER_TOKEN);

        assertEquals(KEY, tokenManager.getKey(TOKEN));

        assertEquals(KEY, tokenManager.getKey(ANOTHER_TOKEN));
    }

    @AfterClass
    public static void shutdown() {
        jedisPool.close();
    }


}
