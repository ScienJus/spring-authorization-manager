package com.scienjus.authorization.manager;

/**
 * 对Token进行操作的接口
 * @author ScienJus
 * @date 2015/7/31.
 */
public interface TokenManager {

    void delRelationshipByKey(String key);

    void delRelationshipByToken(String token);

    void createRelationship(String key, String token);

    String getKey(String token);
}
