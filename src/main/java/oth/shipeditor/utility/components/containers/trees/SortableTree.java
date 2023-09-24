package oth.shipeditor.utility.components.containers.trees;

import lombok.Getter;
import oth.shipeditor.utility.components.rendering.SortableTreeCellRenderer;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Terai Atsuhiro, refactored by Ontheheavens
 * @since 24.09.2023
 */
@SuppressWarnings("AbstractClassWithOnlyOneDirectInheritor")
public abstract class SortableTree extends JTree {

    @Getter
    private transient DropTarget treeDropTarget;

    @Getter
    private transient DefaultMutableTreeNode dropTargetNode;

    @Getter
    private transient DefaultMutableTreeNode draggedNode;
    @SuppressWarnings("TransientFieldNotInitialized")
    private final transient DragSourceListener listener = new NodeDragSourceListener();

    protected SortableTree(DefaultMutableTreeNode root) {}

    @Override public void updateUI() {
        setCellRenderer(null);
        super.updateUI();
        setCellRenderer(new SortableTreeCellRenderer(this));
        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
                this, DnDConstants.ACTION_MOVE, new NodeDragGestureListener());
        if (Objects.isNull(treeDropTarget)) {
            treeDropTarget = new DropTarget(this, new NodeDropTargetListener());
        }
    }

    /**
     * @param dragged assumed to be a leaf node.
     * @param target assumed to be a branch node and a children of root.
     */
    public abstract void sortTreeModel(DefaultMutableTreeNode dragged, DefaultMutableTreeNode target, int targetIndex);

    private final class NodeDragGestureListener implements DragGestureListener {
        @Override public void dragGestureRecognized(DragGestureEvent dge) {
            Point pt = dge.getDragOrigin();
            TreePath path = getPathForLocation(pt.x, pt.y);
            if (Objects.isNull(path) || Objects.isNull(path.getParentPath())) {
                return;
            }
            draggedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (draggedNode instanceof DefaultMutableTreeNode mutableTreeNode && mutableTreeNode.isLeaf()) {
                Transferable trans = new TreeNodeTransferable(mutableTreeNode);
                DragSource.getDefaultDragSource().startDrag(dge,
                        Cursor.getDefaultCursor(), trans, listener);
            }
        }
    }

    private static class NodeDragSourceListener extends DragSourceAdapter {

        @Override public void dragEnter(DragSourceDragEvent dsde) {
            DragSourceContext sourceContext = dsde.getDragSourceContext();
            sourceContext.setCursor(DragSource.DefaultMoveDrop);
        }

        @Override public void dragExit(DragSourceEvent dse) {
            DragSourceContext sourceContext = dse.getDragSourceContext();
            sourceContext.setCursor(DragSource.DefaultMoveNoDrop);
        }

    }

    private static class TreeNodeTransferable implements Transferable {
        static final String NAME = "Transferable";
        private static final String MIME_TYPE = DataFlavor.javaJVMLocalObjectMimeType;
        private static final DataFlavor FLAVOR = new DataFlavor(MIME_TYPE, NAME);
        private final Object object;

        TreeNodeTransferable(Object o) {
            object = o;
        }

        @Override public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (isDataFlavorSupported(flavor)) {
                return object;
            } else {
                throw new UnsupportedFlavorException(flavor);
            }
        }

        @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
            return NAME.equals(flavor.getHumanPresentableName());
        }

        @Override public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] {FLAVOR};
        }
    }

    private final class NodeDropTargetListener extends DropTargetAdapter {

        @SuppressWarnings("MethodWithMultipleReturnPoints")
        @Override public void dragOver(DropTargetDragEvent dtde) {
            DataFlavor[] f = dtde.getCurrentDataFlavors();
            boolean isSupported = TreeNodeTransferable.NAME.equals(f[0].getHumanPresentableName());
            if (!isSupported) {
                // This DataFlavor is not supported(e.g. files from the desktop).
                rejectDrag(dtde);
                return;
            }

            // Figure out which cell it's over, can't drag to self.
            Point pt = dtde.getLocation();
            TreePath path = getPathForLocation(pt.x, pt.y);
            if (Objects.isNull(path)) {
                // Dropped into the non-node locations(e.g. margin area of JTree).
                rejectDrag(dtde);
                return;
            }
            Object draggingNode = Optional.ofNullable(getSelectionPath())
                    .map(TreePath::getLastPathComponent).orElse(null);
            DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) path.getLastPathComponent();
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) targetNode.getParent();
            if (targetNode.isRoot()) {
                rejectDrag(dtde);
                return;
            }
            if (parent instanceof DefaultMutableTreeNode ancestor && draggingNode instanceof TreeNode) {
                List<TreeNode> treeNodes = Arrays.asList(ancestor.getPath());
                if (treeNodes.contains(draggingNode)) {
                    rejectDrag(dtde);
                    return;
                }
            }
            dropTargetNode = targetNode;
            dtde.acceptDrag(dtde.getDropAction());
            repaint();
        }

        @Override public void drop(DropTargetDropEvent dtde) {
            Object draggingObject = Optional.ofNullable(getSelectionPath())
                    .map(TreePath::getLastPathComponent).orElse(null);
            Point pt = dtde.getLocation();
            TreePath path = getPathForLocation(pt.x, pt.y);
            if (Objects.isNull(path) || !(draggingObject instanceof MutableTreeNode draggingNode)) {
                dtde.dropComplete(false);
                return;
            }
            MutableTreeNode targetNode = (MutableTreeNode) path.getLastPathComponent();
            if (targetNode.equals(draggingNode)) {
                // Cannot move the node to the node itself.
                dtde.dropComplete(false);
                return;
            }
            dtde.acceptDrop(DnDConstants.ACTION_MOVE);

            DefaultTreeModel model = (DefaultTreeModel) getModel();
            model.removeNodeFromParent(draggingNode);

            TreeNode parent = targetNode.getParent();
            int targetIndex;
            if (parent instanceof MutableTreeNode && targetNode.isLeaf()) {
                targetIndex = parent.getIndex(targetNode);
                model.insertNodeInto(draggingNode, (MutableTreeNode) parent, targetIndex);
            } else {
                targetIndex = targetNode.getChildCount();
                model.insertNodeInto(draggingNode, targetNode, targetIndex);
            }
            dtde.dropComplete(true);
            sortTreeModel(draggedNode, dropTargetNode, targetIndex);

            dropTargetNode = null;
            draggedNode = null;
            repaint();
        }

        private void rejectDrag(DropTargetDragEvent e) {
            e.rejectDrag();
            dropTargetNode = null;
            repaint();
        }
    }

}
