package com.scienjus.authorization.manager.impl;

import com.scienjus.authorization.manager.TokenManager;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author XieEnlong
 * @date 2015/10/27.
 */
public class DBTokenManager implements TokenManager {

    private JdbcTemplate jdbcTemplate;

    private String tableName;
    private String keyColumnName;
    private String tokenColumnName;
    private String expireAtColumnName;
    private long tokenExpireSeconds;

    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
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

    public void setTokenExpireSeconds(long tokenExpireSeconds) {
        this.tokenExpireSeconds = tokenExpireSeconds;
    }

    @Override
    public void delRelationshipByKey(String key) {
        String sql = String.format("delete from %s where %s = ?", tableName, keyColumnName);
        jdbcTemplate.update(sql, new Object[]{key});
    }

    @Override
    public void delRelationshipByToken(String token) {
        String sql = String.format("delete from %s where %s = ?", tableName, tokenColumnName);
        jdbcTemplate.update(sql, new Object[]{token});
    }

    @Override
    public void createRelationship(String key, String token) {
        String select = String.format("select count(*) from %s where %s = ?", tableName, keyColumnName);
        int count = jdbcTemplate.queryForObject(select, new Object[]{key}, Integer.class);
        if (count > 0) {
            String sql = String.format("update %s set %s = ?, %s = ? where %s = ?", tableName, tokenColumnName, expireAtColumnName, keyColumnName);
            jdbcTemplate.update(sql, new Object[]{token, new Timestamp(System.currentTimeMillis() + tokenExpireSeconds * 1000), key});
        } else {
            String sql = String.format("insert into %s (%s, %s, %s) values(?, ?, ?)", tableName, keyColumnName, tokenColumnName, expireAtColumnName);
            jdbcTemplate.update(sql, new Object[]{key, token, new Timestamp(System.currentTimeMillis() + tokenExpireSeconds * 1000)});
        }
    }

    @Override
    public String getKey(String token) {
        String sql = String.format("select %s from %s where %s = ? and %s > ? limit 1", keyColumnName, tableName, tokenColumnName, expireAtColumnName);
        List<String> keys = jdbcTemplate.queryForList(sql, new Object[]{token, new Timestamp(System.currentTimeMillis())}, String.class);
        if (keys.size() > 0) {
            return keys.get(0);
        }
        return null;
    }
}
