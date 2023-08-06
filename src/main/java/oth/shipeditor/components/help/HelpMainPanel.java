package oth.shipeditor.components.help;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 06.08.2023
 */
public class HelpMainPanel extends JPanel {

    private final ArticleTreePanel articlePanel;

    public HelpMainPanel() {
        this.setLayout(new BorderLayout());
        articlePanel = new ArticleTreePanel();

        this.populateArticles();

        JTree articlePanelTree = articlePanel.getTree();
        articlePanelTree.expandPath(new TreePath(articlePanel.getRootNode()));

        this.add(articlePanel, BorderLayout.CENTER);
    }

    private void populateArticles() {
        DefaultMutableTreeNode rootNode = articlePanel.getRootNode();

        rootNode.add(Articles.createDevelopmentSection());
    }

}
