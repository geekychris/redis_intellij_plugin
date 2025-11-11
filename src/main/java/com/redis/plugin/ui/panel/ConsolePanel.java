package com.redis.plugin.ui.panel;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.redis.plugin.model.RedisResult;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Console-style panel that shows command history with clickable commands for re-execution
 */
public class ConsolePanel extends JBPanel<ConsolePanel> {
    private final Project project;
    private final JTextPane consolePane;
    private final StyledDocument document;
    private final List<ConsoleEntry> entries;
    private final List<Consumer<String>> commandClickListeners;
    private final SimpleDateFormat timeFormat;

    // Styles for different types of content
    private Style commandStyle;
    private Style responseStyle;
    private Style errorStyle;
    private Style timestampStyle;
    private Style clickableCommandStyle;

    public ConsolePanel(Project project) {
        super(new BorderLayout());
        this.project = project;
        this.entries = new ArrayList<>();
        this.commandClickListeners = new ArrayList<>();
        this.timeFormat = new SimpleDateFormat("HH:mm:ss");

        // Create console text pane
        consolePane = new JTextPane();
        consolePane.setEditable(false);
        consolePane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        consolePane.setBackground(UIUtil.getTextFieldBackground());

        document = consolePane.getStyledDocument();
        initializeStyles();

        // Setup mouse listener for clickable commands
        consolePane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e);
            }
        });

        // Change cursor when hovering over clickable commands
        consolePane.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMove(e);
            }
        });

        // Create scroll pane
        JBScrollPane scrollPane = new JBScrollPane(consolePane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);

        // Setup toolbar
        setupToolbar();

        // Add welcome message
        addWelcomeMessage();
    }

    private void initializeStyles() {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        Style defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);

        // Command style (bold, blue)
        commandStyle = document.addStyle("command", defaultStyle);
        StyleConstants.setBold(commandStyle, true);
        StyleConstants.setForeground(commandStyle, new Color(0, 102, 204));

        // Clickable command style (bold, blue, underlined)
        clickableCommandStyle = document.addStyle("clickableCommand", defaultStyle);
        StyleConstants.setBold(clickableCommandStyle, true);
        StyleConstants.setForeground(clickableCommandStyle, new Color(0, 102, 204));
        StyleConstants.setUnderline(clickableCommandStyle, true);

        // Response style (normal, dark gray)
        responseStyle = document.addStyle("response", defaultStyle);
        StyleConstants.setForeground(responseStyle, JBColor.foreground());

        // Error style (bold, red)
        errorStyle = document.addStyle("error", defaultStyle);
        StyleConstants.setBold(errorStyle, true);
        StyleConstants.setForeground(errorStyle, JBColor.RED);

        // Timestamp style (italic, gray)
        timestampStyle = document.addStyle("timestamp", defaultStyle);
        StyleConstants.setItalic(timestampStyle, true);
        StyleConstants.setForeground(timestampStyle, JBColor.GRAY);
        StyleConstants.setFontSize(timestampStyle, 11);
    }

    private void setupToolbar() {
        DefaultActionGroup actionGroup = new DefaultActionGroup();

        // Clear console action
        AnAction clearAction = new AnAction("Clear Console", "Clear all console output", AllIcons.Actions.GC) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                clearConsole();
            }
        };

        // Copy all action
        AnAction copyAllAction = new AnAction("Copy All", "Copy all console content", AllIcons.Actions.Copy) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                copyAllContent();
            }
        };

        // Export console action
        AnAction exportAction = new AnAction("Export", "Export console to file", AllIcons.Actions.MenuSaveall) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                exportConsole();
            }
        };

        actionGroup.add(clearAction);
        actionGroup.addSeparator();
        actionGroup.add(copyAllAction);
        actionGroup.add(exportAction);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("ConsolePanelToolbar", actionGroup, true);
        toolbar.setTargetComponent(this);
        add(toolbar.getComponent(), BorderLayout.NORTH);
    }

    private void addWelcomeMessage() {
        try {
            document.insertString(document.getLength(),
                    "Redis Console - Click on any command to re-execute it\n\n",
                    responseStyle);
        } catch (BadLocationException e) {
            // Ignore
        }
    }

    /**
     * Add a command execution to the console
     *
     * @param command the Redis command that was executed
     * @param result  the result of the command execution
     */
    public void addCommandExecution(String command, RedisResult result) {
        ConsoleEntry entry = new ConsoleEntry(command, result, System.currentTimeMillis());
        entries.add(entry);

        try {
            // Add timestamp
            String timestamp = "[" + timeFormat.format(new Date(entry.timestamp)) + "] ";
            document.insertString(document.getLength(), timestamp, timestampStyle);

            // Add command as clickable text
            int commandStart = document.getLength();
            document.insertString(document.getLength(), "redis> " + command, clickableCommandStyle);
            int commandEnd = document.getLength();

            // Store command bounds for click detection
            entry.commandStart = commandStart;
            entry.commandEnd = commandEnd;

            document.insertString(document.getLength(), "\n", null);

            // Add result
            if (result.isError()) {
                document.insertString(document.getLength(),
                        "(error) " + result.getError() + "\n",
                        errorStyle);
            } else {
                String resultText = formatResult(result);
                document.insertString(document.getLength(), resultText + "\n", responseStyle);
            }

            // Add execution time if available
            if (result.getExecutionTime() > 0) {
                document.insertString(document.getLength(),
                        "(" + result.getExecutionTime() + " ms)\n",
                        timestampStyle);
            }

            document.insertString(document.getLength(), "\n", null);

            // Auto-scroll to bottom
            SwingUtilities.invokeLater(() -> {
                consolePane.setCaretPosition(document.getLength());
            });

        } catch (BadLocationException e) {
            // Log error but don't fail
            e.printStackTrace();
        }
    }

    private String formatResult(RedisResult result) {
        if (result.getValue() == null) {
            return "(nil)";
        }

        Object value = result.getValue();

        // Handle different result types
        switch (result.getType()) {
            case STRING:
                return "\"" + value.toString() + "\"";
            case INTEGER:
                return "(integer) " + value.toString();
            case ARRAY:
                if (value instanceof java.util.List) {
                    List<?> list = (List<?>) value;
                    if (list.isEmpty()) {
                        return "(empty list or set)";
                    }
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < list.size(); i++) {
                        sb.append(String.format("%d) %s", i + 1, formatValue(list.get(i))));
                        if (i < list.size() - 1) {
                            sb.append("\n");
                        }
                    }
                    return sb.toString();
                }
                break;
            case HASH:
                if (value instanceof java.util.Map) {
                    Map<?, ?> map = (Map<?, ?>) value;
                    if (map.isEmpty()) {
                        return "(empty hash)";
                    }
                    StringBuilder sb = new StringBuilder();
                    int i = 1;
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        sb.append(String.format("%d) \"%s\"\n%d) \"%s\"",
                                i, entry.getKey(), i + 1, entry.getValue()));
                        i += 2;
                        if (i <= map.size() * 2) {
                            sb.append("\n");
                        }
                    }
                    return sb.toString();
                }
                break;
            case SET:
            case ZSET:
                if (value instanceof java.util.Set) {
                    Set<?> set = (Set<?>) value;
                    if (set.isEmpty()) {
                        return "(empty set)";
                    }
                    StringBuilder sb = new StringBuilder();
                    int i = 1;
                    for (Object item : set) {
                        sb.append(String.format("%d) %s", i++, formatValue(item)));
                        if (i <= set.size()) {
                            sb.append("\n");
                        }
                    }
                    return sb.toString();
                }
                break;
            case BOOLEAN:
                return value.toString();
            case STATUS:
                return value.toString();
            case NIL:
                return "(nil)";
        }

        return value.toString();
    }

    private String formatValue(Object value) {
        if (value == null) {
            return "(nil)";
        }
        if (value instanceof String) {
            return "\"" + value + "\"";
        }
        return value.toString();
    }

    private void handleMouseClick(MouseEvent e) {
        int pos = consolePane.viewToModel2D(e.getPoint());

        // Find which command was clicked
        for (ConsoleEntry entry : entries) {
            if (pos >= entry.commandStart && pos <= entry.commandEnd) {
                // Notify listeners that a command was clicked
                for (Consumer<String> listener : commandClickListeners) {
                    listener.accept(entry.command);
                }
                break;
            }
        }
    }

    private void handleMouseMove(MouseEvent e) {
        int pos = consolePane.viewToModel2D(e.getPoint());
        boolean overCommand = false;

        // Check if mouse is over a clickable command
        for (ConsoleEntry entry : entries) {
            if (pos >= entry.commandStart && pos <= entry.commandEnd) {
                overCommand = true;
                break;
            }
        }

        // Change cursor accordingly
        consolePane.setCursor(overCommand ?
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) :
                Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    }

    /**
     * Add a listener for command click events
     *
     * @param listener the listener to add
     */
    public void addCommandClickListener(Consumer<String> listener) {
        commandClickListeners.add(listener);
    }

    /**
     * Clear all console content
     */
    public void clearConsole() {
        entries.clear();
        try {
            document.remove(0, document.getLength());
            addWelcomeMessage();
        } catch (BadLocationException e) {
            // Ignore
        }
    }

    /**
     * Copy all console content to clipboard
     */
    private void copyAllContent() {
        String content = consolePane.getText();
        CopyPasteManager.getInstance().setContents(new StringSelection(content));
    }

    /**
     * Export console content to a file
     */
    private void exportConsole() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Console");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = dateFormat.format(new Date());
        fileChooser.setSelectedFile(new java.io.File("redis_console_" + timestamp + ".txt"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                writer.write(consolePane.getText());
                JOptionPane.showMessageDialog(this,
                        "Console exported to " + file.getAbsolutePath(),
                        "Export Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (java.io.IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error exporting console: " + e.getMessage(),
                        "Export Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Get the number of entries in the console
     *
     * @return the number of entries
     */
    public int getEntryCount() {
        return entries.size();
    }

    /**
     * Enable or disable the console panel
     * @param enabled true to enable, false to disable
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        consolePane.setEnabled(enabled);
    }

    /**
     * Inner class to represent a console entry
     */
    private static class ConsoleEntry {
        final String command;
        final RedisResult result;
        final long timestamp;
        int commandStart;
        int commandEnd;

        ConsoleEntry(String command, RedisResult result, long timestamp) {
            this.command = command;
            this.result = result;
            this.timestamp = timestamp;
        }
    }
}