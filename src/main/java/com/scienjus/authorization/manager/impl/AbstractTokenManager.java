package com.scienjus.authorization.manager.impl;

import com.scienjus.authorization.manager.TokenManager;

/**
 * @author XieEnlong
 * @date 2015/10/27.
 */
public abstract class AbstractTokenManager implements TokenManager {

    protected long tokenExpireSeconds;

    public void setTokenExpireSeconds(long tokenExpireSeconds) {
        this.tokenExpireSeconds = tokenExpireSeconds;
    }

    @Override
    public String decodeToken(String baseToken) {
        return baseToken;
    }
}
