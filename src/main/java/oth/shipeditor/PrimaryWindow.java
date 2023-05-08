package oth.shipeditor;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.BoundPointsPanel;
import oth.shipeditor.components.ShipViewerPanel;
import oth.shipeditor.components.ViewerStatusPanel;
import oth.shipeditor.components.control.ShipViewerControls;
import oth.shipeditor.data.ShipData;
import oth.shipeditor.menubar.PrimaryMenuBar;
import oth.shipeditor.utility.ChangeDispatchable;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeSupport;

/**
 * @author Ontheheavens
 * @since 27.04.2023
 */
@SuppressWarnings("FieldCanBeLocal")
@Log4j2
public class PrimaryWindow extends JFrame implements ChangeDispatchable {

    @Getter
    private static final PrimaryWindow instance = new PrimaryWindow();

    @Getter
    private final PrimaryMenuBar primaryMenu;

    private final PropertyChangeSupport fieldChangeDispatcher = new PropertyChangeSupport(this);

    /**
     * Runtime representation of JSON ship file.
     */
    @Getter
    private ShipData shipData;

    /**
     * Loaded instance of PNG ship sprite.
     */
    @Getter
    private BufferedImage shipSprite;

    /**
     * Complex component responsible for ship sprite display.
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

    private PrimaryWindow() {
        // Frame initialization block.
        this.setTitle("Ship Editor");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setMinimumSize(new Dimension(800, 600));
        this.setLocationRelativeTo(null);
        this.getContentPane().setLayout(new BorderLayout());

        primaryMenu = new PrimaryMenuBar(this);
        this.setJMenuBar(primaryMenu);


        this.pack();
    }

    /**
     *  Meant to be called when the PNG sprite file loads.
     */
    public void loadShipView(BufferedImage shipSprite) {

        this.setShipView(new ShipViewerPanel());
        this.shipSprite = shipSprite;
        shipView.loadShipSprite(shipSprite);

        southPane = new JPanel();
        southPane.setLayout(new GridLayout());
        statusPanel = new ViewerStatusPanel();
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        statusPanel.setDimensionsLabel(shipSprite);
        southPane.add(statusPanel);
        this.getContentPane().add(southPane, BorderLayout.SOUTH);
    }

    public void setShipData(ShipData data) {

    }

    /**
     *  Meant to be called when ship JSON file loads.
     */
    public void loadEditingPanes(ShipData data) {

        data.initialize(this.getShipView());

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
    }

    public static ShipViewerControls getControls() {
        if (getInstance().getShipView() != null) {
            return getInstance().getShipView().getControls();
        } else throw new RuntimeException("GetControls() was called on null ShipView!");
    }

    @Override
    public PropertyChangeSupport getPCS() {
        return this.fieldChangeDispatcher;
    }

    public void setShipView(ShipViewerPanel newPanel) {
        ShipViewerPanel old = this.shipView;
        if (old != null) {
            this.remove(old);
        }
        this.shipView = newPanel;
        this.fieldChangeDispatcher.firePropertyChange("shipView", old, this.shipView);
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
