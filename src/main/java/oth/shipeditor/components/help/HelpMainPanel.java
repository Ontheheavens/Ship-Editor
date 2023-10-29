package oth.shipeditor.components.help;

import com.fasterxml.jackson.databind.ObjectMapper;
import oth.shipeditor.parsing.FileUtilities;
import oth.shipeditor.persistence.SettingsManager;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.io.IOException;

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
        File articlesRoot = SettingsManager.getApplicationDirectory().resolve("help").toFile();

        if (!articlesRoot.exists() || !articlesRoot.isDirectory()) return;
        File[] sectionFolders = articlesRoot.listFiles(File::isDirectory);

        if (sectionFolders == null) return;
        for (File sectionFolder : sectionFolders) {
            addArticleSection(sectionFolder);
        }
    }

    private void addArticleSection(File sectionFolder) {
        DefaultMutableTreeNode sectionNode = new DefaultMutableTreeNode(sectionFolder.getName());
        DefaultMutableTreeNode rootNode = articlePanel.getRootNode();
        rootNode.add(sectionNode);

        File[] articleFiles = sectionFolder.listFiles((dir, name) -> name.endsWith(".json"));

        if (articleFiles != null) {
            for (File articleFile : articleFiles) {
                HelpArticle article = this.readArticleFromFile(articleFile);
                sectionNode.add(new DefaultMutableTreeNode(article));
            }
        }
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private HelpArticle readArticleFromFile(File file) {
        ObjectMapper objectMapper = FileUtilities.getConfigured();
        try {
            return objectMapper.readValue(file, HelpArticle.class);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Encountered an error while trying to deserialize file with a help article: " + file.getName(),
                    "Failed to load help article!",
                    JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e);
        }
    }

}
