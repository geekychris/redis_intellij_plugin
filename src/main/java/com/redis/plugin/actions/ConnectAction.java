package com.redis.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.redis.plugin.model.RedisConnection;
import com.redis.plugin.service.RedisConnectionManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Action for connecting to Redis server
 */
public class ConnectAction extends AnAction {

    private final RedisConnection connection;
    
    /**
     * Default constructor for plugin.xml registration
     */
    public ConnectAction() {
        this.connection = null;
    }
    
    /**
     * Constructor with specific connection
     * @param connection the connection to use
     */
    public ConnectAction(RedisConnection connection) {
        super("Connect", "Connect to Redis server", null);
        this.connection = connection;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        
        RedisConnectionManager connectionManager = ApplicationManager.getApplication()
                .getService(RedisConnectionManager.class);
        
        String connectionId;
        if (connection != null) {
            // Use the provided connection
            connectionId = connection.getId();
        } else {
            // No connection provided, get available connections
            List<RedisConnection> connections = connectionManager.getConnections();
            if (connections.isEmpty()) {
                Messages.showWarningDialog(
                        project,
                        "No connections available. Please create a connection first.",
                        "Connect to Redis"
                );
                return;
            }
            
            // If there's only one connection, use it
            // Otherwise, this action would need context from the UI tree selection
            if (connections.size() == 1) {
                connectionId = connections.get(0).getId();
            } else {
                // Multiple connections available, need UI context
                Messages.showInfoMessage(
                        project,
                        "Please select a connection from the Redis Client tool window.",
                        "Connect to Redis"
                );
                return;
            }
        }
        
        // Connect to Redis
        connectionManager.connect(connectionId);
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        // Enable only if not connected
        Project project = e.getProject();
        if (project == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        
        RedisConnectionManager connectionManager = ApplicationManager.getApplication()
                .getService(RedisConnectionManager.class);
        
        e.getPresentation().setEnabled(!connectionManager.isConnected());
    }
}
