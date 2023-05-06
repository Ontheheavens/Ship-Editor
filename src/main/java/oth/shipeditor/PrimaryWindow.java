package oth.shipeditor;

import lombok.Getter;
import oth.shipeditor.components.ShipViewerPanel;
import oth.shipeditor.components.ViewerPointsPanel;
import oth.shipeditor.components.ViewerStatusPanel;
import oth.shipeditor.data.ShipData;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * @author Ontheheavens
 * @since 27.04.2023
 */
@SuppressWarnings("FieldCanBeLocal")
public class PrimaryWindow extends JFrame {

    @Getter
    private static final PrimaryWindow instance = new PrimaryWindow();

    @Getter
    private final ShipViewerPanel shipView;
    @Getter
    private final PrimaryMenuBar primaryMenu;
    @Getter
    private final ViewerPointsPanel pointsPanel;
    private final JPanel southPane;
    private final JTabbedPane instrumentPane;

    @Getter
    private ShipData shipData;

    @Getter
    private final ViewerStatusPanel statusPanel;

    private PrimaryWindow() {
        // Frame initialization.
        this.setTitle("Ship Editor");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setMinimumSize(new Dimension(800, 600));
        this.setLocationRelativeTo(null);
        this.getContentPane().setLayout(new BorderLayout());

        shipView = new ShipViewerPanel();


        instrumentPane = new JTabbedPane();
        instrumentPane.setTabPlacement(JTabbedPane.LEFT);
        pointsPanel = new ViewerPointsPanel();
        instrumentPane.addTab("B",pointsPanel);
        instrumentPane.addTab("E",new JPanel());

        JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitter.setLeftComponent(shipView);
        splitter.setRightComponent(instrumentPane);
        splitter.setResizeWeight(0.95);

        this.getContentPane().add(splitter, BorderLayout.CENTER);

        primaryMenu = new PrimaryMenuBar(this);
        this.setJMenuBar(primaryMenu.getMenuBar());

        southPane = new JPanel();
        southPane.setLayout(new GridLayout());
        statusPanel = new ViewerStatusPanel();
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        southPane.add(statusPanel);
        this.getContentPane().add(southPane, BorderLayout.SOUTH);
        this.pack();
    }

    private void initializeComponents() {
        shipView.initialize();
    }

    public void showGUI() {
        URI dataPath;
        try {
            dataPath = Objects.requireNonNull(getClass().getClassLoader().getResource("legion.ship")).toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        this.initializeComponents();
        shipData = new ShipData(dataPath);
        this.setVisible(true);
    }

}
