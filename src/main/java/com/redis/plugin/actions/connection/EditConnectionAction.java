package com.redis.plugin.actions.connection;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.redis.plugin.model.RedisConnection;
import com.redis.plugin.service.RedisConnectionManager;
import com.redis.plugin.ui.dialog.ConnectionDialog;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Action for editing a Redis connection
 */
public class EditConnectionAction extends AnAction {

    private final RedisConnection connection;
    
    public EditConnectionAction(RedisConnection connection) {
        super("Edit Connection", "Edit Redis connection", null);
        this.connection = connection;
    }
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null || connection == null) return;
        
        RedisConnectionManager connectionManager = ApplicationManager.getApplication()
                .getService(RedisConnectionManager.class);
        
        // Don't allow editing active connection
        if (connectionManager.isConnected() && 
            Objects.equals(connectionManager.getActiveConnectionId(), connection.getId())) {
            Messages.showWarningDialog(
                    project,
                    "Cannot edit an active connection. Please disconnect first.",
                    "Edit Connection"
            );
            return;
        }
        
        // Open connection dialog
        ConnectionDialog dialog = new ConnectionDialog(project, connection);
        if (dialog.showAndGet()) {
            // Update connection
            RedisConnection updated = dialog.getConnection();
            connectionManager.updateConnection(updated);
        }
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(e.getProject() != null && connection != null);
    }
}

