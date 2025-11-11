package com.redis.plugin.ui.panel;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.TextTransferable;
import com.intellij.util.ui.tree.TreeUtil;
import com.redis.plugin.model.RedisResult;
import com.redis.plugin.model.RedisResultType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Panel for displaying Redis command results
 */
public class ResultPanel extends SimpleToolWindowPanel {
    private final Project project;
    private final JBTabbedPane tabbedPane;
    private final JTextArea textResultArea;
    private final JTable tableResultArea;
    private final Tree treeResultArea;
    private final DefaultTreeModel treeModel;
    private final DefaultMutableTreeNode rootNode;
    private final JBLabel statusLabel;
    private final JBLabel typeLabel;
    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    
    private static final String CARD_TEXT = "TEXT";
    private static final String CARD_TABLE = "TABLE";
    private static final String CARD_TREE = "TREE";
    private static final String CARD_EMPTY = "EMPTY";
    
    private RedisResult currentResult;
    
    public ResultPanel(Project project) {
        super(true);
        this.project = project;
        
        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(JBUI.Borders.empty(5));
        statusLabel = new JBLabel("No results");
        typeLabel = new JBLabel();
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(typeLabel, BorderLayout.EAST);
        
        // Text result area
        textResultArea = new JTextArea();
        textResultArea.setEditable(false);
        textResultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        // Table result area
        tableResultArea = new JTable();
        tableResultArea.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tableResultArea.getTableHeader().setReorderingAllowed(false);
        
        // Tree result area
        rootNode = new DefaultMutableTreeNode("Result");
        treeModel = new DefaultTreeModel(rootNode);
        treeResultArea = new Tree(treeModel);
        treeResultArea.setCellRenderer(new RedisResultTreeCellRenderer());
        
        // Empty panel
        JPanel emptyPanel = new JBPanel<>(new BorderLayout());
        JBLabel emptyLabel = new JBLabel("No results to display", SwingConstants.CENTER);
        emptyLabel.setForeground(JBColor.GRAY);
        emptyPanel.add(emptyLabel, BorderLayout.CENTER);
        
        // Card layout for different result types
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.add(new JBScrollPane(textResultArea), CARD_TEXT);
        contentPanel.add(new JBScrollPane(tableResultArea), CARD_TABLE);
        contentPanel.add(new JBScrollPane(treeResultArea), CARD_TREE);
        contentPanel.add(emptyPanel, CARD_EMPTY);
        
        // Tabbed pane for multiple results
        tabbedPane = new JBTabbedPane();
        tabbedPane.addTab("Result", contentPanel);
        
        // Setup main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        setContent(mainPanel);
        
        // Setup toolbar
        setupToolbar();
        
        // Add context menu to tree
        treeResultArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    TreePath path = treeResultArea.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        treeResultArea.setSelectionPath(path);
                        createContextMenu().show(treeResultArea, e.getX(), e.getY());
                    }
                }
            }
        });
        
        // Show empty state initially
        cardLayout.show(contentPanel, CARD_EMPTY);
    }
    
    private void setupToolbar() {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        
        // Copy action
        AnAction copyAction = new AnAction("Copy", "Copy result to clipboard", AllIcons.Actions.Copy) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                copyCurrentResult();
            }
            
            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setEnabled(currentResult != null);
            }
            
            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        };
        
        // Export action
        AnAction exportAction = new AnAction("Export", "Export result to file", AllIcons.Actions.MenuSaveall) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                exportResult();
            }
            
            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setEnabled(currentResult != null);
            }
            
            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        };
        
        // Clear action
        AnAction clearAction = new AnAction("Clear", "Clear results", AllIcons.Actions.GC) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                clear();
            }
            
            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        };
        
        actionGroup.add(copyAction);
        actionGroup.add(exportAction);
        actionGroup.addSeparator();
        actionGroup.add(clearAction);
        
        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("ResultPanelToolbar", actionGroup, true);
        toolbar.setTargetComponent(this);
        setToolbar(toolbar.getComponent());
    }
    
    private JPopupMenu createContextMenu() {
        JPopupMenu menu = new JPopupMenu();
        
        JMenuItem copyItem = new JMenuItem("Copy Value", AllIcons.Actions.Copy);
        copyItem.addActionListener(e -> copySelectedNode());
        
        menu.add(copyItem);
        return menu;
    }
    
    /**
     * Display a Redis command result
     * @param result the result to display
     */
    public void displayResult(RedisResult result) {
        if (result == null) {
            clear();
            return;
        }
        
        this.currentResult = result;
        
        // Update status
        if (result.getExecutionTime() > 0) {
            statusLabel.setText("Executed in " + result.getExecutionTime() + " ms");
        } else {
            statusLabel.setText("");
        }
        
        // Update type label
        typeLabel.setText(result.getType().getDisplayName());
        
        // Format result based on type
        if (result.isError()) {
            displayErrorResult(result);
        } else {
            switch (result.getType()) {
                case STRING:
                case INTEGER:
                case BOOLEAN:
                case STATUS:
                    displayTextResult(result);
                    break;
                case ARRAY:
                    displayArrayResult(result);
                    break;
                case HASH:
                    displayHashResult(result);
                    break;
                case SET:
                case ZSET:
                    displaySetResult(result);
                    break;
                case NIL:
                    displayNilResult();
                    break;
                default:
                    displayTextResult(result);
            }
        }
    }
    
    private void displayTextResult(RedisResult result) {
        String text = result.getValue() != null ? result.getValue().toString() : "(nil)";
        textResultArea.setText(text);
        textResultArea.setForeground(JBColor.foreground());
        textResultArea.setCaretPosition(0);
        cardLayout.show(contentPanel, CARD_TEXT);
    }
    
    private void displayErrorResult(RedisResult result) {
        textResultArea.setText("ERROR: " + result.getError());
        textResultArea.setForeground(JBColor.RED);
        cardLayout.show(contentPanel, CARD_TEXT);
    }
    
    private void displayNilResult() {
        textResultArea.setText("(nil)");
        textResultArea.setForeground(JBColor.GRAY);
        cardLayout.show(contentPanel, CARD_TEXT);
    }
    
    private void displayArrayResult(RedisResult result) {
        if (result.getValue() instanceof List) {
            List<?> list = (List<?>) result.getValue();
            
            // Reset tree
            rootNode.removeAllChildren();
            
            // For small lists, use a table
            if (list.size() <= 100) {
                DefaultTableModel model = new DefaultTableModel(
                        new Object[]{"Index", "Value"}, 0);
                
                for (int i = 0; i < list.size(); i++) {
                    model.addRow(new Object[]{i, list.get(i)});
                }
                
                tableResultArea.setModel(model);
                cardLayout.show(contentPanel, CARD_TABLE);
            } else {
                // For large lists, use a tree
                DefaultMutableTreeNode arrayNode = new DefaultMutableTreeNode("Array (" + list.size() + " items)");
                rootNode.add(arrayNode);
                
                for (int i = 0; i < list.size(); i++) {
                    arrayNode.add(new DefaultMutableTreeNode("[" + i + "] " + list.get(i)));
                }
                
                treeModel.reload();
                TreeUtil.expandAll(treeResultArea);
                cardLayout.show(contentPanel, CARD_TREE);
            }
        } else {
            // Fallback to text display
            displayTextResult(result);
        }
    }
    
    private void displayHashResult(RedisResult result) {
        if (result.getValue() instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) result.getValue();
            
            // Reset tree
            rootNode.removeAllChildren();
            
            // For small maps, use a table
            if (map.size() <= 100) {
                DefaultTableModel model = new DefaultTableModel(
                        new Object[]{"Field", "Value"}, 0);
                
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    model.addRow(new Object[]{entry.getKey(), entry.getValue()});
                }
                
                tableResultArea.setModel(model);
                cardLayout.show(contentPanel, CARD_TABLE);
            } else {
                // For large maps, use a tree
                DefaultMutableTreeNode hashNode = new DefaultMutableTreeNode("Hash (" + map.size() + " fields)");
                rootNode.add(hashNode);
                
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    hashNode.add(new DefaultMutableTreeNode(new KeyValue(entry.getKey(), entry.getValue())));
                }
                
                treeModel.reload();
                TreeUtil.expandAll(treeResultArea);
                cardLayout.show(contentPanel, CARD_TREE);
            }
        } else {
            // Fallback to text display
            displayTextResult(result);
        }
    }
    
    private void displaySetResult(RedisResult result) {
        if (result.getValue() instanceof Set) {
            Set<?> set = (Set<?>) result.getValue();
            
            // Reset tree
            rootNode.removeAllChildren();
            
            // For small sets, use a table
            if (set.size() <= 100) {
                DefaultTableModel model = new DefaultTableModel(
                        new Object[]{"Index", "Value"}, 0);
                
                int i = 0;
                for (Object element : set) {
                    model.addRow(new Object[]{i++, element});
                }
                
                tableResultArea.setModel(model);
                cardLayout.show(contentPanel, CARD_TABLE);
            } else {
                // For large sets, use a tree
                DefaultMutableTreeNode setNode = new DefaultMutableTreeNode("Set (" + set.size() + " members)");
                rootNode.add(setNode);
                
                int i = 0;
                for (Object element : set) {
                    setNode.add(new DefaultMutableTreeNode(new IndexedValue(i++, element)));
                }
                
                treeModel.reload();
                TreeUtil.expandAll(treeResultArea);
                cardLayout.show(contentPanel, CARD_TREE);
            }
        } else {
            // Fallback to text display
            displayTextResult(result);
        }
    }
    
    /**
     * Clear all results
     */
    public void clear() {
        textResultArea.setText("");
        rootNode.removeAllChildren();
        treeModel.reload();
        statusLabel.setText("No results");
        typeLabel.setText("");
        currentResult = null;
        cardLayout.show(contentPanel, CARD_EMPTY);
    }
    
    /**
     * Copy current result to clipboard
     */
    private void copyCurrentResult() {
        String content;
        
        if (cardLayout.toString().equals(CARD_TEXT)) {
            content = textResultArea.getText();
        } else if (cardLayout.toString().equals(CARD_TABLE)) {
            StringBuilder sb = new StringBuilder();
            DefaultTableModel model = (DefaultTableModel) tableResultArea.getModel();
            int colCount = model.getColumnCount();
            
            // Add headers
            for (int i = 0; i < colCount; i++) {
                sb.append(model.getColumnName(i));
                sb.append(i < colCount - 1 ? "\t" : "\n");
            }
            
            // Add data
            for (int row = 0; row < model.getRowCount(); row++) {
                for (int col = 0; col < colCount; col++) {
                    Object value = model.getValueAt(row, col);
                    sb.append(value != null ? value.toString() : "null");
                    sb.append(col < colCount - 1 ? "\t" : "\n");
                }
            }
            content = sb.toString();
        } else {
            StringBuilder builder = new StringBuilder();
            appendNodeToBuilder(rootNode, builder, 0);
            content = builder.toString();
        }
        
        CopyPasteManager.getInstance().setContents(new StringSelection(content));
    }
    
    /**
     * Copy selected node value to clipboard
     */
    private void copySelectedNode() {
        if (cardLayout.toString().equals(CARD_TREE)) {
            TreePath path = treeResultArea.getSelectionPath();
            if (path == null) {
                return;
            }
            
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            Object userObject = node.getUserObject();
            
            String text;
            if (userObject instanceof IndexedValue) {
                text = ((IndexedValue) userObject).value.toString();
            } else if (userObject instanceof KeyValue) {
                text = ((KeyValue) userObject).value.toString();
            } else {
                text = userObject.toString();
            }
            
            CopyPasteManager.getInstance().setContents(new StringSelection(text));
        } else if (cardLayout.toString().equals(CARD_TABLE)) {
            int row = tableResultArea.getSelectedRow();
            int col = tableResultArea.getSelectedColumn();
            
            if (row >= 0 && col >= 0) {
                Object value = tableResultArea.getValueAt(row, col);
                String text = value != null ? value.toString() : "";
                CopyPasteManager.getInstance().setContents(new StringSelection(text));
            }
        }
    }
    
    /**
     * Export results to a file
     */
    private void exportResult() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Results");
        
        // Generate a default filename with timestamp
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = dateFormat.format(new Date());
        fileChooser.setSelectedFile(new File("redis_result_" + timestamp + ".txt"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(file)) {
                if (cardLayout.toString().equals(CARD_TEXT)) {
                    writer.write(textResultArea.getText());
                } else if (cardLayout.toString().equals(CARD_TABLE)) {
                    DefaultTableModel model = (DefaultTableModel) tableResultArea.getModel();
                    int colCount = model.getColumnCount();
                    
                    // Write headers
                    for (int i = 0; i < colCount; i++) {
                        writer.write(model.getColumnName(i));
                        writer.write(i < colCount - 1 ? "\t" : "\n");
                    }
                    
                    // Write data
                    for (int row = 0; row < model.getRowCount(); row++) {
                        for (int col = 0; col < colCount; col++) {
                            Object value = model.getValueAt(row, col);
                            writer.write(value != null ? value.toString() : "null");
                            writer.write(col < colCount - 1 ? "\t" : "\n");
                        }
                    }
                } else {
                    StringBuilder builder = new StringBuilder();
                    appendNodeToBuilder(rootNode, builder, 0);
                    writer.write(builder.toString());
                }
                JOptionPane.showMessageDialog(this, 
                        "Results exported to " + file.getAbsolutePath(), 
                        "Export Successful", 
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                        "Error exporting results: " + e.getMessage(), 
                        "Export Failed", 
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void appendNodeToBuilder(DefaultMutableTreeNode node, StringBuilder builder, int level) {
        // Indent based on level
        for (int i = 0; i < level - 1; i++) {
            builder.append("  ");
        }
        builder.append("- ");
        
        // Append node value
        Object userObject = node.getUserObject();
        if (userObject instanceof IndexedValue) {
            IndexedValue iv = (IndexedValue) userObject;
            builder.append("[").append(iv.index).append("] ").append(iv.value);
        } else if (userObject instanceof KeyValue) {
            KeyValue kv = (KeyValue) userObject;
            builder.append(kv.key).append(": ").append(kv.value);
        } else {
            builder.append(userObject);
        }
        builder.append("\n");
        
        // Append children
        for (int i = 0; i < node.getChildCount(); i++) {
            appendNodeToBuilder((DefaultMutableTreeNode) node.getChildAt(i), builder, level + 1);
        }
    }
    
    /**
     * Enable or disable the result panel
     * @param enabled true to enable, false to disable
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        textResultArea.setEnabled(enabled);
        treeResultArea.setEnabled(enabled);
    }
    
    /**
     * Value holder for indexed items (arrays, lists, sets)
     */
    private static class IndexedValue {
        final int index;
        final Object value;
        
        public IndexedValue(int index, Object value) {
            this.index = index;
            this.value = value;
        }
        
        @Override
        public String toString() {
            return "[" + index + "] " + (value != null ? value.toString() : "null");
        }
    }
    
    /**
     * Value holder for key-value pairs (hashes)
     */
    private static class KeyValue {
        final Object key;
        final Object value;
        
        public KeyValue(Object key, Object value) {
            this.key = key;
            this.value = value;
        }
        
        @Override
        public String toString() {
            return key + ": " + (value != null ? value.toString() : "null");
        }
    }
    
    /**
     * Tree cell renderer for Redis results
     */
    private static class RedisResultTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, 
                                                     boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();
            
            if (userObject instanceof String) {
                // Root or category node
                setIcon(AllIcons.Nodes.Folder);
            } else if (userObject instanceof IndexedValue) {
                // Array/List/Set item
                setIcon(AllIcons.Nodes.DataTables);
            } else if (userObject instanceof KeyValue) {
                // Hash field
                setIcon(AllIcons.Nodes.Field);
            }
            
            return this;
        }
    }
}
