package com.redis.plugin.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a Redis server connection with configuration details
 */
public class RedisConnection {
    private String id;
    private String name;
    private String host;
    private int port;
    private String password;
    private boolean useSSL;
    private int database;
    private int timeout;
    private boolean connected;

    /**
     * Default constructor creates a new connection with default values
     */
    public RedisConnection() {
        this.id = UUID.randomUUID().toString();
        this.port = 6379; // Default Redis port
        this.database = 0;
        this.timeout = 5000; // 5 seconds default
        this.connected = false;
    }

    /**
     * Constructor for creating a new connection with all parameters
     */
    public RedisConnection(String name, String host, int port, String password, boolean useSSL, int database, int timeout) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.host = host;
        this.port = port;
        this.password = password;
        this.useSSL = useSSL;
        this.database = database;
        this.timeout = timeout;
        this.connected = false;
    }

    /**
     * Creates a new RedisConnection builder
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a builder initialized with this connection's values
     * @return a new builder instance with current values
     */
    public Builder toBuilder() {
        return new Builder()
                .id(this.id)
                .name(this.name)
                .host(this.host)
                .port(this.port)
                .password(this.password)
                .useSSL(this.useSSL)
                .database(this.database)
                .timeout(this.timeout)
                .connected(this.connected);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isUseSSL() {
        return useSSL;
    }

    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    @Override
    public String toString() {
        return name + " (" + host + ":" + port + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RedisConnection that = (RedisConnection) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Builder for RedisConnection
     */
    public static class Builder {
        private String id;
        private String name;
        private String host;
        private int port = 6379;
        private String password;
        private boolean useSSL = false;
        private int database = 0;
        private int timeout = 5000;
        private boolean connected = false;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder useSSL(boolean useSSL) {
            this.useSSL = useSSL;
            return this;
        }

        public Builder database(int database) {
            this.database = database;
            return this;
        }

        public Builder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder connected(boolean connected) {
            this.connected = connected;
            return this;
        }

        public RedisConnection build() {
            RedisConnection connection = new RedisConnection();
            if (id != null) {
                connection.id = id;
            }
            connection.name = name;
            connection.host = host;
            connection.port = port;
            connection.password = password;
            connection.useSSL = useSSL;
            connection.database = database;
            connection.timeout = timeout;
            connection.connected = connected;
            return connection;
        }
    }
}
