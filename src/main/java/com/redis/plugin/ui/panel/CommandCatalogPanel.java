package com.redis.plugin.ui.panel;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.*;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;
import com.redis.plugin.model.RedisCommand;
import com.redis.plugin.model.RedisCommandCategory;
import com.redis.plugin.service.RedisCommandCatalog;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Panel for browsing Redis commands by category
 */
public class CommandCatalogPanel extends SimpleToolWindowPanel {
    private final Project project;
    private final JBTextField searchField;
    private final Tree commandTree;
    private final DefaultTreeModel treeModel;
    private final DefaultMutableTreeNode rootNode;
    private final JEditorPane detailsPane;
    private final RedisCommandCatalog commandCatalog;
    private final List<Consumer<RedisCommand>> commandSelectionListeners = new ArrayList<>();

    public CommandCatalogPanel(Project project) {
        super(true);
        this.project = project;
        this.commandCatalog = ApplicationManager.getApplication().getService(RedisCommandCatalog.class);
        
        // Search field
        searchField = new JBTextField();
        searchField.getEmptyText().setText("Search commands...");
        
        // Command tree
        rootNode = new DefaultMutableTreeNode("Redis Commands");
        treeModel = new DefaultTreeModel(rootNode);
        commandTree = new Tree(treeModel);
        commandTree.setCellRenderer(new CommandTreeCellRenderer());
        commandTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        // Command details panel
        detailsPane = new JEditorPane("text/html", "");
        detailsPane.setEditable(false);
        detailsPane.setBackground(UIUtil.getPanelBackground());
        detailsPane.setBorder(JBUI.Borders.empty(5));
        
        // Layout components
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.add(new JBLabel("  Search: "), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.setBorder(JBUI.Borders.empty(5));
        
        JBSplitter splitter = new JBSplitter(true, 0.4f);
        splitter.setFirstComponent(new JBScrollPane(commandTree));
        splitter.setSecondComponent(new JBScrollPane(detailsPane));
        
        JPanel mainPanel = new JBPanel<>(new BorderLayout());
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(splitter, BorderLayout.CENTER);
        
        setContent(mainPanel);
        setupToolbar();
        
        // Set up event listeners
        searchField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                filterTree(searchField.getText());
            }
        });
        
        commandTree.addTreeSelectionListener(this::handleTreeSelection);
        
        commandTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleCommandSelection();
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    TreePath path = commandTree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        commandTree.setSelectionPath(path);
                        createContextMenu().show(commandTree, e.getX(), e.getY());
                    }
                }
            }
        });
        
        // Load commands
        loadCommands();
    }
    private void setupToolbar() {
        // Create action group
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        
        // Insert action
        AnAction insertAction = new AnAction("Insert Command", "Insert command into editor", AllIcons.Actions.Edit) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                handleCommandSelection();
            }
            
            @Override
            public void update(@NotNull AnActionEvent e) {
                TreePath path = commandTree.getSelectionPath();
                e.getPresentation().setEnabled(path != null && isCommandNode(path.getLastPathComponent()));
            }
            
            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        };
        
        // Copy action
        AnAction copyAction = new AnAction("Copy Command", "Copy command syntax to clipboard", AllIcons.Actions.Copy) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                copySelectedCommand();
            }
            
            @Override
            public void update(@NotNull AnActionEvent e) {
                TreePath path = commandTree.getSelectionPath();
                e.getPresentation().setEnabled(path != null && isCommandNode(path.getLastPathComponent()));
            }
            
            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        };
        
        // Expand all action
        AnAction expandAction = new AnAction("Expand All", "Expand all categories", AllIcons.Actions.Expandall) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                TreeUtil.expandAll(commandTree);
            }
            
            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        };
        
        // Collapse all action
        AnAction collapseAction = new AnAction("Collapse All", "Collapse all categories", AllIcons.Actions.Collapseall) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                TreeUtil.collapseAll(commandTree, 1);
            }
            
            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        };
        
        actionGroup.add(insertAction);
        actionGroup.add(copyAction);
        actionGroup.addSeparator();
        actionGroup.add(expandAction);
        actionGroup.add(collapseAction);
        
        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("CommandCatalogToolbar", actionGroup, true);
        toolbar.setTargetComponent(this);
        setToolbar(toolbar.getComponent());
    }
    
    private JPopupMenu createContextMenu() {
        JPopupMenu menu = new JPopupMenu();
        
        JMenuItem insertItem = new JMenuItem("Insert Command", AllIcons.Actions.Edit);
        insertItem.addActionListener(e -> handleCommandSelection());
        
        JMenuItem copyItem = new JMenuItem("Copy Command", AllIcons.Actions.Copy);
        copyItem.addActionListener(e -> copySelectedCommand());
        
        TreePath path = commandTree.getSelectionPath();
        boolean isCommandNode = path != null && isCommandNode(path.getLastPathComponent());
        
        insertItem.setEnabled(isCommandNode);
        copyItem.setEnabled(isCommandNode);
        
        menu.add(insertItem);
        menu.add(copyItem);
        
        return menu;
    }
    
    private boolean isCommandNode(Object node) {
        if (!(node instanceof DefaultMutableTreeNode)) {
            return false;
        }
        
        Object userObject = ((DefaultMutableTreeNode) node).getUserObject();
        return userObject instanceof RedisCommand;
    }
    
    private void loadCommands() {
        rootNode.removeAllChildren();
        
        // Add all command categories
        for (RedisCommandCategory category : commandCatalog.getCategories()) {
            DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(category);
            rootNode.add(categoryNode);
            
            // Add commands in this category
            for (RedisCommand command : category.getCommands()) {
                categoryNode.add(new DefaultMutableTreeNode(command));
            }
        }
        
        treeModel.reload();
        TreeUtil.expandAll(commandTree);
        
        // Select the first command if available
        if (rootNode.getChildCount() > 0) {
            DefaultMutableTreeNode firstCategory = (DefaultMutableTreeNode) rootNode.getChildAt(0);
            if (firstCategory.getChildCount() > 0) {
                TreePath path = new TreePath(new Object[] { rootNode, firstCategory, firstCategory.getChildAt(0) });
                commandTree.setSelectionPath(path);
            }
        }
    }
    
    private void filterTree(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            loadCommands();
            return;
        }
        
        searchText = searchText.toLowerCase().trim();
        
        // Create a filtered tree
        rootNode.removeAllChildren();
        boolean anyMatch = false;
        
        for (RedisCommandCategory category : commandCatalog.getCategories()) {
            DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(category);
            boolean categoryHasMatches = false;
            
            for (RedisCommand command : category.getCommands()) {
                if (matches(command, searchText)) {
                    categoryNode.add(new DefaultMutableTreeNode(command));
                    categoryHasMatches = true;
                    anyMatch = true;
                }
            }
            
            if (categoryHasMatches) {
                rootNode.add(categoryNode);
            }
        }
        
        if (!anyMatch) {
            // No matches found
            DefaultMutableTreeNode noResultsNode = new DefaultMutableTreeNode("No matching commands found");
            rootNode.add(noResultsNode);
        }
        
        treeModel.reload();
        TreeUtil.expandAll(commandTree);
    }
    
    private boolean matches(RedisCommand command, String searchText) {
        return command.getName().toLowerCase().contains(searchText) || 
               command.getDescription().toLowerCase().contains(searchText) ||
               command.getSyntax().toLowerCase().contains(searchText);
    }
    
    private void handleTreeSelection(TreeSelectionEvent e) {
        TreePath path = e.getNewLeadSelectionPath();
        if (path == null) {
            detailsPane.setText("");
            return;
        }
        
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        Object userObject = node.getUserObject();
        
        if (userObject instanceof RedisCommand) {
            RedisCommand command = (RedisCommand) userObject;
            displayCommandDetails(command);
        } else if (userObject instanceof RedisCommandCategory) {
            RedisCommandCategory category = (RedisCommandCategory) userObject;
            displayCategoryDetails(category);
        } else {
            detailsPane.setText("");
        }
    }
    
    private void displayCommandDetails(RedisCommand command) {
        try {
            StringBuilder html = new StringBuilder();
            html.append("<html><body style='font-family: sans-serif; font-size: 12px;'>");
            
            // Command name and syntax
            html.append("<h2 style='color: #005CC5;'>").append(command.getName().toUpperCase()).append("</h2>");
            html.append("<div style='background-color: #F0F0F0; padding: 8px; border-radius: 4px; font-family: monospace;'>");
            html.append(command.getSyntax().replace("<", "&lt;").replace(">", "&gt;"));
            html.append("</div>");
            
            // Description
            html.append("<h3>Description</h3>");
            html.append("<p>").append(command.getDescription()).append("</p>");
            
            // Parameters
            if (!command.getParameters().isEmpty()) {
                html.append("<h3>Parameters</h3>");
                html.append("<ul>");
                for (String param : command.getParameters()) {
                    html.append("<li>").append(param).append("</li>");
                }
                html.append("</ul>");
            }
            
            // Examples
            if (!command.getExamples().isEmpty()) {
                html.append("<h3>Examples</h3>");
                html.append("<div style='background-color: #F0F0F0; padding: 8px; border-radius: 4px; font-family: monospace;'>");
                for (String example : command.getExamples()) {
                    html.append(example.replace("<", "&lt;").replace(">", "&gt;")).append("<br/>");
                }
                html.append("</div>");
            }
            
            // Return value
            if (!command.getReturnValues().isEmpty()) {
                html.append("<h3>Return Value</h3>");
                html.append("<ul>");
                for (String returnValue : command.getReturnValues()) {
                    html.append("<li>").append(returnValue).append("</li>");
                }
                html.append("</ul>");
            }
            
            // Complexity
            if (command.getComplexity() != null) {
                html.append("<h3>Time Complexity</h3>");
                html.append("<p>").append(command.getComplexity().toString()).append("</p>");
            }
            
            // Since version
            if (command.getSince() != null && !command.getSince().isEmpty()) {
                html.append("<p style='color: #666666;'>Since: Redis ").append(command.getSince()).append("</p>");
            }
            
            html.append("</body></html>");
            
            detailsPane.setText(html.toString());
            detailsPane.setCaretPosition(0);
        } catch (Exception e) {
            // Ignore initialization errors with JEditorPane
        }
    }
    
    private void displayCategoryDetails(RedisCommandCategory category) {
        try {
            StringBuilder html = new StringBuilder();
            html.append("<html><body style='font-family: sans-serif; font-size: 12px;'>");
            
            // Category name
            html.append("<h2 style='color: #005CC5;'>").append(category.getName()).append("</h2>");
            
            // Description
            if (category.getDescription() != null && !category.getDescription().isEmpty()) {
                html.append("<p>").append(category.getDescription()).append("</p>");
            }
            
            // Commands in this category
            html.append("<h3>Commands</h3>");
            html.append("<ul>");
            for (RedisCommand command : category.getCommands()) {
                html.append("<li><b>").append(command.getName().toUpperCase()).append("</b> - ");
                html.append(command.getDescription()).append("</li>");
            }
            html.append("</ul>");
            
            html.append("</body></html>");
            
            detailsPane.setText(html.toString());
            detailsPane.setCaretPosition(0);
        } catch (Exception e) {
            // Ignore initialization errors with JEditorPane
        }
    }
    
    private void handleCommandSelection() {
        TreePath path = commandTree.getSelectionPath();
        if (path == null) {
            return;
        }
        
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        Object userObject = node.getUserObject();
        
        if (userObject instanceof RedisCommand) {
            RedisCommand command = (RedisCommand) userObject;
            for (Consumer<RedisCommand> listener : commandSelectionListeners) {
                listener.accept(command);
            }
        }
    }
    
    private void copySelectedCommand() {
        TreePath path = commandTree.getSelectionPath();
        if (path == null) {
            return;
        }
        
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        Object userObject = node.getUserObject();
        
        if (userObject instanceof RedisCommand) {
            RedisCommand command = (RedisCommand) userObject;
            CopyPasteManager.getInstance().setContents(new StringSelection(command.getSyntax()));
        }
    }
    
    /**
     * Add a command selection listener
     * @param listener the listener to add
     */
    public void addCommandSelectionListener(Consumer<RedisCommand> listener) {
        commandSelectionListeners.add(listener);
    }
    
    /**
     * Cell renderer for command tree
     */
    private static class CommandTreeCellRenderer extends ColoredTreeCellRenderer {
        @Override
        public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded,
                                         boolean leaf, int row, boolean hasFocus) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();
            
            if (userObject instanceof RedisCommandCategory) {
                RedisCommandCategory category = (RedisCommandCategory) userObject;
                setIcon(AllIcons.Nodes.Folder);
                append(category.getName(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
                
                // Show command count
                int commandCount = category.getCommands().size();
                append(" (" + commandCount + ")", SimpleTextAttributes.GRAYED_ITALIC_ATTRIBUTES);
                
            } else if (userObject instanceof RedisCommand) {
                RedisCommand command = (RedisCommand) userObject;
                setIcon(AllIcons.Nodes.Function);
                append(command.getName().toUpperCase(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                
                // Show brief description
                String desc = command.getDescription();
                if (desc != null && !desc.isEmpty()) {
                    String shortDesc = desc.length() > 60 ? desc.substring(0, 57) + "..." : desc;
                    append(" - " + shortDesc, SimpleTextAttributes.GRAYED_ATTRIBUTES);
                }
                
            } else {
                setIcon(AllIcons.General.Information);
                append(userObject.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }
        }
    }
}
