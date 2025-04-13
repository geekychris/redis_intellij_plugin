package com.redis.plugin.service;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.Topic;
import com.redis.plugin.model.RedisConnection;
import com.redis.plugin.model.RedisResult;
import com.redis.plugin.service.impl.JedisRedisService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing Redis connections
 */
@Service
@State(
    name = "RedisConnections",
    storages = @Storage("redis-connections.xml")
)
public final class RedisConnectionManager implements PersistentStateComponent<RedisConnectionManager.State> {
    private static final Logger LOG = Logger.getInstance(RedisConnectionManager.class);
    
    // Topic for publishing connection events
    public static final Topic<RedisConnectionListener> CONNECTION_TOPIC = Topic.create("Redis Connection Events", RedisConnectionListener.class);
    
    private State state = new State();
    private RedisService redisService;
    
    public RedisConnectionManager() {
        redisService = new JedisRedisService();
    }
    
    @Nullable
    @Override
    public State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }
    
    /**
     * Add a new Redis connection
     * @param connection connection to add
     */
    public void addConnection(RedisConnection connection) {
        state.connections.add(connection);
    }
    
    /**
     * Update an existing connection
     * @param connection connection to update
     */
    public void updateConnection(RedisConnection connection) {
        int index = findConnectionIndex(connection.getId());
        if (index >= 0) {
            state.connections.set(index, connection);
        }
    }
    
    /**
     * Remove a connection
     * @param connectionId ID of the connection to remove
     */
    public void removeConnection(String connectionId) {
        int index = findConnectionIndex(connectionId);
        if (index >= 0) {
            state.connections.remove(index);
        }
    }
    
    /**
     * Get all connections
     * @return list of all Redis connections
     */
    public List<RedisConnection> getConnections() {
        return new ArrayList<>(state.connections);
    }
    
    /**
     * Get a connection by ID
     * @param connectionId connection ID
     * @return the connection or null if not found
     */
    public RedisConnection getConnection(String connectionId) {
        return state.connections.stream()
                .filter(c -> c.getId().equals(connectionId))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Connect to a Redis server
     * @param connectionId ID of the connection to use
     * @return result of the connection attempt
     */
    public RedisResult connect(String connectionId) {
        RedisConnection connection = getConnection(connectionId);
        if (connection == null) {
            return RedisResult.error("Connection not found: " + connectionId);
        }
        
        RedisResult result = redisService.connect(connection);
        if (!result.isError()) {
            state.activeConnectionId = connectionId;
        }
        return result;
    }
    
    /**
     * Disconnect from the current Redis server
     */
    public void disconnect() {
        redisService.disconnect();
        state.activeConnectionId = null;
    }
    
    /**
     * Get the Redis service
     * @return the Redis service
     */
    public RedisService getRedisService() {
        return redisService;
    }
    
    /**
     * Check if currently connected to a Redis server
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return redisService.isConnected();
    }
    
    /**
     * Get the active connection ID
     * @return the active connection ID or null if not connected
     */
    public String getActiveConnectionId() {
        return state.activeConnectionId;
    }
    
    /**
     * Get the active connection
     * @return the active connection or null if not connected
     */
    public RedisConnection getActiveConnection() {
        if (state.activeConnectionId == null) {
            return null;
        }
        return getConnection(state.activeConnectionId);
    }
    
    private int findConnectionIndex(String connectionId) {
        for (int i = 0; i < state.connections.size(); i++) {
            if (state.connections.get(i).getId().equals(connectionId)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * State class for persisting connections
     */
    public static class State {
        public List<RedisConnection> connections = new ArrayList<>();
        public String activeConnectionId;
    }
    
    /**
     * Interface for Redis connection listeners
     */
    public interface RedisConnectionListener {
        void connectionChanged(RedisConnection connection, boolean connected);
    }
}

