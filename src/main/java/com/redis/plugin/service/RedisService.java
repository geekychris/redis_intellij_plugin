package com.redis.plugin.service;

import com.redis.plugin.model.RedisConnection;
import com.redis.plugin.model.RedisResult;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for Redis operations
 */
public interface RedisService {
    /**
     * Execute a Redis command
     * @param command the command string to execute
     * @return the result of the command execution
     */
    RedisResult execute(String command);
    
    /**
     * Check if service is connected to Redis server
     * @return true if connected, false otherwise
     */
    boolean isConnected();
    
    /**
     * Connect to a Redis server using the specified connection
     * @param connection the connection details
     * @return result of the connection attempt
     */
    RedisResult connect(RedisConnection connection);
    
    /**
     * Disconnect from the current Redis server
     */
    void disconnect();
    
    /**
     * Get keys matching a pattern
     * @param pattern the pattern to match keys against
     * @return list of matching keys
     */
    List<String> getKeys(String pattern);
    
    /**
     * Get string value for a key
     * @param key the key
     * @return the string value
     */
    String getString(String key);
    
    /**
     * Get hash entries for a key
     * @param key the key
     * @return map of field-value pairs
     */
    Map<String, String> getHash(String key);
    
    /**
     * Get list elements for a key
     * @param key the key
     * @return list of elements
     */
    List<String> getList(String key, long start, long end);
    
    /**
     * Get set members for a key
     * @param key the key
     * @return set of members
     */
    Set<String> getSet(String key);
    
    /**
     * Get the type of a key
     * @param key the key
     * @return the type of the key
     */
    String getType(String key);
    
    /**
     * Get database size
     * @return number of keys in the current database
     */
    long getDatabaseSize();
    
    /**
     * Flush current database
     * @return result of the operation
     */
    RedisResult flushDb();
    
    /**
     * Get information about the Redis server
     * @return server information
     */
    RedisResult info();
    
    /**
     * Get the currently connected Redis connection
     * @return the current connection or null if not connected
     */
    RedisConnection getCurrentConnection();
}

