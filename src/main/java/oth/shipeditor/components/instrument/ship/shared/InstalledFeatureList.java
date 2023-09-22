package oth.shipeditor.components.instrument.ship.shared;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.SelectWeaponDataEntry;
import oth.shipeditor.communication.events.viewer.points.PointSelectQueued;
import oth.shipeditor.communication.events.viewer.points.PointSelectedConfirmed;
import oth.shipeditor.components.datafiles.entities.CSVEntry;
import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;
import oth.shipeditor.components.viewer.painters.points.ship.WeaponSlotPainter;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.components.containers.SortableList;
import oth.shipeditor.utility.components.rendering.InstalledFeatureCellRenderer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 17.09.2023
 */
public class InstalledFeatureList extends SortableList<InstalledFeature> {

    private boolean propagationBlock;

    @Getter @Setter
    private boolean belongsToBaseHullBuiltIns;

    @Getter
    private final WeaponSlotPainter slotPainter;

    private final Consumer<InstalledFeature> uninstaller;

    private final Consumer<Map<String, InstalledFeature>> sorter;

    public InstalledFeatureList(ListModel<InstalledFeature> dataModel, WeaponSlotPainter painter,
                                Consumer<InstalledFeature> removeAction,
                                Consumer<Map<String, InstalledFeature>> sortAction) {
        super(dataModel);
        this.slotPainter = painter;
        this.uninstaller = removeAction;
        this.sorter = sortAction;
        this.addListSelectionListener(e -> {
            this.actOnSelectedEntry(feature -> {

            });
            if (propagationBlock) {
                propagationBlock = false;
                return;
            }
            this.actOnSelectedEntry(InstalledFeatureList::selectSlotPoint);
        });
        this.addMouseListener(new FeatureContextMenuListener());
        this.setCellRenderer(InstalledFeatureList.createCellRenderer());
        int margin = 3;
        this.setBorder(new EmptyBorder(margin, margin, margin, margin));
        this.initListeners();

        this.setDragEnabled(true);
    }

    private static ListCellRenderer<InstalledFeature> createCellRenderer() {
        return new InstalledFeatureCellRenderer();
    }

    /**
     * This is quite a hack; it is assumed that whatever feature is selected is installed onto
     * existing slot point in active layer.
     */
    private static void selectSlotPoint(InstalledFeature feature) {
        var activeLayer = StaticController.getActiveLayer();
        if (activeLayer instanceof ShipLayer shipLayer) {
            var shipPainter = shipLayer.getPainter();
            if (shipPainter == null || shipPainter.isUninitialized()) return;
            var slotPainter = shipPainter.getWeaponSlotPainter();
            var slotPoint = slotPainter.getSlotByID(feature.getSlotID());
            if (slotPoint == null) return;
            EventBus.publish(new PointSelectQueued(slotPoint));
            var repainter = StaticController.getRepainter();
            repainter.queueViewerRepaint();
        }
    }

    private void initListeners() {
        // TODO: this one is currently not cleaned up from event bus. As it's a UI class, should not be super spammy...
        //  However, keep in mind for future refactors. Difficulty here is, you can't easily get a hold of time
        //  when old feature list is discarded - it's done in Swing internals. Possible solutions are:
        //  Either move listener to outer panel classes or employ WeakHashMap (entails huge refactor).
        EventBus.subscribe(event -> {
            if (event instanceof PointSelectedConfirmed checked) {
                DefaultListModel<InstalledFeature> model = (DefaultListModel<InstalledFeature>) this.getModel();

                if (!(checked.point() instanceof WeaponSlotPoint slotPoint)) return;

                InstalledFeature target = null;

                for (int i = 0; i < model.size(); i++) {
                    InstalledFeature feature = model.get(i);
                    String slotID = feature.getSlotID();
                    if (slotID.equals(slotPoint.getId())) {
                        target = feature;
                    }
                }

                propagationBlock = true;
                this.setSelectedValue(target, true);
            }
        });
    }

    private void actOnSelectedEntry(Consumer<InstalledFeature> action) {
        int index = this.getSelectedIndex();
        if (index != -1) {
            ListModel<InstalledFeature> listModel = this.getModel();
            InstalledFeature feature = listModel.getElementAt(index);
            action.accept(feature);
        }
    }

    @Override
    protected void sortListModel() {
        ListModel<InstalledFeature> model = this.getModel();
        Map<String, InstalledFeature> rearranged = new LinkedHashMap<>(model.getSize());
        for (int i = 0; i < model.getSize(); i++) {
            InstalledFeature feature = model.getElementAt(i);
            rearranged.put(feature.getSlotID(), feature);
        }
        this.sorter.accept(rearranged);
    }

    private class FeatureContextMenuListener extends MouseAdapter {

        private JPopupMenu getContextMenu() {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem removePoint = new JMenuItem("Uninstall feature");
            removePoint.addActionListener(event -> actOnSelectedEntry(uninstaller));
            menu.add(removePoint);

            JMenuItem selectEntry = new JMenuItem("Select weapon entry");
            selectEntry.addActionListener(event -> actOnSelectedEntry(feature -> {
                CSVEntry dataEntry = feature.getDataEntry();
                if (dataEntry instanceof WeaponCSVEntry weaponEntry) {
                    EventBus.publish(new SelectWeaponDataEntry(weaponEntry));
                }
            }));
            InstalledFeature selected = getSelectedValue();
            if (!(selected.getDataEntry() instanceof WeaponCSVEntry)) {
                selectEntry.setEnabled(false);
            }
            menu.add(selectEntry);

            return menu;
        }

        public void mousePressed(MouseEvent e) {
            if ( SwingUtilities.isRightMouseButton(e) ) {
                setSelectedIndex(locationToIndex(e.getPoint()));
                JPopupMenu menu = getContextMenu();
                menu.show(InstalledFeatureList.this, e.getPoint().x, e.getPoint().y);
            }
        }
    }

}
