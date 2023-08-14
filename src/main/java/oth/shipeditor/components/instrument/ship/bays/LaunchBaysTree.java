package oth.shipeditor.components.instrument.ship.bays;

import lombok.Getter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.PointSelectQueued;
import oth.shipeditor.communication.events.viewer.points.PointSelectedConfirmed;
import oth.shipeditor.components.viewer.entities.bays.LaunchBay;
import oth.shipeditor.components.viewer.entities.bays.LaunchPortPoint;
import oth.shipeditor.components.viewer.painters.points.LaunchBayPainter;

import javax.swing.*;
import javax.swing.tree.*;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 13.08.2023
 */
public class LaunchBaysTree extends JTree {

    @Getter
    private final DefaultMutableTreeNode baysRoot;

    private DefaultTreeModel model;

    LaunchBaysTree(DefaultMutableTreeNode root) {
        super(root);
        this.baysRoot = root;
        this.model = new DefaultTreeModel(baysRoot);
        this.setModel(model);
        this.initListeners();
    }

    private void initListeners() {
        this.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.getLastSelectedPathComponent();
            if (node == null) return;
            Object nodeObject = node.getUserObject();

            if (!(nodeObject instanceof LaunchPortPoint checked)) {
                return;
            }
            EventBus.publish(new PointSelectQueued(checked));
            EventBus.publish(new ViewerRepaintQueued());
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointSelectedConfirmed checked) {
                if (!(checked.point() instanceof LaunchPortPoint port)) return;
                actOnPortPoint(port, node -> {
                    this.expandTree();
                    this.setSelectionPath(new TreePath(node.getPath()));
                    // TODO: finish selection and control panel refresh functionality!
                });
            }
        });
    }

    void addBay(LaunchBay bay) {
        MutableTreeNode newChild = new DefaultMutableTreeNode(bay);
        model.insertNodeInto(newChild, baysRoot, baysRoot.getChildCount());
        model.reload();
        this.expandTree();
    }

    void removeBay(LaunchBay bay) {
        Enumeration<TreeNode> bays = baysRoot.children();
        while (bays.hasMoreElements()) {
            DefaultMutableTreeNode bayNode = (DefaultMutableTreeNode) bays.nextElement();
            LaunchBay bayObject = (LaunchBay) bayNode.getUserObject();
            if (bayObject == bay) {
                model.removeNodeFromParent(bayNode);
                model.reload();
            }
        }
        this.expandTree();
    }

    void addPort(LaunchPortPoint point) {
        Enumeration<TreeNode> bays = baysRoot.children();
        while (bays.hasMoreElements()) {
            DefaultMutableTreeNode bayNode = (DefaultMutableTreeNode) bays.nextElement();
            LaunchBay bayObject = (LaunchBay) bayNode.getUserObject();
            if (bayObject == point.getParentBay()) {
                MutableTreeNode portNode = new DefaultMutableTreeNode(point);

                model.insertNodeInto(portNode, bayNode, bayNode.getChildCount());
                model.reload();
            }
        }
        this.expandTree();
    }

    private void actOnPortPoint(LaunchPortPoint point, Consumer<DefaultMutableTreeNode> action) {
        Enumeration<TreeNode> allNodes = this.baysRoot.depthFirstEnumeration();

        while (allNodes.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) allNodes.nextElement();
            Object nodeObject = node.getUserObject();
            if (nodeObject == point) {
                action.accept(node);
                break;
            }
        }
    }

    void removePort(LaunchPortPoint point) {
        actOnPortPoint(point, node -> {
            model.removeNodeFromParent(node);
            model.reload();
        });
        this.expandTree();
    }

    void clearRoot() {
        baysRoot.removeAllChildren();
        this.model = new DefaultTreeModel(baysRoot);
        this.setModel(model);
        this.model.reload();
        this.repaint();
    }

    private void expandTree() {
        for (int i = 0; i < this.getRowCount(); i++) {
            this.expandRow(i);
        }
    }

    void repopulateTree(LaunchBayPainter bayPainter) {
        final List<LaunchBay> bays = bayPainter.getBaysList();
        for (LaunchBay bay : bays) {
            this.addBay(bay);
            final List<LaunchPortPoint> ports = bay.getPortPoints();
            for (LaunchPortPoint port : ports) {
                this.addPort(port);
            }
        }
        this.expandTree();
    }

}
