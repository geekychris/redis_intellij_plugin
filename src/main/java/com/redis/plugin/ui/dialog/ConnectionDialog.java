package com.redis.plugin.ui.dialog;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.redis.plugin.model.RedisConnection;
import com.redis.plugin.model.RedisResult;
import com.redis.plugin.service.RedisConnectionManager;
import com.redis.plugin.service.RedisService;
import com.redis.plugin.service.impl.JedisRedisService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog for adding or editing Redis connections
 */
public class ConnectionDialog extends DialogWrapper {
    private final Project project;
    private final JBTextField nameField;
    private final JBTextField hostField;
    private final JBTextField portField;
    private final JBPasswordField passwordField;
    private final JCheckBox useSslCheckbox;
    private final JBTextField databaseField;
    private final JBTextField timeoutField;
    private final RedisConnection originalConnection;
    private final JButton testButton;
    
    public ConnectionDialog(@Nullable Project project, @Nullable RedisConnection connection) {
        super(project);
        this.project = project;
        
        // Set dialog title based on whether we're adding or editing
        setTitle(connection == null ? "Add Redis Connection" : "Edit Redis Connection");
        
        // Create form fields
        nameField = new JBTextField();
        hostField = new JBTextField();
        portField = new JBTextField();
        passwordField = new JBPasswordField();
        useSslCheckbox = new JCheckBox("Use SSL/TLS");
        databaseField = new JBTextField();
        timeoutField = new JBTextField();
        
        // Set initial values
        originalConnection = connection;
        if (connection != null) {
            nameField.setText(connection.getName());
            hostField.setText(connection.getHost());
            portField.setText(String.valueOf(connection.getPort()));
            passwordField.setText(connection.getPassword());
            useSslCheckbox.setSelected(connection.isUseSSL());
            databaseField.setText(String.valueOf(connection.getDatabase()));
            timeoutField.setText(String.valueOf(connection.getTimeout()));
        } else {
            // Default values
            portField.setText("6379");
            databaseField.setText("0");
            timeoutField.setText("5000");
        }
        
        // Create test button
        testButton = new JButton("Test Connection");
        testButton.addActionListener(e -> testConnection());
        
        // Initialize dialog
        init();
    }
    
    /**
     * Creates the panel with form fields
     */
    @Override
    protected @Nullable JComponent createCenterPanel() {
        // Create labels
        JBLabel nameLabel = new JBLabel("Name:");
        JBLabel hostLabel = new JBLabel("Host:");
        JBLabel portLabel = new JBLabel("Port:");
        JBLabel passwordLabel = new JBLabel("Password:");
        JBLabel databaseLabel = new JBLabel("Database:");
        JBLabel timeoutLabel = new JBLabel("Timeout (ms):");
        
        // Build form using FormBuilder
        JPanel formPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(nameLabel, nameField)
                .addLabeledComponent(hostLabel, hostField)
                .addLabeledComponent(portLabel, portField)
                .addLabeledComponent(passwordLabel, passwordField)
                .addComponent(useSslCheckbox)
                .addLabeledComponent(databaseLabel, databaseField)
                .addLabeledComponent(timeoutLabel, timeoutField)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
        
        // Create test button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(testButton);
        
        // Add both panels to main panel
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Set size and padding
        panel.setPreferredSize(new Dimension(400, 300));
        panel.setBorder(JBUI.Borders.empty(10));
        
        return panel;
    }
    
    /**
     * Validates form inputs
     */
    @Override
    protected @Nullable ValidationInfo doValidate() {
        // Validate name (required)
        if (nameField.getText().trim().isEmpty()) {
            return new ValidationInfo("Name cannot be empty", nameField);
        }
        
        // Validate host (required)
        if (hostField.getText().trim().isEmpty()) {
            return new ValidationInfo("Host cannot be empty", hostField);
        }
        
        // Validate port (must be a number between 1-65535)
        try {
            int port = Integer.parseInt(portField.getText().trim());
            if (port < 1 || port > 65535) {
                return new ValidationInfo("Port must be between 1 and 65535", portField);
            }
        } catch (NumberFormatException e) {
            return new ValidationInfo("Port must be a valid number", portField);
        }
        
        // Validate database (must be a non-negative number)
        try {
            int db = Integer.parseInt(databaseField.getText().trim());
            if (db < 0) {
                return new ValidationInfo("Database must be a non-negative number", databaseField);
            }
        } catch (NumberFormatException e) {
            return new ValidationInfo("Database must be a valid number", databaseField);
        }
        
        // Validate timeout (must be a positive number)
        try {
            int timeout = Integer.parseInt(timeoutField.getText().trim());
            if (timeout <= 0) {
                return new ValidationInfo("Timeout must be greater than 0", timeoutField);
            }
        } catch (NumberFormatException e) {
            return new ValidationInfo("Timeout must be a valid number", timeoutField);
        }
        
        return null;
    }
    
    /**
     * Tests the connection to the Redis server
     */
    private void testConnection() {
        // Create a temporary connection with current form values
        RedisConnection testConnection = createConnectionFromForm();
        
        // Create a temporary Redis service for testing
        RedisService testService = new JedisRedisService();
        
        // Disable test button during test
        testButton.setEnabled(false);
        testButton.setText("Testing...");
        
        try {
            // Test connection
            RedisResult result = testService.connect(testConnection);
            
            if (result.isError()) {
                // Show error message
                Messages.showErrorDialog(
                        project,
                        "Connection failed: " + result.getError(),
                        "Connection Test"
                );
            } else {
                // Show success message
                Messages.showInfoMessage(
                        project,
                        "Connection successful!",
                        "Connection Test"
                );
            }
        } finally {
            // Cleanup
            testService.disconnect();
            
            // Re-enable test button
            testButton.setEnabled(true);
            testButton.setText("Test Connection");
        }
    }
    
    /**
     * Creates a Redis connection object from the form values
     */
    private RedisConnection createConnectionFromForm() {
        RedisConnection connection = originalConnection != null ? 
                originalConnection : new RedisConnection();
        
        connection.setName(nameField.getText().trim());
        connection.setHost(hostField.getText().trim());
        connection.setPort(Integer.parseInt(portField.getText().trim()));
        connection.setPassword(new String(passwordField.getPassword()));
        connection.setUseSSL(useSslCheckbox.isSelected());
        connection.setDatabase(Integer.parseInt(databaseField.getText().trim()));
        connection.setTimeout(Integer.parseInt(timeoutField.getText().trim()));
        
        return connection;
    }
    
    /**
     * Gets the connection object with values from the form
     */
    public RedisConnection getConnection() {
        return createConnectionFromForm();
    }
}
