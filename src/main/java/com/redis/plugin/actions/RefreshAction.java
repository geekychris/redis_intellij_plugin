package com.redis.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.redis.plugin.service.RedisConnectionManager;
import org.jetbrains.annotations.NotNull;

/**
 * Action for refreshing Redis connections and data
 */
public class RefreshAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        
        RedisConnectionManager connectionManager = ApplicationManager.getApplication()
                .getService(RedisConnectionManager.class);
        
        // If connected, refresh data by executing a no-op command (PING)
        if (connectionManager.isConnected()) {
            connectionManager.getRedisService().execute("PING");
        }
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        // Always enabled if project is available
        e.getPresentation().setEnabled(e.getProject() != null);
    }
}
