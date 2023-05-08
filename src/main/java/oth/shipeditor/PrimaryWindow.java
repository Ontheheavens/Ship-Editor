package oth.shipeditor;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.ShipViewerPanel;
import oth.shipeditor.components.ViewerPointsPanel;
import oth.shipeditor.components.ViewerStatusPanel;
import oth.shipeditor.data.ShipData;
import oth.shipeditor.menubar.PrimaryMenuBar;
import oth.shipeditor.utility.ChangeDispatchable;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeSupport;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

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
    private ShipViewerPanel shipView = null;

    private final PropertyChangeSupport fieldChangeDispatcher = new PropertyChangeSupport(this);
    @Getter
    private final PrimaryMenuBar primaryMenu;
    @Getter
    private ViewerPointsPanel pointsPanel = null;
    private JPanel southPane = null;
    private JTabbedPane instrumentPane = null;

//    private final JTabbedPane fileTabContainer;

    @Getter
    private ShipData shipData;

    @Getter
    private ViewerStatusPanel statusPanel;

    private PrimaryWindow() {
        // Frame initialization.
        this.setTitle("Ship Editor");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setMinimumSize(new Dimension(800, 600));
        this.setLocationRelativeTo(null);
        this.getContentPane().setLayout(new BorderLayout());

        primaryMenu = new PrimaryMenuBar(this);
        this.setJMenuBar(primaryMenu);

        this.setShipView(new ShipViewerPanel());

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

        southPane = new JPanel();
        southPane.setLayout(new GridLayout());
        statusPanel = new ViewerStatusPanel();
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        southPane.add(statusPanel);
        this.getContentPane().add(southPane, BorderLayout.SOUTH);
        this.pack();

        String name;
        try {
//            this.getClass().getDeclaredField("fieldChangeDispatcher").setAccessible(true);
            name = this.getClass().getDeclaredField("fieldChangeDispatcher").getName();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        log.info(name);
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

//    private void noteFieldChanged(String fieldName,  Object oldValue, Object newValue) {
//        this.getFieldChangeDispatcher().firePropertyChange(fieldName, oldValue, newValue);
//    }
//
//    public void setComponent(C instance, AccessorUtilities.ComponentGetter<T, C> getter, T newComponent) {
//        T old = getter.getComponent(instance);
//        if (old != null) {
//            instance.remove(old);
//        }
//        noteFieldChanged();
//        if (newComponent != null) {
//            instance.add(newComponent);
//        }
//    }

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
