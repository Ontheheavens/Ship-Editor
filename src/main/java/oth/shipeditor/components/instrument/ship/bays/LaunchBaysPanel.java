package oth.shipeditor.components.instrument.ship.bays;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.BaysPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.communication.events.viewer.points.LaunchBayAddConfirmed;
import oth.shipeditor.communication.events.viewer.points.LaunchBayRemoveConfirmed;
import oth.shipeditor.communication.events.viewer.points.PointAddConfirmed;
import oth.shipeditor.communication.events.viewer.points.PointRemovedConfirmed;
import oth.shipeditor.components.viewer.entities.bays.LaunchPortPoint;
import oth.shipeditor.components.viewer.entities.weapon.SlotPoint;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.PainterVisibility;
import oth.shipeditor.components.viewer.painters.points.LaunchBayPainter;
import oth.shipeditor.utility.components.ComponentUtilities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * @author Ontheheavens
 * @since 13.08.2023
 */
@Log4j2
public class LaunchBaysPanel extends JPanel {

    private final LaunchBaysTree baysTree;

    private final JPanel bayDataPaneContainer;

    private SlotPoint cachedPortSelection;

    public LaunchBaysPanel() {
        this.setLayout(new BorderLayout());

        baysTree = new LaunchBaysTree(new DefaultMutableTreeNode("Bays"), this::refreshBayControlPane);

        JScrollPane scrollableContainer = new JScrollPane(baysTree);

        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.PAGE_AXIS));

        JPanel visibilityWidgetContainer = this.createPainterVisibilityPanel();
        northContainer.add(visibilityWidgetContainer);

        ComponentUtilities.addSeparatorToBoxPanel(northContainer);

        bayDataPaneContainer = new JPanel();
        bayDataPaneContainer.setLayout(new BorderLayout());
        bayDataPaneContainer.setBorder(new EmptyBorder(3, 0, 4, 0));

        northContainer.add(bayDataPaneContainer);

        this.add(northContainer, BorderLayout.PAGE_START);

        this.add(scrollableContainer, BorderLayout.CENTER);
        this.initPointListener();
        this.initLayerListeners();

        this.refreshBayControlPane(null);
    }

    private void refreshBayControlPane(SlotPoint selectedPort) {
        bayDataPaneContainer.removeAll();

        cachedPortSelection = selectedPort;

        JPanel bayControl = new BayDataControlPane(selectedPort);
        bayDataPaneContainer.add(bayControl, BorderLayout.CENTER);

        bayDataPaneContainer.revalidate();
        bayDataPaneContainer.repaint();
    }

    private void initPointListener() {
        EventBus.subscribe(event -> {
            if (event instanceof BaysPanelRepaintQueued) {
                this.refreshBayControlPane(cachedPortSelection);
                this.baysTree.reloadModel();
                this.repaint();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof LaunchBayAddConfirmed checked) {
                baysTree.addBay(checked.added());
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof LaunchBayRemoveConfirmed checked) {
                baysTree.removeBay(checked.removed());
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointAddConfirmed checked && checked.point() instanceof LaunchPortPoint point) {
                baysTree.addPort(point);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointRemovedConfirmed checked && checked.point() instanceof LaunchPortPoint point) {
                baysTree.removePort(point);
            }
        });
    }

    private void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                ViewerLayer selected = checked.selected();
                baysTree.clearRoot();
                if (!(selected instanceof ShipLayer checkedLayer)) {
                    refreshBayControlPane(null);
                    return;
                }
                ShipPainter painter = checkedLayer.getPainter();
                if (painter != null && !painter.isUninitialized()) {
                    LaunchBayPainter bayPainter = painter.getBayPainter();
                    baysTree.repopulateTree(bayPainter);
                    baysTree.repaint();
                }
                refreshBayControlPane(null);
            }
        });
    }

    @SuppressWarnings("MethodMayBeStatic")
    private JPanel createPainterVisibilityPanel() {
        JComboBox<PainterVisibility> visibilityList = new JComboBox<>(PainterVisibility.values());
        ActionListener selectionAction = e -> {
            if (!(e.getSource() instanceof ShipPainter checked)) return;
            LaunchBayPainter bayPainter = checked.getBayPainter();
            PainterVisibility valueOfLayer = bayPainter.getVisibilityMode();
            visibilityList.setSelectedItem(valueOfLayer);
        };

        return ComponentUtilities.createVisibilityWidget(visibilityList,
                LaunchBayPainter.class, selectionAction, "");
    }

}
