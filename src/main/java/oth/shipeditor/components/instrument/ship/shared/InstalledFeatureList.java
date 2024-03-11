package oth.shipeditor.components.instrument.ship.shared;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.SelectWeaponDataEntry;
import oth.shipeditor.communication.events.viewer.points.PointSelectQueued;
import oth.shipeditor.components.datafiles.entities.CSVEntry;
import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;
import oth.shipeditor.components.viewer.entities.weapon.SlotData;
import oth.shipeditor.components.viewer.painters.points.ship.WeaponSlotPainter;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;
import oth.shipeditor.utility.components.containers.SortableList;
import oth.shipeditor.utility.components.rendering.InstalledFeatureCellRenderer;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
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

    private final Consumer<InstalledFeature> uninstaller;

    private final Consumer<InstalledFeature> selectionAction;

    private final Consumer<Map<String, InstalledFeature>> sorter;

    protected static final DataFlavor FEATURE_FLAVOR = new DataFlavor(InstalledFeature.class,
            StringValues.INSTALLED_FEATURE);

    public InstalledFeatureList(ListModel<InstalledFeature> dataModel,
                                Consumer<InstalledFeature> removeAction,
                                Consumer<Map<String, InstalledFeature>> sortAction,
                                Consumer<InstalledFeature> selectAction) {
        super(dataModel);
        this.uninstaller = removeAction;
        this.sorter = sortAction;
        this.selectionAction = selectAction;
        this.addListSelectionListener(e -> {
            this.actOnSelectedEntry(this::handleEntrySelection);
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
        this.setDragEnabled(true);
    }

    public static WeaponSlotPainter getSlotPainter() {
        return StaticController.getSelectedSlotPainter();
    }

    protected void handleEntrySelection(InstalledFeature feature) {
        if (feature == null) return;
        if (this.selectionAction == null) return;
        this.selectionAction.accept(feature);
    }

    private static ListCellRenderer<InstalledFeature> createCellRenderer() {
        return new InstalledFeatureCellRenderer();
    }

    /**
     * This is quite a hack; it is assumed that whatever feature is selected is installed onto
     * existing slot point in active layer.
     */
    private static void selectSlotPoint(InstalledFeature feature) {
        var slotPainter = InstalledFeatureList.getSlotPainter();
        if (slotPainter != null) {
            var slotPoint = slotPainter.getSlotByID(feature.getSlotID());
            if (slotPoint == null) return;
            EventBus.publish(new PointSelectQueued(slotPoint));
        }
    }

    public void selectEntryByPoint(SlotData slotPoint) {
        DefaultListModel<InstalledFeature> model = (DefaultListModel<InstalledFeature>) this.getModel();
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

    protected JMenuItem getSelectEntryOption(InstalledFeature selected) {
        JMenuItem selectEntry = new JMenuItem(StringValues.SELECT_WEAPON_ENTRY);
        selectEntry.addActionListener(event -> actOnSelectedEntry(feature -> {
            CSVEntry dataEntry = feature.getDataEntry();
            if (dataEntry instanceof WeaponCSVEntry weaponEntry) {
                EventBus.publish(new SelectWeaponDataEntry(weaponEntry));
            }
        }));
        if (!(selected.getDataEntry() instanceof WeaponCSVEntry)) {
            selectEntry.setEnabled(false);
        }
        return selectEntry;
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

    @Override
    protected Transferable createTransferableFromEntry(InstalledFeature entry) {
        CSVEntry dataEntry = entry.getDataEntry();
        return new Transferable() {
            private final InstalledFeature feature = entry;

            private final DataFlavor sourceFlavor = new DataFlavor(InstalledFeatureList.this.getClass(),
                    String.valueOf(InstalledFeatureList.this.hashCode()));

            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[] {FEATURE_FLAVOR, sourceFlavor};
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return flavor.equals(FEATURE_FLAVOR);
            }

            @Override
            public Object getTransferData(DataFlavor flavor) {
                return feature;
            }
        };
    }

    @Override
    protected boolean isSupported(Transferable transferable) {
        return transferable.getTransferDataFlavors()[0].equals(FEATURE_FLAVOR);
    }

    protected JPopupMenu getContextMenu() {
        InstalledFeature selected = getSelectedValue();
        if (selected == null) return null;
        JPopupMenu menu = new JPopupMenu();
        JMenuItem removePoint = new JMenuItem(StringValues.UNINSTALL_FEATURE);
        removePoint.addActionListener(event -> actOnSelectedEntry(uninstaller));
        menu.add(removePoint);

        JMenuItem selectEntry = getSelectEntryOption(selected);
        menu.add(selectEntry);

        return menu;
    }

    private class FeatureContextMenuListener extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            if ( SwingUtilities.isRightMouseButton(e) ) {
                setSelectedIndex(locationToIndex(e.getPoint()));
                JPopupMenu menu = getContextMenu();
                if (menu != null) {
                    menu.show(InstalledFeatureList.this, e.getPoint().x, e.getPoint().y);
                }
            }
        }
    }

}
