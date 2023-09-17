package oth.shipeditor.components.instrument.ship.builtins;

import oth.shipeditor.components.datafiles.entities.CSVEntry;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 17.09.2023
 */
public abstract class CSVEntryBuiltInsPanel<T extends CSVEntry> extends AbstractBuiltInsPanel {

    void handleSkinChanges(List<T> entryList, Color panelColor) {
        handleSkinChanges(entryList, panelColor, StringValues.ADDED_BY_SKIN);
    }

    void handleSkinChanges(List<T> entryList, Color panelColor, String panelTitle) {
        if (entryList != null && !entryList.isEmpty()) {
            JPanel contentPane = this.getContentPane();
            contentPane.add(Box.createVerticalStrut(2));
            JPanel title = ComponentUtilities.createTitledSeparatorPanel(panelTitle);
            title.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
            title.setAlignmentY(0);
            contentPane.add(title);

            this.populateWithEntries(contentPane, entryList, panel -> {
                if (panelColor != null) {
                    panel.setBackground(panelColor);
                }
            });
        }
    }

    protected abstract  void populateWithEntries(JPanel container, List<T> entryList,
                                                 Consumer<JPanel> panelMutator);

}
