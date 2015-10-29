package com.scienjus.authorization.manager.impl;

import com.scienjus.authorization.exception.MethodNotSupportException;

import javax.sql.DataSource;
import java.sql.*;

/**
 * @author XieEnlong
 * @date 2015/10/27.
 */
public class DBTokenManager extends AbstractTokenManager {

    protected DataSource dataSource;

    protected String tableName;
    protected String keyColumnName;
    protected String tokenColumnName;
    protected String expireAtColumnName;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setKeyColumnName(String keyColumnName) {
        this.keyColumnName = keyColumnName;
    }

    public void setTokenColumnName(String tokenColumnName) {
        this.tokenColumnName = tokenColumnName;
    }

    public void setExpireAtColumnName(String expireAtColumnName) {
        this.expireAtColumnName = expireAtColumnName;
    }

    @Override
    public void delRelationshipByKey(String key) {
        if (!singleSignOn) {
            throw new MethodNotSupportException("非单点登录时无法调用该方法");
        }
        String sql = String.format("delete from %s where %s = ?", tableName, keyColumnName);
        update(sql, key);
    }

    @Override
    public void delRelationshipByToken(String token) {
        String sql = String.format("delete from %s where %s = ?", tableName, tokenColumnName);
        update(sql, token);
    }

    @Override
    public void createRelationship(String key, String token) {
        if (!singleSignOn) {
            String sql = String.format("insert into %s (%s, %s, %s) values(?, ?, ?)", tableName, keyColumnName, tokenColumnName, expireAtColumnName);
            update(sql, key, token, new Timestamp(System.currentTimeMillis() + tokenExpireSeconds * 1000));
        } else {
            String select = String.format("select count(*) from %s where %s = ?", tableName, keyColumnName);
            Number count = query(Number.class, select, key);
            if (count.intValue() > 0) {
                String sql = String.format("update %s set %s = ?, %s = ? where %s = ?", tableName, tokenColumnName, expireAtColumnName, keyColumnName);
                update(sql, token, new Timestamp(System.currentTimeMillis() + tokenExpireSeconds * 1000), key);
            } else {
                String sql = String.format("insert into %s (%s, %s, %s) values(?, ?, ?)", tableName, keyColumnName, tokenColumnName, expireAtColumnName);
                update(sql, key, token, new Timestamp(System.currentTimeMillis() + tokenExpireSeconds * 1000));
            }
        }
    }

    @Override
    public String getKey(String token) {
        String sql = String.format("select %s from %s where %s = ? and %s > ? limit 1", keyColumnName, tableName, tokenColumnName, expireAtColumnName);
        String key = query(String.class, sql, token, new Timestamp(System.currentTimeMillis()));
        if (key != null) {
            if (flushExpireAfterOperate) {
                String flushExpireAtSql = String.format("update %s set %s = ? where %s = ?", tableName, expireAtColumnName, tokenColumnName);
                update(flushExpireAtSql, new Timestamp(System.currentTimeMillis() + tokenExpireSeconds * 1000), token);
            }
            return key;
        }
        return null;
    }

    private void update(String sql, Object... args) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            int i = 1;
            for (Object arg : args) {
                statement.setObject(i++, arg);
            }
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private <T> T query(Class<T> clazz, String sql, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int i = 1;
            for (Object arg : args) {
                statement.setObject(i++, arg);
            }
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Object obj = resultSet.getObject(1);
                if (clazz.isInstance(obj)) {
                    return (T)obj;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
