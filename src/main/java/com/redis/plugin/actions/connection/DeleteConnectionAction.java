package com.redis.plugin.actions.connection;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.redis.plugin.model.RedisConnection;
import com.redis.plugin.service.RedisConnectionManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Action for deleting a Redis connection
 */
public class DeleteConnectionAction extends AnAction {

    private final RedisConnection connection;
    
    public DeleteConnectionAction(RedisConnection connection) {
        super("Delete Connection", "Delete Redis connection", null);
        this.connection = connection;
    }
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null || connection == null) return;
        
        RedisConnectionManager connectionManager = ApplicationManager.getApplication()
                .getService(RedisConnectionManager.class);
        
        // Don't allow removing active connection
        if (connectionManager.isConnected() && 
            Objects.equals(connectionManager.getActiveConnectionId(), connection.getId())) {
            Messages.showWarningDialog(
                    project,
                    "Cannot remove an active connection. Please disconnect first.",
                    "Remove Connection"
            );
            return;
        }
        
        // Confirm deletion
        int result = Messages.showYesNoDialog(
                project,
                "Are you sure you want to remove the connection '" + connection.getName() + "'?",
                "Remove Connection",
                Messages.getQuestionIcon()
        );
        
        if (result == Messages.YES) {
            // Remove connection
            connectionManager.removeConnection(connection.getId());
        }
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(e.getProject() != null && connection != null);
    }
}

