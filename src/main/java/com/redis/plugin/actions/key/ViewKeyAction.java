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
 * Action for viewing a Redis key
 */
public class ViewKeyAction extends AnAction {

    private final String key;
    
    /**
     * Default constructor for plugin.xml registration
     */
    public ViewKeyAction() {
        this.key = null;
    }
    
    /**
     * Constructor with specific key
     * @param key the key to view
     */
    public ViewKeyAction(String key) {
        super("View Key", "View Redis key value", null);
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
                    "View Key"
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
        
        // Get key type
        RedisResult typeResult = connectionManager.getRedisService().execute("TYPE " + key);
        if (typeResult.isError() || typeResult.getValue() == null) {
            Messages.showErrorDialog(
                    project,
                    "Error getting key type: " + (typeResult.isError() ? typeResult.getError() : "Unknown error"),
                    "Error"
            );
            return;
        }
        
        String type = typeResult.getValue().toString();
        RedisResult valueResult;
        
        // Get key value based on type
        switch (type.toLowerCase()) {
            case "string":
                valueResult = connectionManager.getRedisService().execute("GET " + key);
                break;
            case "list":
                valueResult = connectionManager.getRedisService().execute("LRANGE " + key + " 0 -1");
                break;
            case "set":
                valueResult = connectionManager.getRedisService().execute("SMEMBERS " + key);
                break;
            case "zset":
                valueResult = connectionManager.getRedisService().execute("ZRANGE " + key + " 0 -1 WITHSCORES");
                break;
            case "hash":
                valueResult = connectionManager.getRedisService().execute("HGETALL " + key);
                break;
            default:
                Messages.showErrorDialog(
                        project,
                        "Unsupported key type: " + type,
                        "Error"
                );
                return;
        }
        
        if (valueResult.isError()) {
            Messages.showErrorDialog(
                    project,
                    "Error getting key value: " + valueResult.getError(),
                    "Error"
            );
            return;
        }
        
        // Display value in a dialog
        Messages.showInfoMessage(
                project,
                valueResult.getValue().toString(),
                "Key: " + key + " (Type: " + type + ")"
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
