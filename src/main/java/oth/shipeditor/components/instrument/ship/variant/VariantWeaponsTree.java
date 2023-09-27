package oth.shipeditor.components.instrument.ship.variant;

import lombok.Getter;
import oth.shipeditor.utility.components.containers.trees.DynamicWidthTree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * @author Ontheheavens
 * @since 27.09.2023
 */
public class VariantWeaponsTree extends DynamicWidthTree {

    @Getter
    private final DefaultMutableTreeNode rootNode;

    private DefaultTreeModel model;

    protected VariantWeaponsTree(DefaultMutableTreeNode root) {
        super(root);
        this.rootNode = root;
        this.model = new DefaultTreeModel(rootNode);
    }

    @Override
    public void sortTreeModel(DefaultMutableTreeNode dragged, DefaultMutableTreeNode target, int targetIndex) {

    }

}
