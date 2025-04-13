package com.redis.plugin.model;

public class RedisResult {
    private RedisResultType type;
    private Object value;
    private String error;
    private long executionTime;

    public RedisResult(RedisResultType type, Object value) {
        this.type = type;
        this.value = value;
    }
    
    public static RedisResult error(String errorMessage) {
        RedisResult result = new RedisResult(RedisResultType.ERROR, null);
        result.error = errorMessage;
        return result;
    }
    
    public static RedisResult success(RedisResultType type, Object value) {
        return new RedisResult(type, value);
    }

    public RedisResultType getType() {
        return type;
    }

    public void setType(RedisResultType type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }
    
    public boolean isError() {
        return type == RedisResultType.ERROR;
    }
    
    @Override
    public String toString() {
        if (isError()) {
            return "Error: " + error;
        }
        return value != null ? value.toString() : "null";
    }
}
