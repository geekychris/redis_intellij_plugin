package com.redis.plugin.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a category of Redis commands
 */
public class RedisCommandCategory {
    private final String name;
    private final String description;
    private final List<RedisCommand> commands;

    /**
     * Create a new command category
     * 
     * @param name the category name
     * @param description the category description
     */
    public RedisCommandCategory(String name, String description) {
        this.name = name;
        this.description = description;
        this.commands = new ArrayList<>();
    }

    /**
     * Get the category name
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the category description
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the commands in this category
     * 
     * @return an unmodifiable list of commands
     */
    public List<RedisCommand> getCommands() {
        return Collections.unmodifiableList(commands);
    }

    /**
     * Add a command to this category
     * 
     * @param command the command to add
     */
    public void addCommand(RedisCommand command) {
        this.commands.add(command);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RedisCommandCategory that = (RedisCommandCategory) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
