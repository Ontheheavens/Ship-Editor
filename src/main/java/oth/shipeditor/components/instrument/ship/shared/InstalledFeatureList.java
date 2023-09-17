package oth.shipeditor.components.instrument.ship.shared;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.points.PointSelectQueued;
import oth.shipeditor.communication.events.viewer.points.PointSelectedConfirmed;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.painters.features.InstalledFeature;
import oth.shipeditor.utility.StaticController;
import oth.shipeditor.utility.components.containers.SortableList;
import oth.shipeditor.utility.components.rendering.InstalledFeatureCellRenderer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 17.09.2023
 */
public class InstalledFeatureList extends SortableList<InstalledFeature> {

    private boolean propagationBlock;

    private final Consumer<InstalledFeature> uninstall;

    public InstalledFeatureList(ListModel<InstalledFeature> dataModel, Consumer<InstalledFeature> removeAction) {
        super(dataModel);
        this.uninstall = removeAction;
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
                if (target == null) return;

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

    @SuppressWarnings("NoopMethodInAbstractClass")
    @Override
    protected void sortListModel() {

    }

    private class FeatureContextMenuListener extends MouseAdapter {

        private JPopupMenu getContextMenu() {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem removePoint = new JMenuItem("Uninstall feature");
            removePoint.addActionListener(event -> actOnSelectedEntry(feature -> uninstall.accept(feature)
            ));
            menu.add(removePoint);
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
