package com.scienjus.authorization.repository;

/**
 * 通过Key获得用户模型的接口
 * @author ScienJus
 * @date 2015/10/26.
 */
public interface UserModelRepository {

    /**
     * 通过Key获得用户模型
     * @param key
     * @return
     */
    Object getCurrentUser(String key);
}
