package com.redis.plugin.actions.connection;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.redis.plugin.model.RedisConnection;
import com.redis.plugin.model.RedisResult;
import com.redis.plugin.service.RedisConnectionManager;
import org.jetbrains.annotations.NotNull;

/**
 * Action for connecting to Redis server
 */
public class ConnectAction extends AnAction {

    private final RedisConnection connection;
    
    public ConnectAction(RedisConnection connection) {
        super("Connect", "Connect to Redis server", null);
        this.connection = connection;
    }
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null || connection == null) return;
        
        RedisConnectionManager connectionManager = ApplicationManager.getApplication()
                .getService(RedisConnectionManager.class);
        
        // Connect to Redis
        RedisResult result = connectionManager.connect(connection.getId());
        
        // Show error if connection failed
        if (result.isError()) {
            Messages.showErrorDialog(
                    project,
                    "Failed to connect to Redis: " + result.getError(),
                    "Connection Error"
            );
        }
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        // Enable only if not already connected
        Project project = e.getProject();
        if (project == null || connection == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        
        RedisConnectionManager connectionManager = ApplicationManager.getApplication()
                .getService(RedisConnectionManager.class);
        
        e.getPresentation().setEnabled(!connectionManager.isConnected());
    }
}
