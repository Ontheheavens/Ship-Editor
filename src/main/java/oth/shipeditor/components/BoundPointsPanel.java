package oth.shipeditor.components;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.fluentui.FluentUiRegularMZ;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.BoundPointPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.control.ViewerRotationToggled;
import oth.shipeditor.communication.events.viewer.control.ViewerTransformChanged;
import oth.shipeditor.communication.events.viewer.control.ViewerZoomChanged;
import oth.shipeditor.communication.events.viewer.points.BoundCreationModeChanged;
import oth.shipeditor.communication.events.viewer.points.BoundInsertedConfirmed;
import oth.shipeditor.communication.events.viewer.points.PointAddConfirmed;
import oth.shipeditor.communication.events.viewer.points.PointRemovedConfirmed;
import oth.shipeditor.components.viewer.PointsDisplay;
import oth.shipeditor.components.viewer.entities.BoundPoint;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ItemEvent;

/**
 * @author Ontheheavens
 * @since 30.04.2023
 */
@Log4j2
public final class BoundPointsPanel extends JPanel implements PointsDisplay<BoundPoint> {

    // TODO: update panel to reflect layer switching functionality.

    @Getter
    private final BoundList boundPointContainer;

    @Getter
    private InteractionMode mode;

    private final DefaultListModel<BoundPoint> model = new DefaultListModel<>();
    @Getter
    private JToggleButton selectModeButton;
    @Getter
    private JToggleButton createModeButton;

    public BoundPointsPanel() {
        this.setLayout(new BorderLayout());
        boundPointContainer = new BoundList(model);
        JScrollPane scrollableContainer = new JScrollPane(boundPointContainer);
        Dimension listSize = new Dimension(boundPointContainer.getPreferredSize().width + 30,
                boundPointContainer.getPreferredSize().height);
        scrollableContainer.setPreferredSize(listSize);
        this.add(scrollableContainer, BorderLayout.CENTER);
        JPanel modePanel = new JPanel();
        modePanel.setBorder(BorderFactory.createEtchedBorder());

        this.createModeButtons(modePanel);
        this.initModeButtonListeners();

        this.add(modePanel, BorderLayout.PAGE_START);
        Border line = BorderFactory.createLineBorder(Color.DARK_GRAY);
        this.setBorder(line);
        this.setMode(InteractionMode.DISABLED);

        this.initPointListener();
    }

    private void setMode(InteractionMode newMode) {
        this.mode = newMode;
        EventBus.publish(new BoundCreationModeChanged(newMode));
    }

    private void initPointListener() {
        EventBus.subscribe(event -> {
            if (event instanceof BoundPointPanelRepaintQueued ||
                event instanceof ViewerZoomChanged ||
                event instanceof ViewerTransformChanged) {
                this.repaint();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointAddConfirmed checked && checked.point() instanceof BoundPoint point) {
                model.addElement(point);
                boundPointContainer.setSelectedIndex(model.indexOf(point));
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof BoundInsertedConfirmed checked) {
                model.insertElementAt(checked.toInsert(), checked.precedingIndex());
                boundPointContainer.setSelectedIndex(model.indexOf(checked.toInsert()));
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointRemovedConfirmed checked && checked.point() instanceof BoundPoint point) {
                model.removeElement(point);
            }
        });
    }

    private void createModeButtons(JPanel modePanel) {
        selectModeButton = new JToggleButton(FontIcon.of(FluentUiRegularMZ.SELECT_OBJECT_20, 16));
        selectModeButton.setToolTipText("Select, move and delete.");
        modePanel.add(selectModeButton);
        createModeButton = new JToggleButton(FontIcon.of(FluentUiRegularAL.ADD_CIRCLE_20, 16));
        createModeButton.setToolTipText("Create new points.");
        modePanel.add(createModeButton);
        ButtonGroup group = new ButtonGroup();
        group.add(selectModeButton);
        group.add(createModeButton);
        selectModeButton.setSelected(false);
        createModeButton.setSelected(false);
    }

    private void initModeButtonListeners() {
        selectModeButton.addItemListener(ev -> {
            if (ev.getStateChange() == ItemEvent.SELECTED) {
                this.setMode(InteractionMode.SELECT);
                EventBus.publish(new ViewerRotationToggled(true, true));
            }
        });
        createModeButton.addItemListener(ev -> {
            if (ev.getStateChange() == ItemEvent.SELECTED) {
                this.setMode(InteractionMode.CREATE);
                EventBus.publish(new ViewerRotationToggled(false, false));
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ViewerRotationToggled checked) {
                if (checked.isSelected()) {
                    this.setMode(InteractionMode.SELECT);
                    selectModeButton.setSelected(true);
                }

            }
        });
    }

}
