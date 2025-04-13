package com.redis.plugin.service;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing Redis command history
 */
@Service
@State(
    name = "RedisCommandHistory",
    storages = @Storage("redis-command-history.xml")
)
public final class RedisHistoryService implements PersistentStateComponent<RedisHistoryService.State> {
    private State state = new State();
    
    public RedisHistoryService(Project project) {
        // Initialize service
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
    
    public void addCommand(String command) {
        // Add command to history
    }
    
    public List<String> getHistory() {
        return state.commandHistory;
    }
    
    public static class State {
        public List<String> commandHistory = new ArrayList<>();
        public int maxSize = 50;
    }
}

