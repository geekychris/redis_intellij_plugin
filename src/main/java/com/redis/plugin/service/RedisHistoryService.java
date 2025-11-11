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

    /**
     * Add a command to the history
     *
     * @param command the command to add
     */
    public void addCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return;
        }

        String trimmedCommand = command.trim();

        // Remove if already exists to avoid duplicates
        state.commandHistory.remove(trimmedCommand);

        // Add to the beginning of the list
        state.commandHistory.add(0, trimmedCommand);

        // Limit history size
        if (state.commandHistory.size() > state.maxSize) {
            state.commandHistory = state.commandHistory.subList(0, state.maxSize);
        }
    }

    /**
     * Get the command history
     *
     * @return list of historical commands
     */
    public List<String> getHistory() {
        return new ArrayList<>(state.commandHistory);
    }

    /**
     * Clear all command history
     */
    public void clearHistory() {
        state.commandHistory.clear();
    }

    /**
     * Set the maximum history size
     *
     * @param maxSize maximum number of commands to keep in history
     */
    public void setMaxSize(int maxSize) {
        state.maxSize = Math.max(1, maxSize);

        // Truncate existing history if needed
        if (state.commandHistory.size() > state.maxSize) {
            state.commandHistory = state.commandHistory.subList(0, state.maxSize);
        }
    }

    /**
     * Get the maximum history size
     *
     * @return maximum history size
     */
    public int getMaxSize() {
        return state.maxSize;
    }

    public static class State {
        public List<String> commandHistory = new ArrayList<>();
        public int maxSize = 100; // Increased default size for console usage
    }
}
