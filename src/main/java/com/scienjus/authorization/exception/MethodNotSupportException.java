package com.scienjus.authorization.exception;

/**
 * 方法不支持当前环境的异常，主要跟一些配置参数有关
 * @author ScienJus
 * @date 2015/10/28.
 */
public class MethodNotSupportException extends RuntimeException {
    public MethodNotSupportException(String message) {
        super(message);
    }
}
