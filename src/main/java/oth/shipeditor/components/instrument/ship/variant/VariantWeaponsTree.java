package oth.shipeditor.components.instrument.ship.variant;

import com.formdev.flatlaf.ui.FlatLineBorder;
import lombok.Getter;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.PointSelectQueued;
import oth.shipeditor.communication.events.viewer.points.PointSelectedConfirmed;
import oth.shipeditor.components.viewer.entities.weapon.SlotData;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.components.viewer.painters.points.ship.WeaponSlotPainter;
import oth.shipeditor.components.viewer.painters.points.ship.features.FireMode;
import oth.shipeditor.components.viewer.painters.points.ship.features.FittedWeaponGroup;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.containers.trees.DynamicWidthTree;
import oth.shipeditor.utility.components.containers.trees.SortableTree;
import oth.shipeditor.utility.components.rendering.CustomTreeNode;
import oth.shipeditor.utility.components.rendering.SortableTreeCellRenderer;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 27.09.2023
 */
public class VariantWeaponsTree extends DynamicWidthTree {

    @Getter
    private final CustomTreeNode rootNode;

    private final WeaponSlotPainter slotPainter;

    private DefaultTreeModel model;

    VariantWeaponsTree(CustomTreeNode root, WeaponSlotPainter weaponSlotPainter) {
        super(root);
        this.rootNode = root;
        this.slotPainter = weaponSlotPainter;
        this.model = new DefaultTreeModel(rootNode);
        this.setModel(model);
        this.setCellRenderer(new WeaponsTreeCellRenderer(this));
        this.initListeners();
    }

    private WeaponSlotPoint getSlotPoint(InstalledFeature installed) {
        return slotPainter.getSlotByID(installed.getSlotID());
    }

    private void actOnNodeByPoint(SlotData point, Consumer<CustomTreeNode> action) {
        Enumeration<TreeNode> allNodes = this.rootNode.depthFirstEnumeration();

        while (allNodes.hasMoreElements()) {
            CustomTreeNode node = (CustomTreeNode) allNodes.nextElement();
            Object nodeObject = node.getUserObject();
            if (nodeObject instanceof InstalledFeature feature) {
                if (Objects.equals(feature.getSlotID(), point.getId())) {
                    action.accept(node);
                    break;
                }
            }
        }
    }

    private void initListeners() {
        this.addTreeSelectionListener(e -> {
            CustomTreeNode node = (CustomTreeNode) this.getLastSelectedPathComponent();
            if (node == null) return;
            Object nodeObject = node.getUserObject();
            if (nodeObject instanceof InstalledFeature checked) {
                var slot = this.getSlotPoint(checked);
                if (slot != null && !slot.isPointSelected()) {
                    EventBus.publish(new PointSelectQueued(slot));
                    EventBus.publish(new ViewerRepaintQueued());
                }
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointSelectedConfirmed checked) {
                this.clearSelection();
                if (checked.point() instanceof WeaponSlotPoint slotPoint) {
                    actOnNodeByPoint(slotPoint, node -> {
                        this.expandTree();
                        this.setSelectionPath(new TreePath(node.getPath()));
                    });
                }
            }
        });
    }

    private void addWeaponGroup(FittedWeaponGroup group) {
        MutableTreeNode newChild = new CustomTreeNode(group);
        model.insertNodeInto(newChild, rootNode, rootNode.getChildCount());
        model.reload();
        this.expandTree();
    }

    void insertWeaponGroup(FittedWeaponGroup group, int index) {
        MutableTreeNode newChild = new CustomTreeNode(group);
        model.insertNodeInto(newChild, rootNode, index);
        model.reload();
        this.expandTree();
    }

    void removeWeaponGroup(FittedWeaponGroup group) {
        Enumeration<TreeNode> groups = rootNode.children();
        while (groups.hasMoreElements()) {
            CustomTreeNode groupNode = (CustomTreeNode) groups.nextElement();
            FittedWeaponGroup groupObject = (FittedWeaponGroup) groupNode.getUserObject();
            if (groupObject == group) {
                model.removeNodeFromParent(groupNode);
                model.reload();
            }
        }
        this.expandTree();
    }

    private void addWeaponInstall(InstalledFeature feature, FittedWeaponGroup target) {
        Enumeration<TreeNode> groups = rootNode.children();
        while (groups.hasMoreElements()) {
            CustomTreeNode groupNode = (CustomTreeNode) groups.nextElement();
            FittedWeaponGroup groupObject = (FittedWeaponGroup) groupNode.getUserObject();
            if (groupObject == target) {
                MutableTreeNode portNode = new CustomTreeNode(feature);

                model.insertNodeInto(portNode, groupNode, groupNode.getChildCount());
                model.reload();
            }
        }
        this.expandTree();
    }

    void removeWeaponInstall(SlotData point) {
        actOnNodeByPoint(point, node -> {
            model.removeNodeFromParent(node);
            model.reload();
        });
        this.expandTree();
    }

    void clearRoot() {
        rootNode.removeAllChildren();
        this.model = new DefaultTreeModel(rootNode);
        this.setModel(model);
        this.reloadModel();
    }

    private void reloadModel() {
        this.model.reload();
        this.expandTree();
        this.repaint();
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        if (getRowForLocation(event.getX(), event.getY()) == -1)
            return null;
        TreePath currentPath = getPathForLocation(event.getX(), event.getY());
        if (currentPath != null) {
            Object node = currentPath.getLastPathComponent();
            if (node instanceof CustomTreeNode customTreeNode) {
                return customTreeNode.getTip();
            }
        }
        return null;
    }

    void repopulateTree(ShipVariant variant, ShipLayer layer) {
        ShipPainter shipPainter = layer.getPainter();
        variant.ensureBuiltInsAreFitted(shipPainter);

        final var weaponGroups = variant.getWeaponGroups();
        for (FittedWeaponGroup group : weaponGroups) {
            this.addWeaponGroup(group);
            final ListOrderedMap<String, InstalledFeature> weapons = group.getWeapons();
            for (InstalledFeature feature : weapons.valueList()) {
                this.addWeaponInstall(feature, group);
            }
        }
        this.reloadModel();
    }

    @Override
    public MutableTreeNode handleAdditionToRoot(MutableTreeNode dragged) {
        if (dragged instanceof DefaultMutableTreeNode treeNode &&
                treeNode.getUserObject() instanceof InstalledFeature feature) {
            ShipLayer layer = (ShipLayer) StaticController.getActiveLayer();
            var shipPainter = layer.getPainter();
            var variant = shipPainter.getActiveVariant();

            FittedWeaponGroup weaponGroup = new FittedWeaponGroup(variant, false, FireMode.LINKED);
            var fitted = weaponGroup.getWeapons();
            fitted.put(feature.getSlotID(), feature);
            List<FittedWeaponGroup> weaponGroups = variant.getWeaponGroups();
            weaponGroups.add(weaponGroup);

            MutableTreeNode groupNode = new CustomTreeNode(weaponGroup);
            model.insertNodeInto(groupNode, rootNode, rootNode.getChildCount());
            return groupNode;
        }
        return null;
    }

    @Override
    public void sortTreeModel(DefaultMutableTreeNode dragged, DefaultMutableTreeNode target, int targetIndex) {

        this.reloadModel();
    }

    private final class WeaponsTreeCellRenderer extends SortableTreeCellRenderer {

        private final JLabel slotTypeIcon;

        private final JLabel featureIDText;

        private WeaponsTreeCellRenderer(SortableTree tree) {
            super(tree);
            slotTypeIcon = new JLabel();
            slotTypeIcon.setOpaque(true);
            slotTypeIcon.setBorder(new FlatLineBorder(new Insets(2, 2, 2, 2), Color.GRAY));
            slotTypeIcon.setBackground(Color.LIGHT_GRAY);

            JLabel textLabel = getTextLabel();
            textLabel.setBorder(new EmptyBorder(0, 4, 0, 0));

            featureIDText = new JLabel();

            JPanel leftContainer = getLeftContainer();
            leftContainer.removeAll();
            leftContainer.add(getIconLabel());
            leftContainer.add(slotTypeIcon);
            leftContainer.add(textLabel);

            JPanel rightContainer = getRightContainer();
            rightContainer.add(featureIDText);
        }

        @Override
        public void setForeground(Color fg) {
            super.setForeground(fg);
            if (featureIDText != null) {
                featureIDText.setForeground(fg);
            }
        }

        @SuppressWarnings({"ParameterHidesMemberVariable", "ChainOfInstanceofChecks"})
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
                                                      boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            CustomTreeNode treeNode = (CustomTreeNode) value;
            Object object = treeNode.getUserObject();
            JLabel iconLabel = getIconLabel();
            JLabel textLabel = getTextLabel();
            slotTypeIcon.setIcon(null);
            slotTypeIcon.setVisible(false);
            featureIDText.setText("");

            treeNode.setTip(null);
            textLabel.setBorder(new EmptyBorder(0, 4, 0, 0));
            if (object instanceof FittedWeaponGroup checked) {
                iconLabel.setIcon(FontIcon.of(BoxiconsRegular.CROSSHAIR, 16, Color.LIGHT_GRAY));
                textLabel.setText("Weapon Group " + checked.getIndexToDisplay());
            } else if (object instanceof InstalledFeature checked && leaf) {
                var slot = getSlotPoint(checked);
                if (slot == null) {
                    setForeground(Color.RED);
                    iconLabel.setIcon(FontIcon.of(BoxiconsRegular.ERROR, 18, Color.RED));
                    iconLabel.setOpaque(false);
                    iconLabel.setBorder(new EmptyBorder(1, 2, 0, 0));

                    textLabel.setBorder(new EmptyBorder(0, 0, 0, 0));

                    treeNode.setTip(StringValues.INVALIDATED_SLOT_NOT_FOUND);
                } else {
                    slotTypeIcon.setVisible(true);
                    WeaponType weaponType = slot.getWeaponType();
                    Icon color = ComponentUtilities.createIconFromColor(weaponType.getColor(), 10, 10);
                    slotTypeIcon.setIcon(color);

                    WeaponSize weaponSize = slot.getWeaponSize();
                    iconLabel.setIcon(weaponSize.getIcon());

                    if (!slot.canFit(checked)) {
                        setForeground(Color.RED);
                        treeNode.setTip(StringValues.INVALIDATED_WEAPON_UNFIT_FOR_SLOT);
                    }
                }
                textLabel.setText(checked.getSlotID());
                featureIDText.setText(checked.getFeatureID());
            } else {
                textLabel.setText(" " + value);
            }

            return this;
        }

    }

}
