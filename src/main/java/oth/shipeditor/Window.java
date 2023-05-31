package oth.shipeditor;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.ShipLayerCreated;
import oth.shipeditor.communication.events.viewer.layers.ShipLayerUpdated;
import oth.shipeditor.components.BoundPointsPanel;
import oth.shipeditor.components.ShipViewerPanel;
import oth.shipeditor.components.ViewerStatusPanel;
import oth.shipeditor.menubar.PrimaryMenuBar;
import oth.shipeditor.representation.LayerManager;
import oth.shipeditor.representation.ShipLayer;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 27.04.2023
 */
@SuppressWarnings("FieldCanBeLocal")
@Log4j2
public class Window extends JFrame {

    private static final Window global = new Window();

    @Getter
    private final PrimaryMenuBar primaryMenu;

    /**
     * Complex component responsible for ship layers display.
     */
    @Getter
    private ShipViewerPanel shipView = null;

    /**
     * Parent pane for ship data editing tabs.
     */
    private JTabbedPane instrumentPane = null;

    /**
     * Panel for data representation of ship bounds.
     */
    @Getter
    private BoundPointsPanel pointsPanel = null;

    /**
     * Parent pane for various status panels.
     */
    private JPanel southPane = null;

    /**
     * Status line panel for ship sprite viewer.
     */
    @Getter
    private ViewerStatusPanel statusPanel = null;

    private final LayerManager layerManager;

    private Window() {
        log.info("Creating window.");
        this.setTitle("Ship Editor");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setMinimumSize(new Dimension(800, 600));

        // This centers the frame.
        this.setLocationRelativeTo(null);
        this.getContentPane().setLayout(new BorderLayout());

        primaryMenu = new PrimaryMenuBar(this);
        this.setJMenuBar(primaryMenu);

        this.layerManager = new LayerManager();
        this.layerManager.initListeners();
        this.initLoaderListeners();

        this.pack();
    }

    public static Window getFrame() {
        return global;
    }

    // TODO: this is all wrong, replace later.
    private void initLoaderListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof ShipLayerCreated checked) {
                if (shipView == null) {
                    if (checked.newLayer().getShipSprite() != null) {
                        loadShipView(checked.newLayer());
                    }
                } else {
                    if (checked.newLayer().getShipSprite() != null) {
                        shipView.loadLayer(checked.newLayer());
                    }
                }
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ShipLayerUpdated checked) {
                if (shipView == null) return;
                if (checked.updated().getShipData() != null && instrumentPane == null) {
//                    loadEditingPanes();
                }
            }
        });
    }

    /**
     *  Meant to be called when the PNG sprite file loads.
     */
    public void loadShipView(ShipLayer newLayer) {

        this.setShipView(new ShipViewerPanel());
        shipView.loadLayer(newLayer);

        southPane = new JPanel();
        southPane.setLayout(new GridLayout());
        statusPanel = new ViewerStatusPanel();
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        statusPanel.setDimensionsLabel(newLayer.getShipSprite());
        southPane.add(statusPanel);
        this.getContentPane().add(southPane, BorderLayout.SOUTH);
        this.loadEditingPanes();
        this.refreshContent();
    }

    private void refreshContent() {
        this.getContentPane().revalidate();
        this.getContentPane().repaint();
    }

    /**
     *  Meant to be called when ship JSON file loads.
     */
    public void loadEditingPanes() {

        instrumentPane = new JTabbedPane();
        instrumentPane.setTabPlacement(JTabbedPane.LEFT);
        pointsPanel = new BoundPointsPanel();
        instrumentPane.addTab("B",pointsPanel);
        instrumentPane.addTab("E",new JPanel());

        JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitter.setLeftComponent(shipView);
        splitter.setRightComponent(instrumentPane);
        splitter.setResizeWeight(0.95);
        this.getContentPane().add(splitter, BorderLayout.CENTER);
        this.refreshContent();
    }

    public void setShipView(ShipViewerPanel newPanel) {
        ShipViewerPanel old = this.shipView;
        if (old != null) {
            this.remove(old);
        }
        this.shipView = newPanel;
        if (this.shipView != null) {
            this.add(this.shipView);
        }
    }


    public void showGUI() {
//        URI dataPath;
//        try {
//            dataPath = Objects.requireNonNull(getClass().getClassLoader().getResource("legion.ship")).toURI();
//        } catch (URISyntaxException e) {
//            throw new RuntimeException(e);
//        }
//        shipData = new ShipData(dataPath);
        this.setVisible(true);
    }

}
