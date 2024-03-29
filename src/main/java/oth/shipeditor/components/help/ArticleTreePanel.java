package oth.shipeditor.components.help;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.GameDataPanelResized;
import oth.shipeditor.components.datafiles.trees.DataTreePanel;
import oth.shipeditor.components.datafiles.OpenDataTarget;
import oth.shipeditor.components.help.parts.ArticlePart;
import oth.shipeditor.utility.components.containers.TextScrollPanel;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 06.08.2023
 */
class ArticleTreePanel extends DataTreePanel {

    private final Runnable reload;

    ArticleTreePanel(Runnable reloadAction) {
        super("Articles");
        this.reload = reloadAction;
    }

    @Override
    public void reload() {
        reload.run();
    }

    @Override
    protected JPanel createTopPanel() {
        return null;
    }

    @Override
    protected void initTreePanelListeners(JPanel passedTreePanel) {
        this.initComponentListeners();
    }

    @Override
    protected float getSplitterResizeWeight() {
        return 0;
    }

    private void initComponentListeners() {
        JTree tree = getTree();
        tree.addTreeSelectionListener(e -> {
            TreePath selectedNode = e.getNewLeadSelectionPath();
            if (selectedNode == null) return;
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedNode.getLastPathComponent();
            if (node.getUserObject() instanceof HelpArticle checked) {
                updateEntryPanel(checked);
                EventBus.publish(new GameDataPanelResized(this.getMinimumSize()));
            }
        });
    }

    private void updateEntryPanel(HelpArticle selected) {
        JPanel rightPanel = getRightPanel();
        rightPanel.removeAll();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));

        JPanel contentContainer = new TextScrollPanel(new FlowLayout());
        contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.PAGE_AXIS));

        List<ArticlePart> articleParts = selected.getArticleParts();
        for (ArticlePart articlePart : articleParts) {
            contentContainer.add(articlePart.createContent());
        }
        JScrollPane scrollContainer = new JScrollPane(contentContainer);
        scrollContainer.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        JScrollBar verticalScrollBar = scrollContainer.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(24);

        rightPanel.add(scrollContainer);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    @Override
    protected Class<?> getEntryClass() {
        return HelpArticle.class;
    }

    @Override
    protected String getTooltipForEntry(Object entry) {
        return null;
    }

    @Override
    protected void openEntryPath(OpenDataTarget target) {
        throw new IllegalArgumentException("File context menus are inapplicable to Help panel!");
    }

}
