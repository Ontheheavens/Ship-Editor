package oth.shipeditor;

import lombok.Getter;
import oth.shipeditor.components.ShipViewerPanel;
import oth.shipeditor.components.ViewerPointsPanel;
import oth.shipeditor.components.ViewerStatusPanel;
import oth.shipeditor.data.HullData;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * @author Ontheheavens
 * @since 27.04.2023
 */
public class PrimaryWindow {

    @Getter
    private static final PrimaryWindow instance = new PrimaryWindow();
    @Getter
    private final JFrame mainFrame;
    @Getter
    private final ShipViewerPanel shipView;
    @Getter
    private final PrimaryMenuBar primaryMenu;
    @Getter
    private final ViewerPointsPanel pointsPanel;
    @SuppressWarnings("FieldCanBeLocal")
    private final JPanel southPane;
    private final JTabbedPane instrumentPane;


    @Getter
    private final ViewerStatusPanel statusPanel;

    private PrimaryWindow() {
        mainFrame = new JFrame("Ship Editor");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainFrame.setMinimumSize(new Dimension(800, 600));
        mainFrame.setLocationRelativeTo(null);
        mainFrame.getContentPane().setLayout(new BorderLayout());

        shipView = new ShipViewerPanel();
        shipView.getViewer().setBackground(Color.GRAY);

        instrumentPane = new JTabbedPane();
        instrumentPane.setTabPlacement(JTabbedPane.LEFT);
        pointsPanel = new ViewerPointsPanel();
        instrumentPane.addTab("B",pointsPanel);
        instrumentPane.addTab("E",new JPanel());

        JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitter.setLeftComponent(shipView.getViewer());
        splitter.setRightComponent(instrumentPane);
        splitter.setResizeWeight(1);

        mainFrame.getContentPane().add(splitter, BorderLayout.CENTER);

        primaryMenu = new PrimaryMenuBar(this);
        mainFrame.setJMenuBar(primaryMenu.getMenuBar());

        southPane = new JPanel();
        southPane.setLayout(new GridLayout());
        statusPanel = new ViewerStatusPanel();
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        southPane.add(statusPanel);
        mainFrame.getContentPane().add(southPane, BorderLayout.SOUTH);
        mainFrame.pack();
    }

    public void showWindow() {
        URI dataPath;
        try {
            dataPath = Objects.requireNonNull(getClass().getClassLoader().getResource("legion.ship")).toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        new HullData(dataPath);

        mainFrame.setVisible(true);
    }

}
