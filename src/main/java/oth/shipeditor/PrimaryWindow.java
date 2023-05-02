package oth.shipeditor;

import lombok.Getter;
import oth.shipeditor.components.ShipViewerPanel;
import oth.shipeditor.components.ViewerPointsPanel;
import oth.shipeditor.components.ViewerStatusPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

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
        mainFrame.getContentPane().add(shipView.getViewer(), BorderLayout.CENTER);

        pointsPanel = new ViewerPointsPanel();
        mainFrame.getContentPane().add(pointsPanel, BorderLayout.EAST);
        mainFrame.addComponentListener(new ComponentAdapter(){
            @Override
            public void componentResized(ComponentEvent e){
                shipView.centerViewpoint();
            }
        });

        primaryMenu = new PrimaryMenuBar(this);
        mainFrame.setJMenuBar(primaryMenu.getMenuBar());

        southPane = new JPanel();
        southPane.setLayout(new GridLayout());
        statusPanel = new ViewerStatusPanel();
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        southPane.add(statusPanel);
        mainFrame.getContentPane().add(southPane, BorderLayout.SOUTH);
    }

    public void showWindow() {
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

}
