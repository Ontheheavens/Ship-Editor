package oth.shipeditor.components;

import lombok.Getter;
import lombok.Setter;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.fluentui.FluentUiRegularMZ;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.PrimaryWindow;
import oth.shipeditor.components.control.ShipViewerControls;
import oth.shipeditor.components.entities.WorldPoint;
import oth.shipeditor.menubar.PrimaryMenuBar;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;

/**
 * @author Ontheheavens
 * @since 30.04.2023
 */
public class BoundPointsPanel extends JPanel {
    public enum PointsMode {
        DISABLED, SELECT, CREATE
    }
    @Getter
    private final JList<WorldPoint> pointContainer;
    @Getter @Setter
    private PointsMode mode;
    @Getter
    private final DefaultListModel<WorldPoint> model = new DefaultListModel<>();
    @Getter
    private JToggleButton selectModeButton;
    @Getter
    private JToggleButton createModeButton;

    public BoundPointsPanel() {
        this.setLayout(new BorderLayout());
        pointContainer = new JList<>(model);
        int margin = 3;
        pointContainer.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = pointContainer.getSelectedIndex();
                if (index != -1) {
                    WorldPoint point = model.getElementAt(index);
                    ShipViewerPanel viewerPanel = PrimaryWindow.getInstance().getShipView();
                    if (viewerPanel.getControls().getSelected() != null) {
                        viewerPanel.getControls().getSelected().setSelected(false);
                        viewerPanel.getControls().setSelected(null);
                    }
                    point.setSelected(true);
                    viewerPanel.getControls().setSelected(point);
                    viewerPanel.repaint();
                }
            }
        });
        pointContainer.setBorder(new EmptyBorder(margin, margin, margin, margin));
        JScrollPane scrollableContainer = new JScrollPane(pointContainer);
        Dimension listSize = new Dimension(pointContainer.getPreferredSize().width + 30,
                pointContainer.getPreferredSize().height);
        scrollableContainer.setPreferredSize(listSize);
        this.add(scrollableContainer, BorderLayout.CENTER);
        JPanel modePanel = new JPanel();
        modePanel.setBorder(BorderFactory.createEtchedBorder());
        this.createModeButtons(modePanel);
        this.add(modePanel, BorderLayout.NORTH);
        Border line = BorderFactory.createLineBorder(Color.DARK_GRAY);
        this.setBorder(line);
        this.setMode(PointsMode.DISABLED);
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

    {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                PrimaryMenuBar menuBar = PrimaryWindow.getInstance().getPrimaryMenu();
                ShipViewerControls controls = PrimaryWindow.getControls();
                selectModeButton.addItemListener(ev -> {
                    if (ev.getStateChange() == ItemEvent.SELECTED) {
                        BoundPointsPanel.this.setMode(PointsMode.SELECT);
                        menuBar.getToggleRotate().setEnabled(true);
                    }
                });
                createModeButton.addItemListener(ev -> {
                    if (ev.getStateChange() == ItemEvent.SELECTED) {
                        BoundPointsPanel.this.setMode(PointsMode.CREATE);
                        controls.setRotationEnabled(false);
                        menuBar.getToggleRotate().setSelected(false);
                        menuBar.getToggleRotate().setEnabled(false);
                    }
                });
                controls.getPCS().addPropertyChangeListener("rotationEnabled", evt -> {
                    if (controls.isRotationEnabled()) {
                        BoundPointsPanel.this.setMode(PointsMode.SELECT);
                        selectModeButton.setSelected(true);
                    }
                });
            }
        });
    }

}
