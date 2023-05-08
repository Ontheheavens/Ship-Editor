package oth.shipeditor.menubar;

import de.javagl.viewer.Viewer;
import lombok.extern.log4j.Log4j2;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.fluentui.FluentUiRegularMZ;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.PrimaryWindow;
import oth.shipeditor.components.ShipViewerControls;
import oth.shipeditor.components.ViewerPointsPanel;
import oth.shipeditor.utility.ChangeDispatchable;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

/**
 * @author Ontheheavens
 * @since 29.04.2023
 */
@Log4j2
public class PrimaryMenuBar extends JMenuBar {

    private final PrimaryWindow parent;

    private JMenuItem toggleRotate;

    public PrimaryMenuBar(PrimaryWindow parent) {
        this.parent = parent;
        this.add(createFileMenu());
        this.add(createViewMenu());
    }

    private JMenu createFileMenu() {
        JMenu fileMenu = new JMenu("File");

        JMenuItem openOption = fileMenu.add(new JMenuItem("Open"));
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "PNG Images", "png");
        chooser.setFileFilter(filter);

        openOption.setIcon(FontIcon.of(FluentUiRegularAL.FOLDER_OPEN_20, 16));
        openOption.addActionListener(l -> SwingUtilities.invokeLater(() -> {
            int returnVal = chooser.showOpenDialog(parent);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    BufferedImage sprite = ImageIO.read(file);
                    parent.getShipView().setShipSprite(sprite);
                    parent.getStatusPanel().setDimensionsLabel(sprite);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                log.info("Opening: " + file.getName() + ".");
            } else {
                log.info("Open command cancelled by user.");
            }
        }));

        return fileMenu;
    }

    private JMenu createViewMenu() {
        JMenu viewMenu = new JMenu("View");

        JMenuItem changeBackground = this.createOptionWithAssociate("Change background color",
                FluentUiRegularAL.COLOR_BACKGROUND_20, l -> SwingUtilities.invokeLater(() ->  {
                    Viewer shipView = parent.getShipView();
                    Color chosen = JColorChooser.showDialog(parent, "Choose Background", Color.GRAY);
                    shipView.setBackground(chosen);
                    shipView.repaint();
                }), parent::getShipView, parent);
        viewMenu.add(changeBackground);

        JMenuItem resetTransform = this.createOptionWithAssociate("Reset view transforms",
                FluentUiRegularMZ.PICTURE_IN_PICTURE_20, l -> SwingUtilities.invokeLater(() -> {
                    Viewer shipView = parent.getShipView();
                    shipView.resetTransform();
                    parent.getShipView().getControls().setZoomLevel(1);
                    parent.getShipView().centerViewpoint();
                }), parent::getShipView, parent);
        viewMenu.add(resetTransform);

        JMenuItem toggleRotate = this.createOptionWithAssociate("Toggle view rotation",
                FluentUiRegularAL.ARROW_ROTATE_CLOCKWISE_20, l -> SwingUtilities.invokeLater(() -> {
                    AbstractButton button = (AbstractButton) l.getSource();
                    this.toggleRotationFromMenu(button.isSelected());
                }), parent::getShipView, parent);
        viewMenu.add(toggleRotate);

        return viewMenu;
    }

    private JMenuItem createMenuOption(String text, Ikon icon, ActionListener action) {
        JMenuItem newOption = new JMenuItem(text);
        newOption.setIcon(FontIcon.of(icon, 16));
        newOption.addActionListener(action);
        return newOption;
    }

    private JMenuItem createOptionWithAssociate(String text, Ikon icon, ActionListener action,
                                                Supplier<?> associated, ChangeDispatchable instance) {
        JMenuItem newOption = createMenuOption(text, icon, action);
        if (associated.get() == null) {
            newOption.setEnabled(false);
        }
        instance.getPCS().addPropertyChangeListener("shipView", evt -> {
            newOption.setEnabled(evt.getNewValue() != null);
        });
        return newOption;
    }

    private JMenuItem createOptionWithListeners(String text, Ikon icon, ActionListener action,
                                                Supplier<?> associated, ChangeDispatchable instance,
                                                PropertyChangeListener listener) {
        JMenuItem newOption = createOptionWithAssociate(text, icon, action, associated, instance);
        instance.getPCS().addPropertyChangeListener(listener);
        return newOption;
    }

    public void toggleRotationFromMenu(boolean rotationEnabled) {
        ShipViewerControls controls = parent.getShipView().getControls();
        controls.setRotationEnabled(rotationEnabled);
        toggleRotate.setSelected(rotationEnabled);
        if (rotationEnabled) {
            ViewerPointsPanel pointsPanel = PrimaryWindow.getInstance().getPointsPanel();
            pointsPanel.setMode(ViewerPointsPanel.PointsMode.SELECT);
            pointsPanel.getSelectModeButton().setSelected(true);
        }
    }

}
