package com.redis.plugin.model;

/**
 * Represents the time complexity of a Redis command
 */
public enum RedisCommandComplexity {
    O1("O(1)", "Constant time complexity"),
    OLOG_N("O(log N)", "Logarithmic time complexity"),
    ON("O(N)", "Linear time complexity"),
    OM_PLUS_N("O(M+N)", "Linear time complexity where M and N are inputs"),
    ON_SQUARE("O(N²)", "Quadratic time complexity");

    private final String notation;
    private final String description;

    RedisCommandComplexity(String notation, String description) {
        this.notation = notation;
        this.description = description;
    }

    /**
     * Get the complexity notation
     * 
     * @return the notation (e.g., "O(1)")
     */
    public String getNotation() {
        return notation;
    }

    /**
     * Get the complexity description
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return notation + " - " + description;
    }
}
