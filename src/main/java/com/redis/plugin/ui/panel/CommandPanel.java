package com.redis.plugin.ui.panel;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.redis.plugin.service.RedisHistoryService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Panel for entering and executing Redis commands
 */
public class CommandPanel extends JBPanel<CommandPanel> {
    private final Project project;
    private final JTextArea commandTextArea;
    private final JButton executeButton;
    private final ComboBox<String> historyComboBox;
    private final DefaultComboBoxModel<String> historyModel;
    private final RedisHistoryService historyService;
    private final List<Consumer<String>> commandListeners;

    public CommandPanel(Project project) {
        super(new BorderLayout());
        this.project = project;
        this.historyService = project.getService(RedisHistoryService.class);
        this.commandListeners = new ArrayList<>();

        // Command input
        commandTextArea = new JTextArea();
        commandTextArea.setRows(3);
        commandTextArea.setBorder(JBUI.Borders.empty(5));
        commandTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        // Execute button
        executeButton = new JButton("Execute", AllIcons.Actions.Execute);
        executeButton.setEnabled(false);

        // History dropdown
        historyModel = new DefaultComboBoxModel<>();
        historyComboBox = new ComboBox<>(historyModel);
        historyComboBox.setEditable(false);

        // Layout components
        JPanel commandPanel = new JPanel(new BorderLayout());
        commandPanel.add(new JBScrollPane(commandTextArea), BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(executeButton);

        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.add(new JBLabel("History: "), BorderLayout.WEST);
        historyPanel.add(historyComboBox, BorderLayout.CENTER);

        controlPanel.add(historyPanel, BorderLayout.WEST);
        controlPanel.add(buttonPanel, BorderLayout.EAST);

        add(commandPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        // Set up event listeners
        executeButton.addActionListener(e -> executeCommand());

        // Command history selection handling
        historyComboBox.addActionListener(e -> {
            String selectedCommand = (String) historyComboBox.getSelectedItem();
            if (selectedCommand != null && !selectedCommand.isEmpty()) {
                commandTextArea.setText(selectedCommand);
            }
        });

        // Enable/disable execute button based on command text
        commandTextArea.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                executeButton.setEnabled(!commandTextArea.getText().trim().isEmpty());
            }
        });

        // Add keyboard shortcuts
        commandTextArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
                    executeCommand();
                    e.consume();
                }
            }
        });

        // Load initial history
        updateHistoryDropdown();

        // Initial state
        setEnabled(false);
    }

    /**
     * Execute the current command
     */
    private void executeCommand() {
        String command = commandTextArea.getText().trim();
        if (command.isEmpty()) {
            return;
        }

        // Notify listeners
        for (Consumer<String> listener : commandListeners) {
            listener.accept(command);
        }
    }

    /**
     * Add a command to the history
     *
     * @param command the command to add
     */
    public void addToHistory(String command) {
        // Add to persistent history service
        historyService.addCommand(command);

        // Update history dropdown
        updateHistoryDropdown();
    }

    /**
     * Update the history dropdown with current command history
     */
    private void updateHistoryDropdown() {
        historyModel.removeAllElements();
        List<String> history = historyService.getHistory();

        // Add most recent 20 commands to dropdown (for UI performance)
        int maxDropdownItems = Math.min(20, history.size());
        for (int i = 0; i < maxDropdownItems; i++) {
            historyModel.addElement(history.get(i));
        }
    }

    /**
     * Set the command text
     *
     * @param command the command to set
     */
    public void setCommand(String command) {
        commandTextArea.setText(command);
        commandTextArea.requestFocus();
    }

    /**
     * Add a command listener
     *
     * @param listener the listener to add
     */
    public void addCommandListener(Consumer<String> listener) {
        commandListeners.add(listener);
    }

    /**
     * Enable or disable the command panel
     *
     * @param enabled true to enable, false to disable
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        commandTextArea.setEnabled(enabled);
        executeButton.setEnabled(enabled && !commandTextArea.getText().trim().isEmpty());
        historyComboBox.setEnabled(enabled);
    }
}
