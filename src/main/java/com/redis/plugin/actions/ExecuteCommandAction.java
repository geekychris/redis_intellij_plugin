package com.redis.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.redis.plugin.model.RedisResult;
import com.redis.plugin.service.RedisConnectionManager;
import com.redis.plugin.service.RedisHistoryService;
import org.jetbrains.annotations.NotNull;

/**
 * Action for executing a Redis command
 */
public class ExecuteCommandAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        
        // Ensure the Redis tool window is open
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Redis Client");
        if (toolWindow != null) {
            toolWindow.show(() -> {
                // Tool window is now visible - focus will be handled by the CommandPanel
            });
        }
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        // Enable only if connected to Redis
        Project project = e.getProject();
        if (project == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        
        RedisConnectionManager connectionManager = ApplicationManager.getApplication()
                .getService(RedisConnectionManager.class);
        e.getPresentation().setEnabled(connectionManager.isConnected());
    }
}
