package com.scienjus.authorization.manager;

import javax.servlet.http.HttpServletRequest;

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

    String decodeToken(String baseToken, HttpServletRequest request);
}
