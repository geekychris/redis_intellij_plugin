package com.redis.plugin.model;

/**
 * Enum representing different types of Redis operation results
 */
public enum RedisResultType {
    STRING("String"),
    INTEGER("Integer"),
    ARRAY("Array"),
    HASH("Hash"),
    SET("Set"),
    ZSET("Sorted Set"),
    BOOLEAN("Boolean"),
    NIL("Nil"),
    ERROR("Error"),
    STATUS("Status");

    private final String displayName;

    RedisResultType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

