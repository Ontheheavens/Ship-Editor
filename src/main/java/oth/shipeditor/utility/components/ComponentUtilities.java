package oth.shipeditor.utility.components;

import com.formdev.flatlaf.ui.FlatLineBorder;
import com.formdev.flatlaf.ui.FlatRoundBorder;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.components.instrument.ship.PointList;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.painters.PainterVisibility;
import oth.shipeditor.components.viewer.painters.points.AbstractPointPainter;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.utility.Pair;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.graphics.ColorUtilities;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 22.07.2023
 */
@SuppressWarnings("ClassWithTooManyMethods")
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
        int sidePadding = 6;
        return ComponentUtilities.createBoxLabelPanel(leftName, rightValue, sidePadding);
    }

    @SuppressWarnings("WeakerAccess")
    public static JPanel createBoxLabelPanel(String leftName, JLabel rightValue, int sidePadding) {
        JPanel infoContainer = new JPanel();
        infoContainer.setLayout(new BoxLayout(infoContainer, BoxLayout.LINE_AXIS));
        JLabel label = new JLabel(leftName);
        ComponentUtilities.layoutAsOpposites(infoContainer, label, rightValue, sidePadding);
        return infoContainer;
    }

    public static void addSeparatorToBoxPanel(JPanel panel) {
        ComponentUtilities.addSeparatorToBoxPanel(panel, 5);
    }

    public static void addSeparatorToBoxPanel(JPanel panel, int height) {
        panel.add(Box.createVerticalStrut(height));
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        panel.add(separator);
    }

    private static JLabel createIconLabelWithBorder(Icon icon) {
        JLabel imageLabel = new JLabel(icon);
        imageLabel.setOpaque(true);
        imageLabel.setBorder(new FlatLineBorder(new Insets(2, 2, 2, 2), Color.GRAY));
        imageLabel.setBackground(Color.LIGHT_GRAY);
        return imageLabel;
    }

    public static JLabel createHullmodIcon(HullmodCSVEntry entry) {
        Map<String, String> rowData = entry.getRowData();
        String name = rowData.get("name");
        Image iconImage = FileLoading.loadSpriteAsImage(entry.fetchHullmodSpriteFile());
        int iconSize = 32;
        if (iconImage.getWidth(null) > iconSize || iconImage.getHeight(null) > iconSize) {
            iconImage = iconImage.getScaledInstance(iconSize, iconSize, Image.SCALE_DEFAULT);
        }
        JLabel imageLabel = ComponentUtilities.createIconLabelWithBorder(new ImageIcon(iconImage));
        imageLabel.setToolTipText(name);
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
        JMenuItem openSourceFile = new JMenuItem(StringValues.OPEN_SOURCE_FILE);
        openSourceFile.addActionListener(e -> FileUtilities.openPathInDesktop(filePath));
        openFileMenu.add(openSourceFile);
        JMenuItem openContainingFolder = new JMenuItem(StringValues.OPEN_CONTAINING_FOLDER);
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

    @SuppressWarnings("WeakerAccess")
    public static JLabel createColorIconLabel(Color color) {
        ImageIcon colorIcon = ComponentUtilities.createIconFromColor(color, 10, 10);
        return ComponentUtilities.createIconLabelWithBorder(colorIcon);
    }

    public static JPanel createFileTitlePanel(Path filePath, Path packagePath, String titleText) {
        JLabel label = new JLabel(titleText);
        Insets empty = new Insets(0, 3, 2, 4);
        label.setBorder(ComponentUtilities.createLabelSimpleBorder(empty));

        JPopupMenu contextMenu = ComponentUtilities.createPathContextMenu(filePath);

        JMenuItem openContainingPackage = new JMenuItem(StringValues.OPEN_DATA_PACKAGE);
        openContainingPackage.addActionListener(e -> FileUtilities.openPathInDesktop(packagePath));
        contextMenu.add(openContainingPackage);

        label.addMouseListener(new MouseoverLabelListener(contextMenu, label, Color.GRAY));
        label.setToolTipText(filePath.toString());

        JPanel titleContainer = new JPanel();
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.LINE_AXIS));
        titleContainer.add(label);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));

        titleContainer.add(separator);

        titleContainer.setBorder(new FlatRoundBorder());
        titleContainer.setBackground(Color.LIGHT_GRAY);
        return titleContainer;
    }

    public static JPanel createColorPropertyPanel(Component left, Color color) {
        JPanel container = new JPanel();

        container.setLayout(new BoxLayout(container, BoxLayout.LINE_AXIS));
        container.setBorder(new EmptyBorder(4, 0, 0, 0));

        JLabel colorIcon = ComponentUtilities.createColorIconLabel(color);
        colorIcon.setToolTipText(ColorUtilities.getColorBreakdown(color));

        ComponentUtilities.layoutAsOpposites(container, left, colorIcon, 0);
        return container;
    }

    public static JPanel createVisibilityWidget(JComboBox<PainterVisibility> visibilityList,
                                                Class<? extends AbstractPointPainter> painterClass,
                                                ActionListener selectionAction, String labelName) {
        String widgetLabel = labelName;
        JPanel widgetPanel = new JPanel();
        widgetPanel.setLayout(new BoxLayout(widgetPanel, BoxLayout.LINE_AXIS));

        visibilityList.setRenderer(PainterVisibility.createCellRenderer());
        visibilityList.addActionListener(PainterVisibility.createActionListener(visibilityList, painterClass));
        EventBus.subscribe(PainterVisibility.createBusEventListener(visibilityList, selectionAction));

        visibilityList.setMaximumSize(visibilityList.getPreferredSize());

        if (widgetLabel.isEmpty()) {
            widgetLabel = StringValues.PAINTER_VIEW;
        }

        JLabel visibilityWidgetLabel = new JLabel(widgetLabel);
        visibilityWidgetLabel.setToolTipText(StringValues.TOGGLED_ON_PER_LAYER_BASIS);
        widgetPanel.setBorder(new EmptyBorder(6, 0, 4, 0));

        int sidePadding = 6;
        ComponentUtilities.layoutAsOpposites(widgetPanel, visibilityWidgetLabel,
                visibilityList, sidePadding);

        return widgetPanel;
    }

    public static Insets createLabelInsets() {
        return new Insets(0, 3, 2, 4);
    }

    public static Pair<JPanel, JCheckBox> createReorderCheckboxPanel(PointList<? extends BaseWorldPoint> pointList) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.LINE_AXIS));
        container.setBorder(new EmptyBorder(2, 0, 2, 0));

        JCheckBox reorderCheckbox = new JCheckBox("Reorder by drag");
        reorderCheckbox.addItemListener(e -> {
            boolean reorderOn = reorderCheckbox.isSelected();
            pointList.setDragEnabled(reorderOn);
        });
        container.add(Box.createRigidArea(new Dimension(6,0)));
        container.add(reorderCheckbox);
        container.add(Box.createHorizontalGlue());

        return new Pair<>(container, reorderCheckbox);
    }

    public static JPanel createTitledSeparatorPanel(String text) {
        var insets = new Insets(1, 0, 0, 0);
        return ComponentUtilities.createTitledSeparatorPanel(text, insets);
    }

    public static TextScrollPanel createTextPanel(String text, int pad) {
        TextScrollPanel container = new TextScrollPanel(new FlowLayout());
        container.setBorder(new EmptyBorder(0, 0, pad, 0));
        container.setLayout(new BoxLayout(container, BoxLayout.LINE_AXIS));
        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMinimumSize(new Dimension(100,100));
        container.setMinimumSize(new Dimension(100,100));

        container.add(textArea);
        container.setAlignmentY(0);
        return container;
    }

    private static JPanel createTitledSeparatorPanel(String text, Insets insets) {
        JPanel container = new JPanel();
        container.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        ComponentUtilities.outfitPanelWithTitle(container, insets, text);
        container.setAlignmentY(0);
        return container;
    }

    public static void outfitPanelWithTitle(JPanel panel, Insets insets, String text) {
        MatteBorder matteLine = new MatteBorder(insets, Color.LIGHT_GRAY);
        Border titledBorder = new TitledBorder(matteLine, text,
                TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION);
        panel.setBorder(titledBorder);
    }

    public static Pair<JPanel, JButton> createSingleButtonPanel(String labelText, Action buttonAction) {
        JPanel topContainer = new JPanel();
        topContainer.add(new JLabel(labelText));
        JButton loadButton = new JButton(buttonAction);
        loadButton.addActionListener(Utility.scheduleTask(3000,
                e1 -> {
                    loadButton.setEnabled(false);
                    topContainer.repaint();
                },
                e1 -> {
                    loadButton.setEnabled(true);
                    topContainer.repaint();
                }));
        topContainer.add(loadButton);
        return new Pair<>(topContainer, loadButton);
    }

    /**
     * @param parent assumes that JPanel instance has GridBagLayout set as component layout.
     */
    public static JComponent addLabelAndComponent(JPanel parent, JLabel label, JComponent component, int y) {
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.insets = new Insets(3, 6, 0, 3);
        constraints.gridx = 0;
        constraints.gridy = y;
        constraints.weightx = 0.0;
        constraints.anchor = GridBagConstraints.LINE_START;
        parent.add(label, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1.0;
        constraints.gridy = y;
        if (component instanceof JLabel) {
            constraints.fill = GridBagConstraints.NONE;
            constraints.insets = new Insets(3, 3, 0, 9);
        } else {
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.insets = new Insets(3, 3, 0, 6);
        }
        constraints.anchor = GridBagConstraints.LINE_END;
        parent.add(component, constraints);
        return component;
    }

    public static JLabel getNoSelected() {
        JLabel label = new JLabel(StringValues.NO_SELECTED);
        label.setBorder(new EmptyBorder(5, 0, 5, 0));
        return label;
    }

}
