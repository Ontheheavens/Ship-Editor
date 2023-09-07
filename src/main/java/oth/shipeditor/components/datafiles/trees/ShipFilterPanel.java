package oth.shipeditor.components.datafiles.trees;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullTreeReloadQueued;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.HullSize;
import oth.shipeditor.utility.components.ComponentUtilities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.file.Path;
import java.util.List;
import java.util.*;

/**
 * @author Ontheheavens
 * @since 07.09.2023
 */
class ShipFilterPanel extends JPanel {

    @Getter @Setter
    private static String currentTextFilter;

    @SuppressWarnings("StaticCollection")
    @Getter
    private static final Map<HullSize, Boolean> SIZE_FILTERS = new EnumMap<>(HullSize.class);

    static {
        SIZE_FILTERS.put(HullSize.DEFAULT, true);
        SIZE_FILTERS.put(HullSize.FIGHTER, true);
        SIZE_FILTERS.put(HullSize.FRIGATE, true);
        SIZE_FILTERS.put(HullSize.DESTROYER, true);
        SIZE_FILTERS.put(HullSize.CRUISER, true);
        SIZE_FILTERS.put(HullSize.CAPITAL_SHIP, true);
    }

    ShipFilterPanel() {
        this.setLayout(new BorderLayout());

        JPanel filtersPane = new JPanel();
        filtersPane.setLayout(new BoxLayout(filtersPane, BoxLayout.PAGE_AXIS));
        filtersPane.setAlignmentY(0);
        filtersPane.add(Box.createRigidArea(new Dimension(10, 4)));
        filtersPane.add(ShipFilterPanel.createHullSizeFilters());
        filtersPane.add(Box.createRigidArea(new Dimension(10, 4)));

        filtersPane.add(Box.createVerticalGlue());

        JScrollPane scrollContainer = new JScrollPane(filtersPane);

        this.add(scrollContainer, BorderLayout.CENTER);
    }

    private static boolean shouldDisplayByHandle(ShipCSVEntry entry) {
        if (currentTextFilter == null || currentTextFilter.isEmpty()) return true;
        String name = entry.toString();
        if (name.toLowerCase(Locale.ROOT).contains(currentTextFilter)) {
            return true;
        }
        String id = entry.getHullID();
        return id.toLowerCase(Locale.ROOT).contains(currentTextFilter);
    }

    static Map<Path, List<ShipCSVEntry>> getFilteredEntries() {
        GameDataRepository gameData = SettingsManager.getGameData();
        Map<Path, List<ShipCSVEntry>> shipEntriesByPackage = gameData.getShipEntriesByPackage();

        if (shipEntriesByPackage == null) return null;

        Map<Path, List<ShipCSVEntry>> filteredResult = new HashMap<>();
        for (Map.Entry<Path, List<ShipCSVEntry>> entryPackage : shipEntriesByPackage.entrySet()) {
            List<ShipCSVEntry> entryList = entryPackage.getValue();
            List<ShipCSVEntry> filteredList = entryList.stream()
                    .filter(ShipFilterPanel::shouldDisplayBySize)
                    .filter(ShipFilterPanel::shouldDisplayByHandle)
                    .toList();
            if (!filteredList.isEmpty()) {
                filteredResult.put(entryPackage.getKey(), filteredList);
            }
        }
        return filteredResult;
    }

    private static boolean shouldDisplayBySize(ShipCSVEntry entry) {
        HullSize entrySize = entry.getSize();
        return SIZE_FILTERS.get(entrySize);
    }

    private static JPanel createHullSizeFilters() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
        container.setAlignmentX(0.5f);
        container.setAlignmentY(0);

        ComponentUtilities.outfitPanelWithTitle(container,
                new Insets(1, 0, 0, 0), "Hull size");

        Iterable<HullSize> hullSizes = new ArrayList<>(List.of(HullSize.values()));

        for (HullSize size : hullSizes) {
            JPanel buttonContainer = new JPanel();

            buttonContainer.setLayout(new BoxLayout(buttonContainer, BoxLayout.LINE_AXIS));
            buttonContainer.setBorder(new EmptyBorder(4, 0, 0, 0));

            JCheckBox checkBox = new JCheckBox();
            checkBox.setText(size.getDisplayedName());
            checkBox.setSelected(SIZE_FILTERS.get(size));
            checkBox.addActionListener(e -> {
                if (checkBox.isSelected()) {
                    SIZE_FILTERS.put(size, Boolean.TRUE);
                } else {
                    SIZE_FILTERS.put(size, Boolean.FALSE);
                }
                EventBus.publish(new HullTreeReloadQueued());
            });
            buttonContainer.add(new JLabel(size.getIcon()));
            buttonContainer.add(checkBox);
            buttonContainer.add(Box.createHorizontalGlue());

            container.add(buttonContainer);
        }
        container.add(Box.createRigidArea(new Dimension(10, 4)));
        return container;
    }

}
