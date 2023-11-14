package oth.shipeditor.components.instrument.ship;

import lombok.Getter;
import oth.shipeditor.components.viewer.entities.weapon.SlotData;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.objects.Pair;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Ontheheavens
 * @since 14.11.2023
 */
public abstract class AbstractSlotValuesPanel2 extends AbstractShipPropertiesPanel {

    @Getter
    private SlotData selected;

    private final boolean multiSelectionAllowed;

    protected AbstractSlotValuesPanel2(boolean multiSelection) {
        this.multiSelectionAllowed = multiSelection;
    }

    protected abstract String getEntityName();

    /**
     * @return ID from painter of cached layer that is not yet assigned to any slot.
     */
    protected abstract String getNextUniqueID();

    @Override
    public void refreshContent(LayerPainter layerPainter) {

    }

    @Override
    protected void populateContent() {

    }

    protected Pair<JLabel, JComponent> createIDPanel() {
        JLabel label = new JLabel(getEntityName() + " ID:");

        if (multiSelectionAllowed) {
            label.setToolTipText(StringValues.CHANGE_APPLIES_TO_FIRST_SELECTED_SLOT);
        }

        JComponent right = ComponentUtilities.getNoSelected();

        if (selected != null) {
            JTextField editor = new JTextField(selected.getId());
            editor.setColumns(10);
            editor.addActionListener(e -> {
                String currentText = editor.getText();
                EditDispatch.postSlotIDChanged(selected, currentText);
            });
            right = editor;

            JPopupMenu contextMenu = getIDMenu(editor);
            String confirmHint = StringValues.ENTER_TO_SAVE_CHANGES;
            String menuHint = StringValues.RIGHT_CLICK_TO_GENERATE;
            editor.setToolTipText(Utility.getWithLinebreaks(confirmHint, menuHint));
            editor.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        contextMenu.show(editor, e.getX(), e.getY());
                    }
                }
            });
        }

        ComponentUtilities.addLabelAndComponent(this, label, right, 0);

        return new Pair<>(label, right);
    }

    private JPopupMenu getIDMenu(JTextField editor) {
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem createNextUniqueId = new JMenuItem(StringValues.CREATE_NEXT_UNIQUE_ID);
        createNextUniqueId.addActionListener(e -> {
            String nextUniqueID = getNextUniqueID();
            if (nextUniqueID != null) {
                editor.setText(nextUniqueID);
            }
        });
        contextMenu.add(createNextUniqueId);
        return contextMenu;
    }

}
