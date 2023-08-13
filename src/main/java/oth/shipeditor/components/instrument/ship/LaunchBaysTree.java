package oth.shipeditor.components.instrument.ship;

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

    LaunchBaysTree(DefaultMutableTreeNode root) {
        super(root);
        this.baysRoot = root;
        this.initListeners();
    }

    private void initListeners() {
        this.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.getLastSelectedPathComponent();
            if (node == null) return;
            Object nodeObject = node.getUserObject();
            if (!(nodeObject instanceof LaunchPortPoint checked)) return;
            EventBus.publish(new PointSelectQueued(checked));
            EventBus.publish(new ViewerRepaintQueued());
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointSelectedConfirmed checked) {
                if (!(checked.point() instanceof LaunchPortPoint port)) return;
                actOnPortPoint(port, node -> this.setSelectionPath(new TreePath(node.getPath())));
            }
        });
    }

    void addBay(LaunchBay bay) {
        baysRoot.add(new DefaultMutableTreeNode(bay));
    }

    void removeBay(LaunchBay bay) {
        Enumeration<TreeNode> bays = baysRoot.children();
        while (bays.hasMoreElements()) {
            DefaultMutableTreeNode bayNode = (DefaultMutableTreeNode) bays.nextElement();
            LaunchBay bayObject = (LaunchBay) bayNode.getUserObject();
            if (bayObject == bay) {
                baysRoot.remove(bayNode);
            }
        }
    }

    void addPort(LaunchPortPoint point) {
        Enumeration<TreeNode> bays = baysRoot.children();
        while (bays.hasMoreElements()) {
            DefaultMutableTreeNode bayNode = (DefaultMutableTreeNode) bays.nextElement();
            LaunchBay bayObject = (LaunchBay) bayNode.getUserObject();
            if (bayObject == point.getParentBay()) {
                MutableTreeNode portNode = new DefaultMutableTreeNode(point);
                bayNode.add(portNode);
            }
        }
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
            System.out.println(point);
            System.out.println(node);

            // TODO: Everything in this class is wrong. Use the model!
            node.removeFromParent();
            this.repaint();
        });
    }

    void clearRoot() {
        baysRoot.removeAllChildren();
        this.setModel(new DefaultTreeModel(baysRoot));
        this.repaint();
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
        for (int i = 0; i < this.getRowCount(); i++) {
            this.expandRow(i);
        }
    }

}
