package oth.shipeditor.components.instrument.ship.bays;

import lombok.Getter;
import org.kordamp.ikonli.boxicons.BoxiconsSolid;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.PointSelectQueued;
import oth.shipeditor.communication.events.viewer.points.PointSelectedConfirmed;
import oth.shipeditor.components.viewer.entities.bays.LaunchBay;
import oth.shipeditor.components.viewer.entities.bays.LaunchPortPoint;
import oth.shipeditor.components.viewer.entities.weapon.SlotPoint;
import oth.shipeditor.components.viewer.painters.points.ship.LaunchBayPainter;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
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

    private final Consumer<SlotPoint> infoPanelRefresh;

    LaunchBaysTree(DefaultMutableTreeNode root, Consumer<SlotPoint> selectAction) {
        super(root);
        this.baysRoot = root;
        this.infoPanelRefresh = selectAction;
        this.model = new DefaultTreeModel(baysRoot);
        this.setModel(model);
        this.setCellRenderer(new BaysTreeCellRenderer());
        this.initListeners();
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    private void initListeners() {
        this.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.getLastSelectedPathComponent();
            if (node == null) return;
            Object nodeObject = node.getUserObject();
            if (nodeObject instanceof LaunchBay checked) {
                List<LaunchPortPoint> portPoints = checked.getPortPoints();
                LaunchPortPoint firstChild = portPoints.get(0);
                EventBus.publish(new PointSelectQueued(firstChild));
                EventBus.publish(new ViewerRepaintQueued());
                infoPanelRefresh.accept(firstChild);
            } else if (nodeObject instanceof LaunchPortPoint checked) {
                EventBus.publish(new PointSelectQueued(checked));
                EventBus.publish(new ViewerRepaintQueued());
                infoPanelRefresh.accept(checked);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointSelectedConfirmed checked) {
                if (!(checked.point() instanceof LaunchPortPoint port)) return;
                actOnPortPoint(port, node -> {
                    this.expandTree();
                    this.setSelectionPath(new TreePath(node.getPath()));
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
        this.reloadModel();
    }

    void reloadModel() {
        this.model.reload();
        this.expandTree();
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

    private static class BaysTreeCellRenderer extends DefaultTreeCellRenderer {

        @SuppressWarnings({"ParameterHidesMemberVariable", "ChainOfInstanceofChecks"})
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                                                      boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            Object object = ((DefaultMutableTreeNode) value).getUserObject();
            if (object instanceof LaunchBay checked) {
                setIcon(FontIcon.of(BoxiconsSolid.CHEVRON_UP_SQUARE, 16, Color.LIGHT_GRAY));
                setText(checked.getId());
            } else if (object instanceof LaunchPortPoint checked && leaf) {
                setIcon(FontIcon.of(BoxiconsSolid.CHEVRON_UP, 16, Color.LIGHT_GRAY));
                setText(checked.getIndexToDisplay());
            }

            return this;
        }

    }

}
