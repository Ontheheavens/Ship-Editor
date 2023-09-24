package oth.shipeditor.utility.components.rendering;

import oth.shipeditor.utility.components.containers.trees.SortableTree;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 24.09.2023
 */
public class SortableTreeCellRenderer extends TreePanelCellRenderer {

    private final SortableTree sortableTree;
    private boolean isTargetNode;
    private boolean isTargetNodeLeaf;

    public SortableTreeCellRenderer(SortableTree tree) {
        this.sortableTree = tree;
    }

    @SuppressWarnings("ParameterHidesMemberVariable")
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
                                                  boolean expanded, boolean leaf,
                                                  int row, boolean hasFocus) {
        if (value instanceof TreeNode) {
            isTargetNode = value.equals(sortableTree.getDropTargetNode());
            isTargetNodeLeaf = isTargetNode && ((TreeNode) value).isLeaf();
        }
        return super.getTreeCellRendererComponent(
                tree, value, selected, expanded, leaf, row, hasFocus);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (isTargetNode) {
            g.setColor(Color.BLACK);
            if (isTargetNodeLeaf) {
                g.drawLine(0, 0, getSize().width, 0);
            }
            else {
                g.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
            }
        }
    }

}
