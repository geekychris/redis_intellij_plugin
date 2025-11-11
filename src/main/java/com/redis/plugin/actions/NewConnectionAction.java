package com.redis.plugin.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.redis.plugin.model.RedisConnection;
import com.redis.plugin.service.RedisConnectionManager;
import com.redis.plugin.ui.dialog.ConnectionDialog;
import org.jetbrains.annotations.NotNull;

/**
 * Action for creating a new Redis connection
 */
public class NewConnectionAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        
        // Open connection dialog
        ConnectionDialog dialog = new ConnectionDialog(project, null);
        if (dialog.showAndGet()) {
            // Get the new connection and add it to the connection manager
            RedisConnection connection = dialog.getConnection();
            RedisConnectionManager connectionManager = ApplicationManager.getApplication()
                    .getService(RedisConnectionManager.class);
            connectionManager.addConnection(connection);
        }
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        // Always enabled if project is available
        e.getPresentation().setEnabled(e.getProject() != null);
    }
    
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}

