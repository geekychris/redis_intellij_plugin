package com.redis.plugin.actions.connection;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
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
        
        ConnectionDialog dialog = new ConnectionDialog(project, null);
        if (dialog.showAndGet()) {
            // Handle new connection
        }
    }
}

