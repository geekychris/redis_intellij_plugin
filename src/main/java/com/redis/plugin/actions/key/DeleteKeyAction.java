package com.redis.plugin.actions.key;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.redis.plugin.model.RedisResult;
import com.redis.plugin.service.RedisConnectionManager;
import org.jetbrains.annotations.NotNull;

/**
 * Action for deleting a Redis key
 */
public class DeleteKeyAction extends AnAction {

    private final String key;

    /**
     * Default constructor for plugin.xml registration
     */
    public DeleteKeyAction() {
        this.key = null;
    }

    /**
     * Constructor with specific key
     * @param key the key to delete
     */
    public DeleteKeyAction(String key) {
        super("Delete Key", "Delete Redis key", null);
        this.key = key;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        if (key == null) {
            Messages.showWarningDialog(
                    project,
                    "Please select a key from the Redis Client tool window.",
                    "Delete Key"
            );
            return;
        }

        RedisConnectionManager connectionManager = ApplicationManager.getApplication()
                .getService(RedisConnectionManager.class);

        if (!connectionManager.isConnected()) {
            Messages.showErrorDialog(
                    project,
                    "Not connected to Redis server",
                    "Error"
            );
            return;
        }

        // Confirm deletion
        int result = Messages.showYesNoDialog(
                project,
                "Are you sure you want to delete the key '" + key + "'?",
                "Delete Key",
                Messages.getQuestionIcon()
        );

        if (result != Messages.YES) return;

        // Delete key
        RedisResult deleteResult = connectionManager.getRedisService().execute("DEL " + key);

        if (deleteResult.isError()) {
            Messages.showErrorDialog(
                    project,
                    "Error deleting key: " + deleteResult.getError(),
                    "Error"
            );
            return;
        }

        // Show success message
        Messages.showInfoMessage(
                project,
                "Key '" + key + "' deleted successfully.",
                "Success"
        );
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null || key == null) {
            e.getPresentation().setEnabled(false);
            return;
        }

        RedisConnectionManager connectionManager = ApplicationManager.getApplication()
                .getService(RedisConnectionManager.class);

        e.getPresentation().setEnabled(connectionManager.isConnected());
    }
}