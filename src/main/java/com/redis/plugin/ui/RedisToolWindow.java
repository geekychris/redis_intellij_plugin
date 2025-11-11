package com.redis.plugin.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBTabbedPane;
import com.redis.plugin.model.RedisConnection;
import com.redis.plugin.model.RedisResult;
import com.redis.plugin.service.RedisConnectionManager;
import com.redis.plugin.ui.panel.CommandCatalogPanel;
import com.redis.plugin.ui.panel.CommandPanel;
import com.redis.plugin.ui.panel.ConnectionPanel;
import com.redis.plugin.ui.panel.ConsolePanel;
import com.redis.plugin.ui.panel.ResultPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Main tool window for Redis client
 */
public class RedisToolWindow implements Disposable {
    private final Project project;
    private final SimpleToolWindowPanel panel;
    private ConnectionPanel connectionPanel;
    private CommandPanel commandPanel;
    private ResultPanel resultPanel;
    private ConsolePanel consolePanel;
    private CommandCatalogPanel commandCatalogPanel;
    private final RedisConnectionManager connectionManager;

    public RedisToolWindow(Project project, ToolWindow toolWindow) {
        this.project = project;
        panel = new SimpleToolWindowPanel(true, true);
        connectionManager = ApplicationManager.getApplication().getService(RedisConnectionManager.class);
        
        initComponents();
        setupToolbar();
        setupListeners();
        
        Disposer.register(project, this);
    }

    private void initComponents() {
        // Create UI components
        connectionPanel = new ConnectionPanel(project);
        commandPanel = new CommandPanel(project);
        resultPanel = new ResultPanel(project);
        consolePanel = new ConsolePanel(project);
        commandCatalogPanel = new CommandCatalogPanel(project);

        // Command panel at the top
        JPanel commandArea = new JPanel(new BorderLayout());
        commandArea.add(commandPanel, BorderLayout.CENTER);

        // Results and console in tabs
        JBTabbedPane resultTabs = new JBTabbedPane();
        resultTabs.addTab("Results", resultPanel);
        resultTabs.addTab("Console", consolePanel);

        // Command area and results/console in vertical split
        JBSplitter commandResultSplitter = new JBSplitter(true, 0.3f);
        commandResultSplitter.setFirstComponent(commandArea);
        commandResultSplitter.setSecondComponent(resultTabs);
        
        // Connection panel and catalog in tabs
        JBTabbedPane leftTabs = new JBTabbedPane();
        leftTabs.addTab("Connections", connectionPanel);
        leftTabs.addTab("Commands", commandCatalogPanel);
        
        // Left panel and command/result panel in horizontal split
        JBSplitter mainSplitter = new JBSplitter(false, 0.25f);
        mainSplitter.setFirstComponent(leftTabs);
        mainSplitter.setSecondComponent(commandResultSplitter);
        
        panel.setContent(mainSplitter);
    }
    
    private void setupToolbar() {
        DefaultActionGroup actionGroup = new DefaultActionGroup("Redis Actions", false);
        actionGroup.add(ActionManager.getInstance().getAction("Redis.NewConnection"));
        actionGroup.add(ActionManager.getInstance().getAction("Redis.ExecuteCommand"));
        actionGroup.add(ActionManager.getInstance().getAction("Redis.Refresh"));
        
        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("RedisToolbar", actionGroup, true);
        toolbar.setTargetComponent(panel);
        panel.setToolbar(toolbar.getComponent());
    }
    
    private void setupListeners() {
        // Connection panel sends commands to connection manager
        connectionPanel.addConnectionListener((connection, connect) -> {
            if (connect) {
                // Connect to Redis
                RedisResult result = connectionManager.connect(connection.getId());
                resultPanel.displayResult(result);
                
                if (!result.isError()) {
                    // Update UI elements
                    commandPanel.setEnabled(true);
                    resultPanel.setEnabled(true);
                    consolePanel.setEnabled(true);
                }
            } else {
                // Disconnect from Redis
                connectionManager.disconnect();
                
                // Update UI elements
                commandPanel.setEnabled(false);
                resultPanel.setEnabled(false);
                consolePanel.setEnabled(false);
                resultPanel.clear();
            }
        });
        
        // Command panel executes commands
        commandPanel.addCommandListener(command -> {
            if (connectionManager.isConnected()) {
                RedisResult result = connectionManager.getRedisService().execute(command);

                // Update both result panel and console
                resultPanel.displayResult(result);
                consolePanel.addCommandExecution(command, result);

                // Add to command panel history
                commandPanel.addToHistory(command);
            }
        });

        // Console panel sends clicked commands back to command panel
        consolePanel.addCommandClickListener(command -> {
            commandPanel.setCommand(command);
        });

        // Command catalog panel sends commands to command panel
        commandCatalogPanel.addCommandSelectionListener(command -> {
            commandPanel.setCommand(command.getSyntax());
        });
    }
    
    public JComponent getContent() {
        return panel;
    }
    
    @Override
    public void dispose() {
        // Clean up resources
        if (connectionManager.isConnected()) {
            connectionManager.disconnect();
        }
    }
}
