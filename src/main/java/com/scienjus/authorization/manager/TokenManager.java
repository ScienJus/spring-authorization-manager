package com.scienjus.authorization.manager;

import javax.servlet.http.HttpServletRequest;

/**
 * 对Token进行管理的接口
 * @author ScienJus
 * @date 2015/7/31.
 */
public interface TokenManager {

    /**
     * 通过key删除关联关系
     * @param key
     */
    void delRelationshipByKey(String key);

    /**
     * 通过token删除关联关系
     * @param token
     */
    void delRelationshipByToken(String token);

    /**
     * 创建关联关系
     * @param key
     * @param token
     */
    void createRelationship(String key, String token);

    /**
     * 通过token获得对应的key
     * @param token
     * @return
     */
    String getKey(String token);

    /**
     * 对原始token进行解密、解码、时间戳验证、url签名等一系列操作的扩展接口
     * @param baseToken 原始token
     * @param request   该次请求的request对象
     * @return          存放映射关系的token
     */
    String decodeToken(String baseToken, HttpServletRequest request);
}
