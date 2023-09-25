package oth.shipeditor.utility.components.containers.trees;

import javax.swing.plaf.TreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 24.09.2023
 */
@SuppressWarnings("AbstractClassWithOnlyOneDirectInheritor")
public abstract class DynamicWidthTree extends SortableTree {

    protected DynamicWidthTree(DefaultMutableTreeNode root) {
        super(root);
    }

    @Override
    public TreePath getPathForLocation(int x, int y) {
        TreePath closestPath = getClosestPathForLocation(x, y);

        if (closestPath != null) {
            Rectangle pathBounds = getPathBoundsForTree(closestPath);
            if(DynamicWidthTree.checkPathBounds(x, y, pathBounds)) {
                return closestPath;
            }
        }
        return null;
    }

    private static boolean checkPathBounds(int x, int y, Rectangle pathBounds) {
        return pathBounds != null && x >= pathBounds.x && x < (pathBounds.x + pathBounds.width) &&
                y >= pathBounds.y && y < (pathBounds.y + pathBounds.height);
    }

    private Rectangle getPathBoundsForTree(TreePath path) {
        if (getUI() instanceof DynamicWidthTreeUI checkedUI) {
            return checkedUI.getPathBoundsForTree(this, path);
        } else {
            TreeUI treeUI = getUI();
            return treeUI.getPathBounds(this, path);
        }
    }

}
