package com.redis.plugin.ui.panel;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.redis.plugin.model.RedisConnection;
import com.redis.plugin.service.RedisConnectionManager;
import com.redis.plugin.ui.dialog.ConnectionDialog;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Panel for managing Redis connections
 */
public class ConnectionPanel extends JBPanel<ConnectionPanel> {
    private final Project project;
    private final RedisConnectionManager connectionManager;
    private final JBList<RedisConnection> connectionList;
    private final DefaultListModel<RedisConnection> listModel;
    private final List<BiConsumer<RedisConnection, Boolean>> connectionListeners = new ArrayList<>();
    private final JButton connectButton;
    private final JButton disconnectButton;
    private final JComboBox<Integer> databaseComboBox;
    private final JPanel detailsPanel;
    private final JBLabel statusLabel;

    private JPanel emptyStatePanel;
    private JPanel listPanel;

    public ConnectionPanel(Project project) {
        super(new BorderLayout());
        this.project = project;
        this.connectionManager = ApplicationManager.getApplication().getService(RedisConnectionManager.class);
        
        // Create connection list
        listModel = new DefaultListModel<>();
        connectionList = new JBList<>(listModel);
        connectionList.setCellRenderer(new ConnectionCellRenderer());
        connectionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Create toolbar for connection list
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(connectionList)
                .setAddAction(button -> addConnection())
                .setEditAction(button -> editConnection())
                .setRemoveAction(button -> removeConnection())
                .disableUpDownActions();
        
        // Create list panel with toolbar
        listPanel = new JPanel(new BorderLayout());
        listPanel.add(decorator.createPanel(), BorderLayout.CENTER);
        
        // Create empty state panel
        emptyStatePanel = createEmptyStatePanel();
        
        // Create database selector
        databaseComboBox = new ComboBox<>(new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});
        databaseComboBox.setEnabled(false);
        databaseComboBox.addActionListener(e -> {
            if (connectionManager.isConnected()) {
                int db = (Integer) databaseComboBox.getSelectedItem();
                selectDatabase(db);
            }
        });
        
        // Create buttons panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        connectButton = new JButton("Connect", AllIcons.Actions.Execute);
        disconnectButton = new JButton("Disconnect", AllIcons.Actions.Suspend);
        disconnectButton.setEnabled(false);
        
        controlPanel.add(connectButton);
        controlPanel.add(disconnectButton);
        
        // Create database selector panel
        JPanel dbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dbPanel.add(new JBLabel("Database:"));
        dbPanel.add(databaseComboBox);
        
        // Create status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JBLabel();
        statusLabel.setBorder(JBUI.Borders.empty(5));
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        
        // Create connection details panel
        detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBorder(JBUI.Borders.empty(5));
        
        // Create bottom panel with controls
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(controlPanel, BorderLayout.NORTH);
        bottomPanel.add(dbPanel, BorderLayout.CENTER);
        bottomPanel.add(statusPanel, BorderLayout.SOUTH);
        
        // Add components to main panel - initially show empty state or list
        // This will be updated by loadConnections()
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Add action listeners
        connectButton.addActionListener(e -> connectToSelected());
        disconnectButton.addActionListener(e -> disconnect());
        
        // Update button state on selection change
        connectionList.addListSelectionListener(e -> {
            boolean hasSelection = !connectionList.isSelectionEmpty();
            boolean isConnected = connectionManager.isConnected();
            
            connectButton.setEnabled(hasSelection && !isConnected);
            disconnectButton.setEnabled(isConnected);
            databaseComboBox.setEnabled(isConnected);
            
            if (hasSelection) {
                RedisConnection connection = connectionList.getSelectedValue();
                updateDetailsPanel(connection);
            } else {
                clearDetailsPanel();
            }
        });
        
        // Load connections
        loadConnections();
        
        // Update connection status
        updateConnectionStatus();
    }
    
    /**
     * Create empty state panel shown when no connections exist
     */
    private JPanel createEmptyStatePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = JBUI.insets(10);
        
        // Icon
        JBLabel iconLabel = new JBLabel(AllIcons.General.Information);
        iconLabel.setFont(iconLabel.getFont().deriveFont(48f));
        panel.add(iconLabel, gbc);
        
        // Message
        gbc.gridy++;
        JBLabel messageLabel = new JBLabel("No Redis connections configured");
        messageLabel.setFont(messageLabel.getFont().deriveFont(Font.BOLD, 14f));
        panel.add(messageLabel, gbc);
        
        // Description
        gbc.gridy++;
        JBLabel descLabel = new JBLabel("Click the button below to create your first connection");
        descLabel.setForeground(JBUI.CurrentTheme.ContextHelp.FOREGROUND);
        panel.add(descLabel, gbc);
        
        // Add button
        gbc.gridy++;
        gbc.insets = JBUI.insets(20, 10, 10, 10);
        JButton addButton = new JButton("New Connection", AllIcons.General.Add);
        addButton.addActionListener(e -> addConnection());
        panel.add(addButton, gbc);
        
        return panel;
    }
    
    /**
     * Load connections from connection manager
     */
    private void loadConnections() {
        listModel.clear();
        for (RedisConnection connection : connectionManager.getConnections()) {
            listModel.addElement(connection);
        }
        
        // Toggle between empty state and list view
        remove(listPanel);
        remove(emptyStatePanel);
        
        if (listModel.isEmpty()) {
            add(emptyStatePanel, BorderLayout.CENTER);
        } else {
            add(listPanel, BorderLayout.CENTER);
        }
        
        // Select active connection if any
        if (connectionManager.isConnected()) {
            RedisConnection activeConnection = connectionManager.getActiveConnection();
            if (activeConnection != null) {
                connectionList.setSelectedValue(activeConnection, true);
            }
        }
        
        revalidate();
        repaint();
        updateConnectionStatus();
    }
    
    /**
     * Add a new connection
     */
    private void addConnection() {
        ConnectionDialog dialog = new ConnectionDialog(project, null);
        if (dialog.showAndGet()) {
            RedisConnection connection = dialog.getConnection();
            connectionManager.addConnection(connection);
            loadConnections();  // Reload to switch from empty state to list view
            connectionList.setSelectedValue(connection, true);
        }
    }
    
    /**
     * Edit the selected connection
     */
    private void editConnection() {
        RedisConnection selected = connectionList.getSelectedValue();
        if (selected == null) {
            return;
        }
        
        // Don't allow editing active connection
        if (connectionManager.isConnected() && 
            Objects.equals(connectionManager.getActiveConnectionId(), selected.getId())) {
            Messages.showWarningDialog(
                    project,
                    "Cannot edit an active connection. Please disconnect first.",
                    "Edit Connection"
            );
            return;
        }
        
        ConnectionDialog dialog = new ConnectionDialog(project, selected);
        if (dialog.showAndGet()) {
            RedisConnection updated = dialog.getConnection();
            connectionManager.updateConnection(updated);
            loadConnections();
        }
    }
    
    /**
     * Remove the selected connection
     */
    private void removeConnection() {
        RedisConnection selected = connectionList.getSelectedValue();
        if (selected == null) {
            return;
        }
        
        // Don't allow removing active connection
        if (connectionManager.isConnected() && 
            Objects.equals(connectionManager.getActiveConnectionId(), selected.getId())) {
            Messages.showWarningDialog(
                    project,
                    "Cannot remove an active connection. Please disconnect first.",
                    "Remove Connection"
            );
            return;
        }
        
        int result = Messages.showYesNoDialog(
                project,
                "Are you sure you want to remove the connection '" + selected.getName() + "'?",
                "Remove Connection",
                Messages.getQuestionIcon()
        );
        
        if (result == Messages.YES) {
            connectionManager.removeConnection(selected.getId());
            loadConnections();  // Reload to switch to empty state if last connection removed
        }
    }
    
    /**
     * Connect to the selected Redis server
     */
    private void connectToSelected() {
        RedisConnection selected = connectionList.getSelectedValue();
        if (selected == null) {
            return;
        }
        
        // Update status
        statusLabel.setText("Connecting to " + selected.getName() + "...");
        statusLabel.setIcon(AllIcons.RunConfigurations.TestPassed);
        
        // Notify listeners
        for (BiConsumer<RedisConnection, Boolean> listener : connectionListeners) {
            listener.accept(selected, true);
        }
        
        // Update database selector
        databaseComboBox.setSelectedItem(selected.getDatabase());
        
        // Update UI
        updateConnectionStatus();
    }
    
    /**
     * Disconnect from the current Redis server
     */
    private void disconnect() {
        RedisConnection activeConnection = connectionManager.getActiveConnection();
        if (activeConnection == null) {
            return;
        }
        
        // Update status
        statusLabel.setText("Disconnecting...");
        
        // Notify listeners
        for (BiConsumer<RedisConnection, Boolean> listener : connectionListeners) {
            listener.accept(activeConnection, false);
        }
        
        // Update UI
        updateConnectionStatus();
    }
    
    /**
     * Select a database on the current connection
     */
    private void selectDatabase(int db) {
        if (connectionManager.isConnected()) {
            // Execute SELECT command
            connectionManager.getRedisService().execute("SELECT " + db);
            
            // Update connection database
            RedisConnection activeConnection = connectionManager.getActiveConnection();
            if (activeConnection != null) {
                activeConnection.setDatabase(db);
                connectionManager.updateConnection(activeConnection);
            }
        }
    }
    
    /**
     * Update the connection status display
     */
    private void updateConnectionStatus() {
        boolean isConnected = connectionManager.isConnected();
        connectButton.setEnabled(!isConnected && !connectionList.isSelectionEmpty());
        disconnectButton.setEnabled(isConnected);
        databaseComboBox.setEnabled(isConnected);
        
        // Update status label
        if (isConnected) {
            RedisConnection activeConnection = connectionManager.getActiveConnection();
            if (activeConnection != null) {
                statusLabel.setText("Connected to " + activeConnection.getName());
                statusLabel.setIcon(AllIcons.General.InspectionsOK);
                databaseComboBox.setSelectedItem(activeConnection.getDatabase());
            }
        } else {
            statusLabel.setText("Not connected");
            statusLabel.setIcon(null);
        }
        
        // Update selection to show active connection
        if (isConnected) {
            RedisConnection activeConnection = connectionManager.getActiveConnection();
            if (activeConnection != null) {
                connectionList.setSelectedValue(activeConnection, true);
            }
        }
        
        // Update UI to show connected status
        connectionList.repaint();
    }
    
    /**
     * Update the details panel with connection information
     */
    private void updateDetailsPanel(RedisConnection connection) {
        detailsPanel.removeAll();
        
        if (connection != null) {
            JPanel infoPanel = new JPanel(new GridLayout(0, 2, 5, 5));
            infoPanel.setBorder(JBUI.Borders.empty(5));
            
            infoPanel.add(new JBLabel("Name:"));
            infoPanel.add(new JBLabel(connection.getName()));
            
            infoPanel.add(new JBLabel("Host:"));
            infoPanel.add(new JBLabel(connection.getHost()));
            
            infoPanel.add(new JBLabel("Port:"));
            infoPanel.add(new JBLabel(String.valueOf(connection.getPort())));
            
            infoPanel.add(new JBLabel("Database:"));
            infoPanel.add(new JBLabel(String.valueOf(connection.getDatabase())));
            
            infoPanel.add(new JBLabel("SSL:"));
            infoPanel.add(new JBLabel(connection.isUseSSL() ? "Yes" : "No"));
            
            detailsPanel.add(infoPanel, BorderLayout.CENTER);
        }
        
        detailsPanel.revalidate();
        detailsPanel.repaint();
    }
    
    /**
     * Clear the details panel
     */
    private void clearDetailsPanel() {
        detailsPanel.removeAll();
        detailsPanel.add(new JBLabel("No connection selected"), BorderLayout.CENTER);
        detailsPanel.revalidate();
        detailsPanel.repaint();
    }
    
    /**
     * Add a connection listener
     * @param listener Listener that will be notified of connection events
     */
    public void addConnectionListener(BiConsumer<RedisConnection, Boolean> listener) {
        connectionListeners.add(listener);
    }
    
    /**
     * Cell renderer for Redis connections in the list
     */
    private class ConnectionCellRenderer extends ColoredListCellRenderer<RedisConnection> {
        @Override
        protected void customizeCellRenderer(@NotNull JList<? extends RedisConnection> list, 
                                            RedisConnection connection, 
                                            int index, 
                                            boolean selected, 
                                            boolean hasFocus) {
            setIcon(AllIcons.Nodes.DataColumn);
            String activeId = connectionManager.getActiveConnectionId();
            boolean isActive = activeId != null && activeId.equals(connection.getId());
            
            if (isActive) {
                append(connection.getName(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
                append(" (connected)", SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);
                setIcon(AllIcons.Nodes.DataSchema);
            } else {
                append(connection.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }
            
            append(" - " + connection.getHost() + ":" + connection.getPort(), 
                    SimpleTextAttributes.GRAYED_ATTRIBUTES);
        }
        }
    }
