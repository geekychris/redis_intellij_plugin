package com.redis.plugin.service.impl;

import com.intellij.openapi.diagnostic.Logger;
import com.redis.plugin.model.RedisConnection;
import com.redis.plugin.model.RedisResult;
import com.redis.plugin.model.RedisResultType;
import com.redis.plugin.service.RedisService;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of RedisService using Jedis client
 */
public class JedisRedisService implements RedisService {
    private static final Logger LOG = Logger.getInstance(JedisRedisService.class);
    
    private JedisPool jedisPool;
    private RedisConnection currentConnection;
    
    @Override
    public RedisResult execute(String command) {
        if (!isConnected()) {
            return RedisResult.error("Not connected to Redis server");
        }
        
        String[] parts = parseCommandLine(command);
        if (parts.length == 0) {
            return RedisResult.error("Empty command");
        }
        
        long startTime = System.currentTimeMillis();
        try (Jedis jedis = jedisPool.getResource()) {
            Object result = jedis.sendCommand(Protocol.Command.valueOf(parts[0].toUpperCase()),
                    Arrays.copyOfRange(parts, 1, parts.length));
            
            RedisResult redisResult = convertResult(result);
            redisResult.setExecutionTime(System.currentTimeMillis() - startTime);
            return redisResult;
        } catch (IllegalArgumentException e) {
            return RedisResult.error("Unknown command: " + parts[0]);
        } catch (JedisException e) {
            return RedisResult.error("Error executing command: " + e.getMessage());
        }
    }
    
    @Override
    public boolean isConnected() {
        return jedisPool != null && !jedisPool.isClosed();
    }
    
    @Override
    public RedisResult connect(RedisConnection connection) {
        try {
            if (jedisPool != null && !jedisPool.isClosed()) {
                jedisPool.close();
            }
            
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(10);
            poolConfig.setMaxIdle(5);
            poolConfig.setMinIdle(1);
            
            if (connection.getPassword() != null && !connection.getPassword().isEmpty()) {
                jedisPool = new JedisPool(poolConfig, connection.getHost(), connection.getPort(),
                        connection.getTimeout(), connection.getPassword(), connection.isUseSSL());
            } else {
                jedisPool = new JedisPool(poolConfig, connection.getHost(), connection.getPort(),
                        connection.getTimeout(), null, connection.isUseSSL());
            }
            
            // Test connection
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.select(connection.getDatabase());
                jedis.ping();
                this.currentConnection = connection;
                return RedisResult.success(RedisResultType.STATUS, "Connected to " + connection.getName());
            }
        } catch (JedisConnectionException e) {
            if (jedisPool != null) {
                jedisPool.close();
                jedisPool = null;
            }
            currentConnection = null;
            return RedisResult.error("Failed to connect: " + e.getMessage());
        } catch (Exception e) {
            if (jedisPool != null) {
                jedisPool.close();
                jedisPool = null;
            }
            currentConnection = null;
            LOG.error("Error connecting to Redis", e);
            return RedisResult.error("Error: " + e.getMessage());
        }
    }
    
    @Override
    public void disconnect() {
        if (jedisPool != null) {
            jedisPool.close();
            jedisPool = null;
        }
        currentConnection = null;
    }
    
    @Override
    public List<String> getKeys(String pattern) {
        if (!isConnected()) {
            return Collections.emptyList();
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            return new ArrayList<>(jedis.keys(pattern));
        } catch (Exception e) {
            LOG.error("Error getting keys", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public String getString(String key) {
        if (!isConnected()) {
            return null;
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        } catch (Exception e) {
            LOG.error("Error getting string value", e);
            return null;
        }
    }
    
    @Override
    public Map<String, String> getHash(String key) {
        if (!isConnected()) {
            return Collections.emptyMap();
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hgetAll(key);
        } catch (Exception e) {
            LOG.error("Error getting hash", e);
            return Collections.emptyMap();
        }
    }
    
    @Override
    public List<String> getList(String key, long start, long end) {
        if (!isConnected()) {
            return Collections.emptyList();
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.lrange(key, start, end);
        } catch (Exception e) {
            LOG.error("Error getting list", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public Set<String> getSet(String key) {
        if (!isConnected()) {
            return Collections.emptySet();
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.smembers(key);
        } catch (Exception e) {
            LOG.error("Error getting set", e);
            return Collections.emptySet();
        }
    }
    
    @Override
    public String getType(String key) {
        if (!isConnected()) {
            return "none";
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.type(key);
        } catch (Exception e) {
            LOG.error("Error getting type", e);
            return "none";
        }
    }
    
    @Override
    public long getDatabaseSize() {
        if (!isConnected()) {
            return 0;
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.dbSize();
        } catch (Exception e) {
            LOG.error("Error getting database size", e);
            return 0;
        }
    }
    
    @Override
    public RedisResult flushDb() {
        if (!isConnected()) {
            return RedisResult.error("Not connected to Redis server");
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            String result = jedis.flushDB();
            return RedisResult.success(RedisResultType.STATUS, result);
        } catch (Exception e) {
            LOG.error("Error flushing database", e);
            return RedisResult.error("Error: " + e.getMessage());
        }
    }
    
    @Override
    public RedisResult info() {
        if (!isConnected()) {
            return RedisResult.error("Not connected to Redis server");
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            String info = jedis.info();
            return RedisResult.success(RedisResultType.STRING, info);
        } catch (Exception e) {
            LOG.error("Error getting server info", e);
            return RedisResult.error("Error: " + e.getMessage());
        }
    }
    
    @Override
    public RedisConnection getCurrentConnection() {
        return currentConnection;
    }
    
    private String[] parseCommandLine(String command) {
        List<String> parts = new ArrayList<>();
        StringBuilder currentPart = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < command.length(); i++) {
            char c = command.charAt(i);
            
            if (c == '"' && (i == 0 || command.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            } else if (Character.isWhitespace(c) && !inQuotes) {
                if (currentPart.length() > 0) {
                    parts.add(currentPart.toString());
                    currentPart = new StringBuilder();
                }
            } else {
                currentPart.append(c);
            }
        }
        
        if (currentPart.length() > 0) {
            parts.add(currentPart.toString());
        }
        
        return parts.toArray(new String[0]);
    }
    
    private RedisResult convertResult(Object result) {
        if (result == null) {
            return RedisResult.success(RedisResultType.NIL, null);
        }
        
        if (result instanceof String) {
            return RedisResult.success(RedisResultType.STRING, result);
        } else if (result instanceof Long) {
            return RedisResult.success(RedisResultType.INTEGER, result);
        } else if (result instanceof List) {
            return RedisResult.success(RedisResultType.ARRAY, result);
        } else if (result instanceof Set) {
            return RedisResult.success(RedisResultType.SET, result);
        } else if (result instanceof Map) {
            return RedisResult.success(RedisResultType.HASH, result);
        } else {
            return RedisResult.success(RedisResultType.STRING, result.toString());
        }
    }
}

