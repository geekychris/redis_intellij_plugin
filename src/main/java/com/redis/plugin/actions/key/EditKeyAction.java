package com.redis.plugin.actions.key;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.redis.plugin.model.RedisResult;
import com.redis.plugin.service.RedisConnectionManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Action for editing a Redis key value
 */
public class EditKeyAction extends AnAction {

    private final String key;
    
    /**
     * Default constructor for plugin.xml registration
     */
    public EditKeyAction() {
        this.key = null;
    }
    
    /**
     * Constructor with specific key
     * @param key the key to edit
     */
    public EditKeyAction(String key) {
        super("Edit Key", "Edit Redis key value", null);
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
                    "Edit Key"
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
        
        // Currently only support editing string values
        if (!type.equalsIgnoreCase("string")) {
            Messages.showErrorDialog(
                    project,
                    "Editing is currently only supported for String values. Key type is: " + type,
                    "Unsupported Operation"
            );
            return;
        }
        
        // Get current value
        RedisResult valueResult = connectionManager.getRedisService().execute("GET " + key);
        if (valueResult.isError()) {
            Messages.showErrorDialog(
                    project,
                    "Error getting key value: " + valueResult.getError(),
                    "Error"
            );
            return;
        }
        
        String currentValue = valueResult.getValue() != null ? valueResult.getValue().toString() : "";
        
        // Show edit dialog
        EditValueDialog dialog = new EditValueDialog(project, key, currentValue);
        if (dialog.showAndGet()) {
            String newValue = dialog.getValue();
            
            // Set new value
            RedisResult setResult = connectionManager.getRedisService().execute("SET " + key + " \"" + newValue + "\"");
            
            if (setResult.isError()) {
                Messages.showErrorDialog(
                        project,
                        "Error updating key value: " + setResult.getError(),
                        "Error"
                );
                return;
            }
            
            // Show success message
            Messages.showInfoMessage(
                    project,
                    "Key '" + key + "' updated successfully",
                    "Success"
            );
        }
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
    
    /**
     * Dialog for editing key value
     */
    private static class EditValueDialog extends DialogWrapper {
        private final JBTextArea valueField;
        private final String key;
        
        public EditValueDialog(@Nullable Project project, String key, String currentValue) {
            super(project);
            this.key = key;
            
            setTitle("Edit Key '" + key + "'");
            
            valueField = new JBTextArea(currentValue);
            valueField.setRows(10);
            valueField.setLineWrap(true);
            valueField.setWrapStyleWord(true);
            
            init();
        }
        
        @Override
        protected @Nullable JComponent createCenterPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JBLabel("Value:"), BorderLayout.NORTH);
            panel.add(new JBScrollPane(valueField), BorderLayout.CENTER);
            
            panel.setPreferredSize(new Dimension(500, 300));
            return panel;
        }
        
        public String getValue() {
            return valueField.getText();
        }
    }
    
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
