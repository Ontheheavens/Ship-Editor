package oth.shipeditor.menubar;

import de.javagl.viewer.Viewer;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.fluentui.FluentUiRegularMZ;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.PrimaryWindow;
import oth.shipeditor.components.control.ShipViewerControls;
import oth.shipeditor.utility.ChangeDispatchable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.function.Supplier;

/**
 * @author Ontheheavens
 * @since 29.04.2023
 */
@Log4j2
public class PrimaryMenuBar extends JMenuBar {

    private final PrimaryWindow parent;

    private FileMenu fileMenu;

    @Getter
    private JMenuItem toggleRotate;

    public PrimaryMenuBar(PrimaryWindow parent) {
        this.parent = parent;
        this.add(createFileMenu());
        this.add(createViewMenu());
    }

    private JMenu createFileMenu() {
        fileMenu = new FileMenu();
        fileMenu.initialize();
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
                }), parent::getShipView, "shipView", parent, false);
        viewMenu.add(changeBackground);

        JMenuItem resetTransform = this.createOptionWithAssociate("Reset view transforms",
                FluentUiRegularMZ.PICTURE_IN_PICTURE_20, l -> SwingUtilities.invokeLater(() -> {
                    Viewer shipView = parent.getShipView();
                    shipView.resetTransform();
                    parent.getShipView().getControls().setZoomLevel(1);
                    parent.getShipView().centerViewpoint();
                }), parent::getShipView, "shipView",parent, false);
        viewMenu.add(resetTransform);

        toggleRotate = this.createOptionWithAssociate("Toggle view rotation",
                FluentUiRegularAL.ARROW_ROTATE_CLOCKWISE_20, l -> SwingUtilities.invokeLater(() -> {
                    AbstractButton button = (AbstractButton) l.getSource();
                    ShipViewerControls controls = parent.getShipView().getControls();
                    controls.setRotationEnabled(button.isSelected());
                }), parent::getShipView, "shipView",parent, true);
        viewMenu.add(toggleRotate);

        return viewMenu;
    }

    private JMenuItem createMenuOption(String text, Ikon icon, ActionListener action, boolean checkbox) {
        JMenuItem newOption;
        if (checkbox) {
            newOption = new JCheckBoxMenuItem(text);
        } else {
            newOption = new JMenuItem(text);
        }
        newOption.setIcon(FontIcon.of(icon, 16));
        newOption.addActionListener(action);
        return newOption;
    }

    /**
     * Creates a menu option with an association to a component. If the associated component is null,
     * the option will be unavailable.
     * @param text       The text for the menu option.
     * @param icon       The icon for the menu option.
     * @param action     The action listener for the menu option.
     * @param getter     A supplier that provides the associated component - e.g. instance method reference.
     * @param instance   An instance implementing the {@link ChangeDispatchable} interface.
     * @return The created {@link JMenuItem} option.
     */
    private JMenuItem createOptionWithAssociate(String text, Ikon icon, ActionListener action,
                                                Supplier<?> getter, String field,
                                                ChangeDispatchable instance, boolean checkbox) {
        JMenuItem newOption = createMenuOption(text, icon, action, checkbox);
        if (getter.get() == null) {
            newOption.setEnabled(false);
        }
        instance.getPCS().addPropertyChangeListener(field,
                evt -> newOption.setEnabled(evt.getNewValue() != null));
        return newOption;
    }

    public void initToggleRotateOption(ShipViewerControls controls) {
        PropertyChangeListener rotationChangeListener = evt ->
                toggleRotate.setSelected(controls.isRotationEnabled());
        controls.getPCS().addPropertyChangeListener("rotationEnabled", rotationChangeListener);
        toggleRotate.setSelected(controls.isRotationEnabled());
    }


}
