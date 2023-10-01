package oth.shipeditor.utility.components.containers.trees;

import com.formdev.flatlaf.ui.FlatTreeUI;
import com.formdev.flatlaf.ui.FlatUIUtils;
import oth.shipeditor.Main;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 24.09.2023
 */
public class DynamicWidthTreeUI extends FlatTreeUI {

    private boolean leftToRight;

    public void installUI(JComponent c) {
        if (c == null) {
            throw new NullPointerException("Null component passed to BasicTreeUI.installUI()!");
        }
        JTree parentTree = (JTree) c;
        super.installUI(c);
    }

    protected void prepareForUIInstall() {
        super.prepareForUIInstall();
        ComponentOrientation componentOrientation = tree.getComponentOrientation();
        leftToRight = componentOrientation.isLeftToRight();
        Container parent = tree.getParent();
    }

    @Override
    public void updateSize() {
        super.updateSize();
    }

    @SuppressWarnings("ParameterHidesMemberVariable")
    @Override
    public Rectangle getPathBounds(JTree tree, TreePath path) {
        if(tree != null && treeState != null) {
            Insets insets = tree.getInsets();
            Rectangle bounds = new Rectangle();
            bounds = treeState.getBounds(path, bounds);
            if (bounds != null) {
                int treeWidth = tree.getWidth();
                if (leftToRight) {
                    bounds.x += insets.left;
                } else {

                    bounds.x = treeWidth - (bounds.x + bounds.width) - insets.right;
                }
                bounds.y += insets.top;
                bounds.width = treeWidth - bounds.x - 4;
            }
            return bounds;
        }
        return null;
    }

    Rectangle getPathBoundsForTree(JTree inputTree, TreePath path) {
        Rectangle bounds = super.getPathBounds(inputTree, path);

        boolean widePathToggle = UIManager.getBoolean("FlatLaf.experimental.inputTree.widePathForLocation");
        if (bounds != null && isWideSelection() && widePathToggle) {
            bounds.x = 0;
            bounds.width = inputTree.getWidth();
        }
        return bounds;
    }

    @SuppressWarnings("OverlyComplexMethod")
    @Override
    public void paint( Graphics g, JComponent c ) {
        if (treeState == null) return;

        boolean paintLines = UIManager.getBoolean(Main.TREE_PAINT_LINES);

        Rectangle clipBounds = g.getClipBounds();
        TreePath firstPath = getClosestPathForLocation( tree, 0, clipBounds.y );
        Enumeration<TreePath> visiblePaths = treeState.getVisiblePathsFrom( firstPath );

        if( visiblePaths != null ) {
            Insets insets = tree.getInsets();

            Set<TreePath> verticalLinePaths = paintLines ? new HashSet<>() : null;
            List<Runnable> paintLinesLater = paintLines ? new ArrayList<>() : null;
            List<Runnable> paintExpandControlsLater = paintLines ? new ArrayList<>() : null;

            if( paintLines ) {
                for( TreePath path = firstPath.getParentPath(); path != null; path = path.getParentPath() )
                    verticalLinePaths.add( path );
            }

            Rectangle boundsBuffer = new Rectangle();
            boolean rootVisible = isRootVisible();
            int row = treeState.getRowForPath(firstPath);
            int treeWidth = tree.getWidth();

            // Iterate over visible rows and paint rows, expand control and lines.
            while ( visiblePaths.hasMoreElements()) {
                TreePath path = visiblePaths.nextElement();
                if (path == null) break;
                Rectangle bounds = treeState.getBounds(path, boundsBuffer);
                if (bounds == null) break;

                if (leftToRight) {
                    bounds.x += insets.left;
                } else {
                    bounds.x = treeWidth - insets.right - (bounds.x + bounds.width);
                }
                bounds.y += insets.top;
                bounds.width = treeWidth - bounds.x - 2;

                boolean isLeaf = treeModel.isLeaf(path.getLastPathComponent());
                boolean isExpanded = !isLeaf && treeState.getExpandedState(path);
                boolean hasBeenExpanded = !isLeaf && tree.hasBeenExpanded(path);

                paintRow(g, clipBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf);

                if (paintLines) {
                    TreePath parentPath = path.getParentPath();

                    if (parentPath != null) {
                        verticalLinePaths.add(parentPath);
                    }

                    if (parentPath != null || (rootVisible && row == 0)) {
                        Rectangle bounds2 = new Rectangle(bounds);
                        int row2 = row;
                        paintLinesLater.add(() -> paintHorizontalPartOfLeg(g, clipBounds, insets, bounds2,
                                path, row2, isExpanded, hasBeenExpanded, isLeaf));
                    }
                }
                
                if (shouldPaintExpandControl(path, row, isExpanded, hasBeenExpanded, isLeaf)) {
                    if (paintLines) {
                        // need to paint after painting lines
                        Rectangle bounds2 = new Rectangle(bounds);
                        int row2 = row;
                        paintExpandControlsLater.add(() -> paintExpandControl( g, clipBounds, insets, bounds2,
                                path, row2, isExpanded, hasBeenExpanded, isLeaf ));
                    } else
                        paintExpandControl(g, clipBounds, insets, bounds, path,
                                row, isExpanded, hasBeenExpanded, isLeaf);
                }

                if (bounds.y + bounds.height >= clipBounds.y + clipBounds.height) break;

                row++;
            }

            if (paintLines) {
                Object[] oldRenderingHints = FlatUIUtils.setRenderingHints(g);

                for (Runnable runnable : paintLinesLater) {
                    runnable.run();
                }
                g.setColor(Color.green);
                for (TreePath path : verticalLinePaths) {
                    paintVerticalPartOfLeg(g, clipBounds, insets, path);
                }

                FlatUIUtils.resetRenderingHints(g, oldRenderingHints);

                for (Runnable runnable : paintExpandControlsLater) {
                    runnable.run();
                }
            }
        }
        paintDropLine(g);
        rendererPane.removeAll();
    }

}
