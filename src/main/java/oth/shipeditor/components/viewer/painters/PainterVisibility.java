package oth.shipeditor.components.viewer.painters;

import lombok.Getter;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.communication.events.viewer.layers.PainterVisibilityChanged;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ShipLayer;
import oth.shipeditor.components.viewer.painters.points.AbstractPointPainter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Ontheheavens
 * @since 16.07.2023
 */
public enum PainterVisibility {
    ALWAYS_HIDDEN("Always hidden"),
    SHOWN_WHEN_SELECTED("When selected"),
    SHOWN_WHEN_EDITED("When edited"),
    ALWAYS_SHOWN("Always shown");

    @Getter
    private final String name;

    PainterVisibility(String inputName) {
        this.name = inputName;
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static DefaultListCellRenderer createCellRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Component rendererComponent = super.getListCellRendererComponent(list, value, index,
                        isSelected, cellHasFocus);
                setText(value instanceof PainterVisibility ? ((PainterVisibility) value).getName() : value.toString());
                return rendererComponent;
            }
        };
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static ActionListener createActionListener(JComboBox<PainterVisibility> visibilityList,
                                                      Class<? extends AbstractPointPainter> painterClass) {
        return e -> {
            PainterVisibility changedValue = (PainterVisibility) visibilityList.getSelectedItem();
            EventBus.publish(new PainterVisibilityChanged(painterClass, changedValue));
        };
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static BusEventListener createBusEventListener(JComboBox<PainterVisibility> visibilityList,
                                                          ActionListener selectionAction) {
        return event -> {
            if (event instanceof LayerWasSelected checked) {
                ShipLayer selected = checked.selected();
                if (selected == null) {
                    visibilityList.setSelectedItem(PainterVisibility.SHOWN_WHEN_EDITED);
                    return;
                }
                LayerPainter painter = selected.getPainter();
                if (painter == null) {
                    visibilityList.setSelectedItem(PainterVisibility.SHOWN_WHEN_EDITED);
                } else {
                    selectionAction.actionPerformed(new ActionEvent(painter,
                            ActionEvent.ACTION_PERFORMED, null));
                }
            }
        };
    }

}
