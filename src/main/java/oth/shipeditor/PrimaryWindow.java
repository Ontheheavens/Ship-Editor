package oth.shipeditor;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.ShipViewableCreated;
import oth.shipeditor.communication.events.components.WindowRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.ShipLayerCreated;
import oth.shipeditor.communication.events.viewer.layers.ShipLayerUpdated;
import oth.shipeditor.components.*;
import oth.shipeditor.menubar.Files;
import oth.shipeditor.menubar.PrimaryMenuBar;
import oth.shipeditor.representation.LayerManager;
import oth.shipeditor.representation.ShipLayer;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

/**
 * @author Ontheheavens
 * @since 27.04.2023
 */
@SuppressWarnings("FieldCanBeLocal")
@Log4j2
public final class PrimaryWindow extends JFrame {

    @Getter
    private final PrimaryMenuBar primaryMenu;

    /**
     * Complex component responsible for ship layers display.
     */
    @Getter
    private ShipViewable shipView;

    /**
     * Parent pane for ship data editing tabs.
     */
    private JTabbedPane instrumentPane;

    /**
     * Panel for data representation of ship bounds.
     */
    @Getter
    private BoundPointsPanel pointsPanel;

    /**
     * Parent pane for layers panel and others.
     */
    private JPanel northPane;

    private ShipLayersPanel layersPanel;

    /**
     * Parent pane for various status panels.
     */
    private JPanel southPane;

    /**
     * Status line panel for ship sprite viewer.
     */
    @Getter
    private ViewerStatusPanel statusPanel;

    private LayerManager layerManager;

    private PrimaryWindow() {
        log.info("Creating window.");
        this.setTitle("Ship Editor");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setMinimumSize(new Dimension(800, 600));

        // This centers the frame.
        this.setLocationRelativeTo(null);
        this.getContentPane().setLayout(new BorderLayout());

        primaryMenu = new PrimaryMenuBar();
        this.setJMenuBar(primaryMenu);

        this.loadLayerHandling();

        this.loadShipView();
        this.loadEditingPanes();
        this.dispatchLoaderEvents();

        this.pack();
    }

    public static PrimaryWindow create() {
        return new PrimaryWindow();
    }

    // TODO: this is all wrong, replace later.
    private void initLoaderListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof ShipLayerCreated checked) {
                ShipLayer newLayer = checked.newLayer();
                if (newLayer.getShipSprite() != null) {
                    shipView.loadLayer(newLayer);
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
        EventBus.subscribe(event -> {
            if (event instanceof WindowRepaintQueued) {
                refreshContent();
            }
        });
    }

    private void loadLayerHandling() {
        this.layerManager = new LayerManager();
        this.layerManager.initListeners();
        this.initLoaderListeners();
        this.northPane = new JPanel();
        this.northPane.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        this.northPane.setBorder(null);
        this.layersPanel = new ShipLayersPanel(layerManager);
        this.northPane.add(layersPanel);
        Container contentPane = this.getContentPane();
        contentPane.add(northPane, BorderLayout.PAGE_START);
    }

    private void loadShipView() {
        this.shipView = new ShipViewerPanel();
        this.southPane = new JPanel();
        this.southPane.setLayout(new GridLayout());
        this.statusPanel = new ViewerStatusPanel(this.shipView);
        this.statusPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        this.southPane.add(this.statusPanel);
        Container contentPane = this.getContentPane();
        contentPane.add(this.southPane, BorderLayout.PAGE_END);
        this.refreshContent();
    }

    private void refreshContent() {
        Container contentPane = this.getContentPane();
        contentPane.revalidate();
        contentPane.repaint();
    }

    private void loadEditingPanes() {
        this.instrumentPane = new JTabbedPane();
        this.instrumentPane.setTabPlacement(JTabbedPane.LEFT);
        pointsPanel = new BoundPointsPanel();
        instrumentPane.addTab("B",pointsPanel);
        instrumentPane.addTab("E",new JPanel());

        JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitter.setLeftComponent((Component) shipView);
        splitter.setRightComponent(instrumentPane);
        splitter.setResizeWeight(0.95);
        Container contentPane = this.getContentPane();
        contentPane.add(splitter, BorderLayout.CENTER);
        this.refreshContent();
    }

    private void dispatchLoaderEvents() {
        EventBus.publish(new ShipViewableCreated(shipView));
    }

    private void testFiles() {
        Class<? extends PrimaryWindow> windowClass = getClass();
        ClassLoader classLoader = windowClass.getClassLoader();
        URL spritePath = Objects.requireNonNull(classLoader.getResource("legion_xiv.png"));
        File sprite;
        try {
            sprite = new File(spritePath.toURI());
            Files.loadSprite(sprite);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        URL dataPath = Objects.requireNonNull(classLoader.getResource("legion.ship"));;
        try {
            URI url = dataPath.toURI();
            File hullFile = new File(url);
            Files.loadHullFile(hullFile);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    void showGUI() {
        this.setVisible(true);
        this.testFiles();
    }

}
