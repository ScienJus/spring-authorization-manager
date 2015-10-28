package com.scienjus.authorization.manager.impl;

import com.scienjus.authorization.manager.TokenManager;

import javax.servlet.http.HttpServletRequest;

/**
 * @author XieEnlong
 * @date 2015/10/27.
 */
public abstract class AbstractTokenManager implements TokenManager {

    protected long tokenExpireSeconds = 3600;

    protected boolean singleSignOn = true;

    protected boolean flushExpireAfterOperate = true;

    public void setTokenExpireSeconds(long tokenExpireSeconds) {
        this.tokenExpireSeconds = tokenExpireSeconds;
    }

    public void setSingleSignOn(boolean singleSignOn) {
        this.singleSignOn = singleSignOn;
    }

    public void setFlushExpireAfterOperate(boolean flushExpireAfterOperate) {
        this.flushExpireAfterOperate = flushExpireAfterOperate;
    }

    @Override
    public String decodeToken(String baseToken, HttpServletRequest request) {
        return baseToken;
    }
}
