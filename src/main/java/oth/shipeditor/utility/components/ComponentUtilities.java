package oth.shipeditor.utility.components;

import com.formdev.flatlaf.ui.FlatLineBorder;
import com.formdev.flatlaf.ui.FlatRoundBorder;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.utility.Pair;
import oth.shipeditor.utility.StringConstants;
import oth.shipeditor.utility.graphics.ColorUtilities;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * @author Ontheheavens
 * @since 22.07.2023
 */
public final class ComponentUtilities {


    private ComponentUtilities() {
    }

    /**
     * Arranges two components as opposites within a container with optional side padding.
     * It is assumed that container's layout is set to BoxLayout.
     * @param sidePadding The amount of horizontal padding (in pixels) to be added on both sides. Use 0 for no padding.
     */
    public static void layoutAsOpposites(JPanel container, Component left, Component right, int sidePadding) {
        if (!(container.getLayout() instanceof BoxLayout)) {
            throw new IllegalArgumentException("Passed container is not set to BoxLayout!");
        }
        container.add(Box.createRigidArea(new Dimension(sidePadding,0)));
        container.add(left);
        container.add(Box.createHorizontalGlue());
        container.add(right);
        container.add(Box.createRigidArea(new Dimension(sidePadding,0)));
    }

    public static JPanel createBoxLabelPanel(String leftName, JLabel rightValue) {
        JPanel infoContainer = new JPanel();
        infoContainer.setLayout(new BoxLayout(infoContainer, BoxLayout.LINE_AXIS));
        JLabel label = new JLabel(leftName);
        int sidePadding = 6;
        ComponentUtilities.layoutAsOpposites(infoContainer, label, rightValue, sidePadding);
        return infoContainer;
    }

    public static void addSeparatorToBoxPanel(JPanel panel) {
        panel.add(Box.createVerticalStrut(5));
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 5));
        panel.add(separator);
    }

    public static JLabel createIconLabelWithBorder(Icon icon) {
        JLabel imageLabel = new JLabel(icon);
        imageLabel.setOpaque(true);
        imageLabel.setBorder(new FlatLineBorder(new Insets(2, 2, 2, 2), Color.GRAY));
        imageLabel.setBackground(Color.LIGHT_GRAY);
        return imageLabel;
    }

    public static Pair<JSlider, JLabel> createOpacityWidget(ChangeListener change,
                                                            BusEventListener eventListener) {
        JSlider opacitySlider = new JSlider(SwingConstants.HORIZONTAL,
                0, 100, 100);
        opacitySlider.setAlignmentX(0.0f);
        opacitySlider.setEnabled(false);
        opacitySlider.setSnapToTicks(true);
        opacitySlider.addChangeListener(change);
        EventBus.subscribe(eventListener);
        Dictionary<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(0, new JLabel("0%"));
        labelTable.put(50, new JLabel("50%"));
        labelTable.put(100, new JLabel("100%"));
        opacitySlider.setLabelTable(labelTable);
        opacitySlider.setMajorTickSpacing(50);
        opacitySlider.setMinorTickSpacing(10);
        opacitySlider.setPaintTicks(true);
        opacitySlider.setPaintLabels(true);
        JLabel opacityLabel = new JLabel();
        opacityLabel.setAlignmentX(0.0f);
        return new Pair<>(opacitySlider, opacityLabel);
    }

    public static JPopupMenu createPathContextMenu(Path filePath) {
        JPopupMenu openFileMenu = new JPopupMenu();
        JMenuItem openSourceFile = new JMenuItem(StringConstants.OPEN_SOURCE_FILE);
        openSourceFile.addActionListener(e -> FileUtilities.openPathInDesktop(filePath));
        openFileMenu.add(openSourceFile);
        JMenuItem openContainingFolder = new JMenuItem(StringConstants.OPEN_CONTAINING_FOLDER);
        openContainingFolder.addActionListener(e -> FileUtilities.openPathInDesktop(filePath.getParent()));
        openFileMenu.add(openContainingFolder);

        return openFileMenu;
    }

    public static Border createRoundCompoundBorder(Insets insets) {
        return ComponentUtilities.createRoundCompoundBorder(insets, false);
    }

    @SuppressWarnings("BooleanParameter")
    public static Border createRoundCompoundBorder(Insets insets, boolean reversed) {
        Border empty = new EmptyBorder(insets);
        Border lineBorder = new FlatRoundBorder();
        if (reversed) {
            return BorderFactory.createCompoundBorder(empty, lineBorder);
        } else return BorderFactory.createCompoundBorder(lineBorder, empty);
    }

    public static Border createLabelSimpleBorder(Insets insets) {
        return new FlatLineBorder(insets, Color.LIGHT_GRAY);
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public static ImageIcon createIconFromColor(Color color, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(color);
        graphics.fillRect (0, 0, width, height);
        return new ImageIcon(image);
    }

    public static JLabel createColorIconLabel(Color color) {
        ImageIcon colorIcon = ComponentUtilities.createIconFromColor(color, 10, 10);
        return ComponentUtilities.createIconLabelWithBorder(colorIcon);
    }

    public static JPanel createColorPropertyPanel(String name, Color color) {
        JPanel container = new JPanel();

        container.setLayout(new BoxLayout(container, BoxLayout.LINE_AXIS));
        container.setBorder(new EmptyBorder(4, 0, 0, 0));

        JLabel colorLabel = new JLabel(name);
        JLabel colorIcon = ComponentUtilities.createColorIconLabel(color);
        colorIcon.setToolTipText(ColorUtilities.getColorBreakdown(color));

        ComponentUtilities.layoutAsOpposites(container, colorLabel, colorIcon, 0);
        return container;
    }

}
