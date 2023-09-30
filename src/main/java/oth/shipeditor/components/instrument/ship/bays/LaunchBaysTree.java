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
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.painters.points.ship.LaunchBayPainter;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.components.containers.trees.DynamicWidthTree;
import oth.shipeditor.utility.components.containers.trees.SortableTree;
import oth.shipeditor.utility.components.rendering.SortableTreeCellRenderer;
import oth.shipeditor.utility.overseers.StaticController;

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
public class LaunchBaysTree extends DynamicWidthTree {

    @Getter
    private final DefaultMutableTreeNode baysRoot;

    private DefaultTreeModel model;

    private final Consumer<SlotPoint> infoPanelRefresh;

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    LaunchBaysTree(DefaultMutableTreeNode root, Consumer<SlotPoint> selectAction) {
        super(root);
        this.baysRoot = root;
        this.infoPanelRefresh = selectAction;
        this.model = new DefaultTreeModel(baysRoot);
        this.setModel(model);
        this.setCellRenderer(new BaysTreeCellRenderer(this));
        this.initListeners();
    }

    private void initListeners() {
        this.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.getLastSelectedPathComponent();
            if (node == null) return;
            Object nodeObject = node.getUserObject();
            if (nodeObject instanceof LaunchPortPoint checked) {
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

    void insertBay(LaunchBay bay, int index) {
        MutableTreeNode newChild = new DefaultMutableTreeNode(bay);
        model.insertNodeInto(newChild, baysRoot, index);
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

    @Override
    public MutableTreeNode handleAdditionToRoot(MutableTreeNode dragged) {
        if (dragged instanceof DefaultMutableTreeNode treeNode &&
                treeNode.getUserObject() instanceof LaunchPortPoint port) {
            ShipLayer layer = (ShipLayer) StaticController.getActiveLayer();
            var shipPainter = layer.getPainter();
            var bayPainter = shipPainter.getBayPainter();
            LaunchBay newBay = bayPainter.transferPointToNewBay(port);

            MutableTreeNode bayNode = new DefaultMutableTreeNode(newBay);
            model.insertNodeInto(bayNode, baysRoot, baysRoot.getChildCount());
            return bayNode;
        }
        return null;
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    @Override
    public void sortTreeModel(DefaultMutableTreeNode dragged, DefaultMutableTreeNode target, int targetIndex) {
        LaunchBay targetBay = null;
        if (target.getUserObject() instanceof LaunchBay checkedBay) {
            targetBay = checkedBay;
        } else if (target.getUserObject() instanceof LaunchPortPoint checkedPort) {
            targetBay = checkedPort.getParentBay();
        }
        if (targetBay == null) {
            throw new IllegalStateException("Confirmed port point drop with illegal destination!");
        }
        LaunchPortPoint portPoint = (LaunchPortPoint) dragged.getUserObject();

        EditDispatch.postLaunchPortsRearranged(portPoint, targetBay, targetIndex);
    }

    private static final class BaysTreeCellRenderer extends SortableTreeCellRenderer {

        private final JLabel sizeLabel;

        private final JLabel positionLabel;

        private BaysTreeCellRenderer(SortableTree tree) {
            super(tree);
            var leftContainer = this.getLeftContainer();
            sizeLabel = new JLabel();

            leftContainer.removeAll();
            leftContainer.add(getIconLabel());
            leftContainer.add(sizeLabel);
            leftContainer.add(getTextLabel());

            var rightContainer = this.getRightContainer();
            rightContainer.setLayout(new FlowLayout(FlowLayout.TRAILING, 0, 0));
            this.positionLabel = new JLabel();
            rightContainer.add(positionLabel);
        }

        @SuppressWarnings({"ParameterHidesMemberVariable", "ChainOfInstanceofChecks"})
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
                                                      boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            Object object = ((DefaultMutableTreeNode) value).getUserObject();
            JLabel iconLabel = getIconLabel();
            JLabel textLabel = getTextLabel();
            sizeLabel.setIcon(null);
            positionLabel.setText("");
            if (object instanceof LaunchBay checked) {
                WeaponSize baySize = checked.getWeaponSize();

                iconLabel.setIcon(FontIcon.of(BoxiconsSolid.CHEVRON_UP_SQUARE, 16, Color.LIGHT_GRAY));
                sizeLabel.setIcon(baySize.getIcon());
                textLabel.setText(checked.getId());
            } else if (object instanceof LaunchPortPoint checked && leaf) {
                iconLabel.setIcon(FontIcon.of(BoxiconsSolid.CHEVRON_UP, 16, Color.LIGHT_GRAY));

                String index = checked.getIndexToDisplay() + ":";
                String position = checked.getPositionText();
                textLabel.setText(index);
                positionLabel.setText(position);
            } else {
                textLabel.setText(" " + value);
            }

            return this;
        }

    }

}
