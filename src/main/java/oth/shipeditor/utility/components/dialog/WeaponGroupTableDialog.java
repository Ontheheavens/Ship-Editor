package oth.shipeditor.utility.components.dialog;

import oth.shipeditor.components.datafiles.entities.CSVEntry;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.components.viewer.painters.points.ship.features.FireMode;
import oth.shipeditor.components.viewer.painters.points.ship.features.FittedWeaponGroup;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeatureComparator;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * @author Ontheheavens
 * @since 03.12.2023
 */
class WeaponGroupTableDialog extends JPanel {

    private final ShipVariant variant;

    private TableModel model;

    private static final int MAX_WEAPON_GROUPS = 7;

    WeaponGroupTableDialog(ShipVariant shipVariant) {
        this.variant = shipVariant;

        this.setLayout(new BorderLayout());
        this.add(createTablePanel(), BorderLayout.CENTER);
    }

    private JPanel createTablePanel() {
        this.model = createModel();
        JTable table = new GroupTable();
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel container = new JPanel(new BorderLayout());
        container.add(scrollPane, BorderLayout.CENTER);

        return container;
    }

    private DefaultTableModel createModel() {
        List<InstalledFeature> weapons = variant.getAllFittedWeaponsList();
        List<FittedWeaponGroup> weaponGroups = variant.getWeaponGroups();

        weapons.sort(new InstalledFeatureComparator());
        Collections.reverse(weapons);

        int oldListSize = weaponGroups.size();

        Vector<Vector<Object>> data = new Vector<>(weapons.size());

        weapons.forEach(feature -> {
            Vector<Object> row = new Vector<>(MAX_WEAPON_GROUPS + 1);

            CSVEntry dataEntry = feature.getDataEntry();
            row.add(feature);

            for (int i = 0; i < MAX_WEAPON_GROUPS; i++) {
                if (oldListSize > i) {
                    FittedWeaponGroup group = weaponGroups.get(i);

                    if (group.containsFitting(feature)) {
                        row.add(Boolean.TRUE);
                    } else {
                        row.add(Boolean.FALSE);
                    }
                } else {
                    row.add(Boolean.FALSE);
                }
            }
            data.add(row);
        });

        Vector<String> columnNames = new Vector<>(MAX_WEAPON_GROUPS + 1);
        columnNames.add("Weapon");
        for (int i = 0; i < MAX_WEAPON_GROUPS; i++) {
            columnNames.add(String.valueOf(i + 1));
        }

        return new GroupTableModel(data, columnNames);
    }

    List<FittedWeaponGroup> getUpdatedGroups() {
        List<FittedWeaponGroup> updated = new ArrayList<>();

        int rowCount = model.getRowCount();

        for (int i = 1; i < MAX_WEAPON_GROUPS + 1; i++) {
            FittedWeaponGroup group = null;

            for (int r = 0; r < rowCount; r++) {
                boolean isInGroup = (boolean) model.getValueAt(r, i);
                if (isInGroup) {
                    if (group == null) {
                        group = new FittedWeaponGroup(variant, false, FireMode.LINKED);
                    }
                    InstalledFeature rowWeapon = (InstalledFeature) model.getValueAt(r, 0);

                    group.addFitting(rowWeapon.getSlotID(), rowWeapon);
                }
            }

            if (group != null) {
                updated.add(group);
            }
        }

        return updated;
    }

    private static final class GroupTableModel extends DefaultTableModel {

        private GroupTableModel(Vector<Vector<Object>> data, Vector<String> columnNames) {
            super(data, columnNames);
        }

        public Class<?> getColumnClass(int columnIndex) {
            Object value = getValueAt(0, columnIndex);
            return value.getClass();
        }

        public boolean isCellEditable(int row, int column) {
            return column >= 1;
        }

        public void setValueAt(Object aValue, int row, int column) {
            @SuppressWarnings("unchecked")
            Vector<Object> rowVector = dataVector.elementAt(row);
            for (int i = 1; i < rowVector.size(); i++) {
                rowVector.set(i, Boolean.FALSE);
            }
            rowVector.setElementAt(Boolean.TRUE, column);
            fireTableDataChanged();
        }

    }

    private static class FeatureNameRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof InstalledFeature feature) {
                setText(feature.getName());
            }
            return this;
        }

    }

    private final class GroupTable extends JTable {

        private GroupTable() {
            super(WeaponGroupTableDialog.this.model);

            Dimension preferredSize = this.getPreferredSize();
            int width = Math.min(preferredSize.width, 400);
            int height = Math.min(preferredSize.height, 300);

            this.setPreferredScrollableViewportSize(new Dimension(width, height));

            JTableHeader header = this.getTableHeader();
            header.setReorderingAllowed(false);

            TableColumnModel tableColumnModel = this.getColumnModel();
            int columnCount = tableColumnModel.getColumnCount();
            for (int i = 0; i < columnCount; i++) {
                TableColumn column = tableColumnModel.getColumn(i);
                if (i > 0) {
                    column.setMinWidth(25);
                    column.setPreferredWidth(25);
                } else {
                    column.setMinWidth(100);
                    column.setPreferredWidth(150);
                }
            }
        }

        @Override
        public TableCellRenderer getCellRenderer(int row, int column) {
            if (column == 0) {
                return new FeatureNameRenderer();
            }
            return super.getCellRenderer(row, column);
        }

    }

}
