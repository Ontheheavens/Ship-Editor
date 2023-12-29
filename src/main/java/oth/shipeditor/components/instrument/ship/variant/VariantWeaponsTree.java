package oth.shipeditor.components.instrument.ship.variant;

import com.formdev.flatlaf.ui.FlatLineBorder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.boxicons.BoxiconsSolid;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.SelectWeaponDataEntry;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.PointSelectQueued;
import oth.shipeditor.components.datafiles.entities.CSVEntry;
import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;
import oth.shipeditor.components.viewer.entities.WorldPoint;
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
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.containers.trees.DynamicWidthTree;
import oth.shipeditor.utility.components.containers.trees.SortableTree;
import oth.shipeditor.utility.components.rendering.CustomTreeNode;
import oth.shipeditor.utility.components.rendering.SortableTreeCellRenderer;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.text.StringValues;
import oth.shipeditor.utility.themes.Themes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
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

    private CustomTreeNode cachedSelectedNode;

    @Getter @Setter
    private WeaponSlotPainter slotPainter;

    private DefaultTreeModel model;

    private final Consumer<InstalledFeature> selectionAction;

    VariantWeaponsTree(CustomTreeNode root, Consumer<InstalledFeature> selector) {
        super(root);
        this.selectionAction = selector;
        this.rootNode = root;
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
                selectionAction.accept(checked);
            }
        });
        this.addMouseListener(new ContextMenuListener());
    }

    void selectNode(WorldPoint point) {
        this.clearSelection();
        if (point instanceof WeaponSlotPoint slotPoint) {
            actOnNodeByPoint(slotPoint, node -> {
                this.expandTree();
                TreePath path = new TreePath(node.getPath());
                this.setSelectionPath(path);
                this.scrollPathToVisible(path);
            });
        }
    }

    private void addWeaponGroup(FittedWeaponGroup group) {
        MutableTreeNode newChild = new CustomTreeNode(group);
        model.insertNodeInto(newChild, rootNode, rootNode.getChildCount());
    }

    void insertWeaponGroup(FittedWeaponGroup group, int index) {
        MutableTreeNode newChild = new CustomTreeNode(group);
        model.insertNodeInto(newChild, rootNode, index);
        model.reload();
    }

    @SuppressWarnings("MethodOnlyUsedFromInnerClass")
    private void removeWeaponGroup(FittedWeaponGroup group) {
        Enumeration<TreeNode> groups = rootNode.children();
        while (groups.hasMoreElements()) {
            CustomTreeNode groupNode = (CustomTreeNode) groups.nextElement();
            FittedWeaponGroup groupObject = (FittedWeaponGroup) groupNode.getUserObject();
            if (groupObject == group) {
                // Bulk uninstalls are all undoable actions.
                group.uninstallAll();
                ShipVariant groupParent = group.getParent();
                groupParent.removeWeaponGroup(group);
                model.reload();
            }
        }
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
                Object data = customTreeNode.getUserObject();
                if (data instanceof InstalledFeature feature) {
                    CSVEntry dataEntry = feature.getDataEntry();
                    return dataEntry.getMultilineTooltip();
                } else {
                    String id = customTreeNode.getFirstLineTip();
                    String tip = customTreeNode.getSecondLineTip();
                    return Utility.getWithLinebreaks(id, tip);
                }

            }
        }
        return null;
    }

    void repopulateTree(ShipVariant variant, ShipLayer layer) {
        ShipPainter shipPainter = layer.getPainter();
        variant.ensureBuiltInsSync(shipPainter);

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
            List<FittedWeaponGroup> weaponGroups = variant.getWeaponGroups();
            if (weaponGroups.size() >= 7) {
                return null;
            }

            FittedWeaponGroup weaponGroup = new FittedWeaponGroup(variant, false, FireMode.LINKED);

            weaponGroups.add(weaponGroup);

            MutableTreeNode groupNode = new CustomTreeNode(weaponGroup);
            model.insertNodeInto(groupNode, rootNode, rootNode.getChildCount());
            return groupNode;
        }
        return null;
    }

    @Override
    public boolean isNodeDragValid(DefaultMutableTreeNode dragged) {
        return dragged.getUserObject() instanceof InstalledFeature;
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    @Override
    public void sortTreeModel(DefaultMutableTreeNode dragged, DefaultMutableTreeNode target, int targetIndex) {
        FittedWeaponGroup targetGroup = null;
        if (target.getUserObject() instanceof FittedWeaponGroup checkedGroup) {
            targetGroup = checkedGroup;
        } else if (target.getUserObject() instanceof InstalledFeature checkedFeature) {
            targetGroup = checkedFeature.getParentGroup();
        }
        if (targetGroup == null) {
            throw new IllegalStateException("Confirmed feature drop with illegal destination!");
        }
        InstalledFeature feature = (InstalledFeature) dragged.getUserObject();

        EditDispatch.postWeaponGroupsRearranged(feature, targetGroup, targetIndex);

        this.reloadModel();
    }

    private final class ContextMenuListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if(e.getButton() == MouseEvent.BUTTON3){
                Point point = e.getPoint();
                TreePath pathForLocation = VariantWeaponsTree.this.getPathForLocation(point.x, point.y);
                if(pathForLocation != null){
                    cachedSelectedNode = (CustomTreeNode) pathForLocation.getLastPathComponent();
                    showMenu(pathForLocation, e);
                } else{
                    cachedSelectedNode = null;
                }
            }
            super.mousePressed(e);
        }

        private void showMenu(TreePath pathForLocation, MouseEvent e) {
            CustomTreeNode selectedNode = VariantWeaponsTree.this.cachedSelectedNode;
            Object nodeUserObject = selectedNode.getUserObject();
            JPopupMenu contextMenu = createContextMenu(nodeUserObject);
            if (contextMenu != null) {
                VariantWeaponsTree.this.setSelectionPath(pathForLocation);
                contextMenu.show(VariantWeaponsTree.this, e.getPoint().x, e.getPoint().y);
            }
        }

        private JPopupMenu createContextMenu(Object nodeUserObject) {
            JPopupMenu contextMenu = null;
            switch (nodeUserObject) {
                case FittedWeaponGroup weaponGroup -> {
                    contextMenu = new JPopupMenu();

                    JMenu modeSubmenu = getModeSubmenu(weaponGroup);
                    contextMenu.add(modeSubmenu);

                    JCheckBoxMenuItem autofire = new JCheckBoxMenuItem("Toggle autofire");
                    autofire.setSelected(weaponGroup.isAutofire());
                    autofire.addActionListener(e -> {
                        weaponGroup.setAutofire(autofire.isSelected());
                        VariantWeaponsTree.this.repaint();
                    });
                    contextMenu.add(autofire);

                    contextMenu.addSeparator();

                    JMenuItem removeGroup = new JMenuItem("Remove weapon group");
                    removeGroup.addActionListener(e -> removeWeaponGroup(weaponGroup));
                    contextMenu.add(removeGroup);
                }
                case InstalledFeature feature -> {
                    contextMenu = new JPopupMenu();

                    if (!feature.isContainedInBuiltIns()) {
                        JMenuItem uninstallFeature = new JMenuItem(StringValues.UNINSTALL_FEATURE);
                        uninstallFeature.addActionListener(e -> {
                            var group = feature.getParentGroup();
                            EditDispatch.postFeatureUninstalled(group.getWeapons(), feature.getSlotID(),
                                    feature, null);
                        });
                        contextMenu.add(uninstallFeature);
                    }

                    JMenuItem selectEntry = new JMenuItem(StringValues.SELECT_WEAPON_ENTRY);
                    selectEntry.addActionListener(event ->  {
                        CSVEntry dataEntry = feature.getDataEntry();
                        if (dataEntry instanceof WeaponCSVEntry weaponEntry) {
                            EventBus.publish(new SelectWeaponDataEntry(weaponEntry));
                        }
                    });
                    contextMenu.add(selectEntry);
                }
                default -> {}
            }

            return contextMenu;
        }

        private JMenu getModeSubmenu(FittedWeaponGroup weaponGroup) {
            JMenu modeSubmenu = new JMenu("Firing mode");

            JMenuItem linkedMode = new JRadioButtonMenuItem("Mode: Linked");
            linkedMode.setSelected(weaponGroup.getMode() == FireMode.LINKED);
            linkedMode.addActionListener(e -> {
                weaponGroup.setMode(FireMode.LINKED);
                VariantWeaponsTree.this.repaint();
            });
            modeSubmenu.add(linkedMode);

            JMenuItem alternatingMode = new JRadioButtonMenuItem("Mode: Alternating");
            alternatingMode.setSelected(weaponGroup.getMode() == FireMode.ALTERNATING);
            alternatingMode.addActionListener(e -> {
                weaponGroup.setMode(FireMode.ALTERNATING);
                VariantWeaponsTree.this.repaint();
            });
            modeSubmenu.add(alternatingMode);
            return modeSubmenu;
        }

    }

    private final class WeaponsTreeCellRenderer extends SortableTreeCellRenderer {

        private final JLabel slotTypeIcon;

        private final JLabel builtInIcon;

        private final JLabel upperRightLabel;

        private final JLabel lowerLeftLabel;

        private final JLabel lowerRightLabel;

        @SuppressWarnings("ThisEscapedInObjectConstruction")
        private WeaponsTreeCellRenderer(SortableTree tree) {
            super(tree);
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            removeAll();

            setFillBackground(true);

            JPanel upperContainer = new JPanel();
            upperContainer.setOpaque(false);
            upperContainer.setLayout(new BoxLayout(upperContainer, BoxLayout.LINE_AXIS));

            slotTypeIcon = new JLabel();
            slotTypeIcon.setOpaque(true);
            slotTypeIcon.setBorder(new FlatLineBorder(new Insets(2, 2, 2, 2), Color.GRAY));
            slotTypeIcon.setBackground(Color.LIGHT_GRAY);

            builtInIcon = new JLabel();

            JLabel textLabel = getTextLabel();
            textLabel.setBorder(new EmptyBorder(0, 4, 0, 0));

            upperRightLabel = new JLabel();

            JPanel leftContainer = getLeftContainer();
            leftContainer.removeAll();
            leftContainer.add(getIconLabel());
            leftContainer.add(slotTypeIcon);
            leftContainer.add(builtInIcon);
            leftContainer.add(textLabel);

            JPanel rightContainer = getRightContainer();
            rightContainer.add(upperRightLabel);

            ComponentUtilities.layoutAsOpposites(upperContainer, leftContainer, rightContainer, 4);

            this.add(upperContainer);

            JPanel lowerContainer = new JPanel();
            lowerContainer.setOpaque(false);
            lowerContainer.setLayout(new BoxLayout(lowerContainer, BoxLayout.LINE_AXIS));

            lowerLeftLabel = new JLabel();
            lowerRightLabel = new JLabel();

            ComponentUtilities.layoutAsOpposites(lowerContainer, lowerLeftLabel, lowerRightLabel, 4);

            this.add(lowerContainer);
        }

        @Override
        public void setForeground(Color fg) {
            super.setForeground(fg);
            if (upperRightLabel != null) {
                upperRightLabel.setForeground(fg);
            }
            if (lowerLeftLabel != null) {
                lowerLeftLabel.setForeground(fg);
            }
            if (lowerRightLabel != null) {
                lowerRightLabel.setForeground(fg);
            }
        }

        private void handleGroupAppearance(CustomTreeNode treeNode, FittedWeaponGroup weaponGroup) {
            JLabel iconLabel = getIconLabel();
            JLabel textLabel = getTextLabel();

            Color iconColor = Themes.getIconColor();
            iconLabel.setIcon(FontIcon.of(BoxiconsRegular.CROSSHAIR, 16, iconColor));
            iconLabel.setBorder(new EmptyBorder(0, 0, 0, 2));
            textLabel.setText("Weapon Group " + weaponGroup.getIndexToDisplay());

            setBackgroundNonSelectionColor(Themes.getPanelDarkColor());

            if (weaponGroup.isAutofire()) {
                slotTypeIcon.setIcon(FontIcon.of(FluentUiRegularAL.DEVELOPER_BOARD_24,
                        18, iconColor));
                slotTypeIcon.setVisible(true);

                treeNode.setFirstLineTip("Autofire: ON");

                slotTypeIcon.setOpaque(false);
                slotTypeIcon.setBackground(null);
                slotTypeIcon.setBorder(new EmptyBorder(0, 0, 0, 0));
            }
            Icon mode;
            if (weaponGroup.getMode() == FireMode.ALTERNATING) {
                mode = FontIcon.of(BoxiconsRegular.SLIDER, 18, iconColor);
                treeNode.setSecondLineTip("Firing mode: ALTERNATING");
            } else {
                mode = FontIcon.of(BoxiconsRegular.POLL, 18, iconColor);
                treeNode.setSecondLineTip("Firing mode: LINKED");
            }
            builtInIcon.setIcon(mode);
            builtInIcon.setVisible(true);

            textLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
        }

        private void handleFeatureAppearance(CustomTreeNode treeNode, InstalledFeature feature) {
            JLabel iconLabel = getIconLabel();
            JLabel textLabel = getTextLabel();

            var slot = getSlotPoint(feature);
            CSVEntry dataEntry = feature.getDataEntry();

            lowerRightLabel.setText(feature.getName());
            treeNode.setFirstLineTip(StringValues.WEAPON_ID + feature.getFeatureID());

            if (feature.isContainedInBuiltIns()) {
                builtInIcon.setIcon(FontIcon.of(BoxiconsSolid.LOCK_ALT, 16, Themes.getIconColor()));
                builtInIcon.setVisible(true);

                treeNode.setSecondLineTip("Built-in: locked in variant");
                textLabel.setBorder(new EmptyBorder(0, 1, 0, 0));
            }

            if (slot == null) {
                setForeground(Themes.getReddishFontColor());
                iconLabel.setIcon(FontIcon.of(BoxiconsRegular.ERROR, 18, Color.RED));
                iconLabel.setOpaque(false);
                iconLabel.setBorder(new EmptyBorder(1, 0, 0, 0));

                textLabel.setBorder(new EmptyBorder(0, 0, 0, 0));

                treeNode.setSecondLineTip(StringValues.INVALIDATED_SLOT_NOT_FOUND);
            } else {
                slotTypeIcon.setVisible(true);
                WeaponType weaponType = slot.getWeaponType();
                Icon color = ComponentUtilities.createIconFromColor(weaponType.getColor(), 10, 10);
                slotTypeIcon.setIcon(color);

                WeaponSize weaponSize = slot.getWeaponSize();
                iconLabel.setIcon(weaponSize.getIcon());

                if (!slot.canFit(feature)) {
                    setForeground(Themes.getReddishFontColor());
                    String weaponUnfitForSlot = StringValues.INVALIDATED_WEAPON_UNFIT_FOR_SLOT;
                    if (feature.isContainedInBuiltIns()) {
                        weaponUnfitForSlot = Utility.getWithLinebreaks(weaponUnfitForSlot,
                                "Built-in: will appear in game");
                    }
                    treeNode.setSecondLineTip(weaponUnfitForSlot);
                }
            }
            textLabel.setText(feature.getSlotID());
            upperRightLabel.setText("OP: " + feature.getOPCost());
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

            iconLabel.setBorder(new EmptyBorder(0, 0, 0, 0));

            slotTypeIcon.setOpaque(true);
            slotTypeIcon.setBorder(new FlatLineBorder(new Insets(2, 2, 2, 2), Color.GRAY));
            slotTypeIcon.setBackground(Color.LIGHT_GRAY);

            slotTypeIcon.setIcon(null);
            slotTypeIcon.setVisible(false);

            builtInIcon.setIcon(null);
            builtInIcon.setVisible(false);

            upperRightLabel.setText("");
            lowerLeftLabel.setText("");
            lowerRightLabel.setText("");

            setBackgroundNonSelectionColor(Themes.getListBackgroundColor());

            treeNode.setSecondLineTip(null);
            treeNode.setFirstLineTip(null);
            textLabel.setBorder(new EmptyBorder(0, 4, 0, 0));
            if (object instanceof FittedWeaponGroup checked) {
                this.handleGroupAppearance(treeNode, checked);
            } else if (object instanceof InstalledFeature checked && leaf) {
                this.handleFeatureAppearance(treeNode, checked);
            } else {
                textLabel.setText(" " + value);
            }

            return this;
        }

    }

}
