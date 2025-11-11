package com.redis.plugin.actions.connection;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.redis.plugin.service.RedisConnectionManager;
import org.jetbrains.annotations.NotNull;

/**
 * Action for disconnecting from Redis server
 */
public class DisconnectAction extends AnAction {

    public DisconnectAction  ()
    {
        System.out.println();
    }
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        
        RedisConnectionManager connectionManager = ApplicationManager.getApplication()
                .getService(RedisConnectionManager.class);
        
        // Disconnect from Redis
        connectionManager.disconnect();
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        // Enable only if connected
        Project project = e.getProject();
        if (project == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        
        RedisConnectionManager connectionManager = ApplicationManager.getApplication()
                .getService(RedisConnectionManager.class);
        
        e.getPresentation().setEnabled(connectionManager.isConnected());
    }
    
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
