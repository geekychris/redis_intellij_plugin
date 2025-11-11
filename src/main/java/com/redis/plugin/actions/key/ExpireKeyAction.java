package com.redis.plugin.actions.key;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.redis.plugin.model.RedisResult;
import com.redis.plugin.service.RedisConnectionManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Action for setting expiration on a Redis key
 */
public class ExpireKeyAction extends AnAction {

    private final String key;
    
    /**
     * Default constructor for plugin.xml registration
     */
    public ExpireKeyAction() {
        this.key = null;
    }
    
    /**
     * Constructor with specific key
     * @param key the key to set expiration for
     */
    public ExpireKeyAction(String key) {
        super("Set Expiration", "Set expiration time for Redis key", null);
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
                    "Set Expiration"
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
        
        // Show expire dialog
        ExpireDialog dialog = new ExpireDialog(project, key);
        if (dialog.showAndGet()) {
            int seconds = dialog.getSeconds();
            
            // Set expiration
            RedisResult result;
            if (seconds > 0) {
                result = connectionManager.getRedisService().execute("EXPIRE " + key + " " + seconds);
            } else {
                // Remove expiration
                result = connectionManager.getRedisService().execute("PERSIST " + key);
            }
            
            if (result.isError()) {
                Messages.showErrorDialog(
                        project,
                        "Error setting expiration: " + result.getError(),
                        "Error"
                );
                return;
            }
            
            // Show success message
            if (seconds > 0) {
                Messages.showInfoMessage(
                        project,
                        "Expiration set for key '" + key + "' (" + seconds + " seconds)",
                        "Success"
                );
            } else {
                Messages.showInfoMessage(
                        project,
                        "Expiration removed for key '" + key + "'",
                        "Success"
                );
            }
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
    
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
    
    /**
     * Dialog for setting expiration time
     */
    private static class ExpireDialog extends DialogWrapper {
        private final JBTextField secondsField;
        private final String key;
        
        public ExpireDialog(@Nullable Project project, String key) {
            super(project);
            this.key = key;
            
            setTitle("Set Expiration for Key '" + key + "'");
            
            secondsField = new JBTextField("3600");
            secondsField.setToolTipText("Enter 0 to remove expiration");
            
            init();
        }
        
        @Override
        protected @Nullable ValidationInfo doValidate() {
            try {
                int seconds = Integer.parseInt(secondsField.getText().trim());
                if (seconds < 0) {
                    return new ValidationInfo("Seconds must be greater than or equal to 0", secondsField);
                }
            } catch (NumberFormatException e) {
                return new ValidationInfo("Seconds must be a number", secondsField);
            }
            return null;
        }
        
        @Override
        protected @Nullable JComponent createCenterPanel() {
            JPanel panel = FormBuilder.createFormBuilder()
                    .addLabeledComponent(new JBLabel("Seconds (0 to remove expiration):"), secondsField)
                    .getPanel();
            
            panel.setPreferredSize(new Dimension(300, 80));
            return panel;
        }
        
        public int getSeconds() {
            try {
                return Integer.parseInt(secondsField.getText().trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }
}
