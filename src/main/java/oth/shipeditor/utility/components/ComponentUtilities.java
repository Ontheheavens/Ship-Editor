package oth.shipeditor.utility.components;

import com.formdev.flatlaf.ui.FlatLineBorder;
import com.formdev.flatlaf.ui.FlatRoundBorder;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.components.viewer.painters.PainterVisibility;
import oth.shipeditor.components.viewer.painters.points.AbstractPointPainter;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.utility.objects.Pair;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.components.containers.SortableList;
import oth.shipeditor.utility.components.containers.TextScrollPanel;
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
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.function.Consumer;

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

    @SuppressWarnings("WeakerAccess")
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
        File imageFile = entry.fetchHullmodSpriteFile();
        return ComponentUtilities.createIconFromImage(imageFile, name);
    }

    @SuppressWarnings("WeakerAccess")
    public static JLabel createIconFromImage(File imageFile, String tooltip) {
        return ComponentUtilities.createIconFromImage(imageFile, tooltip, 32);
    }

    @SuppressWarnings("WeakerAccess")
    public static JLabel createIconFromImage(File imageFile, String tooltip, int maxSize) {
        BufferedImage iconImage = FileLoading.loadSpriteAsImage(imageFile);
        return ComponentUtilities.createIconFromImage(iconImage, tooltip, maxSize);
    }

    @SuppressWarnings("WeakerAccess")
    public static JLabel createIconFromImage(BufferedImage image, String tooltip, int maxSize) {
        Image clamped = ComponentUtilities.resizeImageToSquareLimit(image, maxSize);
        JLabel imageLabel = ComponentUtilities.createIconLabelWithBorder(new ImageIcon(clamped));
        if (tooltip != null && !tooltip.isEmpty()) {
            imageLabel.setToolTipText(tooltip);
        }
        return imageLabel;
    }

    @SuppressWarnings("WeakerAccess")
    public static Image resizeImageToSquareLimit(Image iconImage, int limit) {
        int originalWidth = iconImage.getWidth(null);
        int originalHeight = iconImage.getHeight(null);

        BufferedImage resizedImage = new BufferedImage(limit, limit, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();

        if (originalWidth > limit || originalHeight > limit) {
            double widthRatio = (double) limit / originalWidth;
            double heightRatio = (double) limit / originalHeight;
            double ratio = Math.min(widthRatio, heightRatio);

            int newWidth = (int) (originalWidth * ratio);
            int newHeight = (int) (originalHeight * ratio);

            int xOffset = (limit - newWidth) / 2;
            int yOffset = (limit - newHeight) / 2;

            g2d.drawImage(iconImage, xOffset, yOffset, newWidth, newHeight, null);
        } else {
            int xOffset = (limit - originalWidth) / 2;
            int yOffset = (limit - originalHeight) / 2;

            g2d.drawImage(iconImage, xOffset, yOffset, originalWidth, originalHeight, null);
        }

        g2d.dispose();
        return resizedImage;
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
        ActionListener chooseAction = PainterVisibility.createActionListener(visibilityList, painterClass);
        return ComponentUtilities.createVisibilityWidgetRaw(visibilityList, chooseAction, selectionAction, labelName);
    }

    public static JPanel createVisibilityWidgetRaw(JComboBox<PainterVisibility> visibilityList,
                                                ActionListener chooseAction,
                                                ActionListener selectionAction, String labelName) {
        String widgetLabel = labelName;
        JPanel widgetPanel = new JPanel();
        widgetPanel.setLayout(new GridBagLayout());

        visibilityList.setRenderer(PainterVisibility.createCellRenderer());
        visibilityList.addActionListener(chooseAction);
        EventBus.subscribe(PainterVisibility.createBusEventListener(visibilityList, selectionAction));

        visibilityList.setMaximumSize(visibilityList.getPreferredSize());

        if (widgetLabel.isEmpty()) {
            widgetLabel = StringValues.PAINTER_VIEW;
        }

        JLabel visibilityWidgetLabel = new JLabel(widgetLabel);
        visibilityWidgetLabel.setToolTipText(StringValues.TOGGLED_ON_PER_LAYER_BASIS);

        ComponentUtilities.addLabelAndComponent(widgetPanel, visibilityWidgetLabel, visibilityList, 0);
        widgetPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 29));

        return widgetPanel;
    }

    public static Insets createLabelInsets() {
        return new Insets(0, 3, 2, 4);
    }

    public static<T> Pair<JPanel, JCheckBox> createReorderCheckboxPanel(SortableList<T> pointList) {
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

    public static JPanel createCSVEntryPanel(JLabel iconLabel, JComponent middleUpper,
                                             JComponent middleLower, ActionListener removeAction) {
        JPanel container = new JPanel(new GridBagLayout());
        Border flatRoundBorder = new FlatRoundBorder();
        container.setBorder(flatRoundBorder);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridheight = 2;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(4, 4, 4, 0);
        constraints.anchor = GridBagConstraints.LINE_START;

        container.add(iconLabel, constraints);

        constraints.gridx = 1;
        if (middleLower == null) {
            constraints.gridheight = 2;
            constraints.insets = new Insets(0, 4, 0, 0);
        } else {
            constraints.gridheight = 1;
            constraints.insets = new Insets(2, 4, 0, 0);
        }
        constraints.weightx = 1;
        if (middleUpper != null) {
            container.add(middleUpper, constraints);
        }

        constraints.gridy = 1;
        constraints.insets = new Insets(0, 4, 0, 0);
        if (middleLower != null) {
            container.add(middleLower, constraints);
        }

        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.weightx = 0;
        constraints.gridheight = 2;
        constraints.insets = new Insets(0, 0, 0, 4);
        constraints.anchor = GridBagConstraints.LINE_END;

        JButton removeButton = new JButton();

        removeButton.setIcon(FontIcon.of(FluentUiRegularAL.DISMISS_16, 16, Color.GRAY));
        removeButton.setRolloverIcon(FontIcon.of(FluentUiRegularAL.DISMISS_16, 16, Color.DARK_GRAY));
        removeButton.setPressedIcon(FontIcon.of(FluentUiRegularAL.DISMISS_16, 16, Color.BLACK));

        removeButton.addActionListener(removeAction);
        removeButton.setToolTipText("Remove from list");
        removeButton.putClientProperty("JButton.buttonType", "borderless");

        container.add(removeButton, constraints);

        container.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        container.setAlignmentY(0);
        return container;
    }

    /**
     * Default values for spinner are set to 0/360.
     */
    @SuppressWarnings("UnusedReturnValue")
    public static JSpinner addLabelWithSpinner(JPanel container, String labelText, Consumer<Double> spinnerEffect, int y) {
        return ComponentUtilities.addLabelWithSpinner(container, labelText, spinnerEffect, 0, 360, y);
    }

    /**
     * @param container expected to have GridBagLayout.
     * @param y vertical grid position in layout, 0 corresponds to first/top.
     */
    @SuppressWarnings("MethodWithTooManyParameters")
    public static JSpinner addLabelWithSpinner(JPanel container, String labelText,
                                               Consumer<Double> spinnerEffect,
                                               double minValue, double maxValue, int y) {
        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(0,
                minValue, maxValue, 0.5d);
        JSpinner spinner = new JSpinner(spinnerNumberModel);
        return ComponentUtilities.addLabelWithSpinner(container, labelText, spinnerEffect,
                spinner, spinnerNumberModel, minValue, maxValue, y);
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    public static JSpinner addLabelWithSpinner(JPanel container, String labelText,
                                               Consumer<Double> spinnerEffect, JSpinner spinner,
                                               SpinnerNumberModel spinnerNumberModel,
                                               double minValue, double maxValue, int y) {
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.insets = new Insets(3, 10, 0, 3);
        constraints.gridx = 0;
        constraints.gridy = y;
        constraints.weightx = 0.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.LINE_START;

        JLabel selectorLabel = new JLabel(labelText);
        container.add(selectorLabel, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1.0;
        constraints.gridy = y;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(3, 3, 0, 6);
        constraints.anchor = GridBagConstraints.LINE_END;

        spinner.addChangeListener(e -> {
            Number modelNumber = spinnerNumberModel.getNumber();
            double current = modelNumber.doubleValue();
            if (spinnerEffect != null) {
                spinnerEffect.accept(current);
            }
        });
        spinner.addMouseWheelListener(e -> {
            if (e.getScrollType() != MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                return;
            }
            double value = (Double) spinner.getValue();
            double newValue = value - e.getUnitsToScroll();
            newValue = Math.min(maxValue, Math.max(minValue, newValue));
            spinner.setValue(newValue);
        });

        container.add(spinner, constraints);
        return spinner;
    }

    public static JPanel createHintPanel(String text, FontIcon icon) {
        JPanel hintPanel = new JPanel();
        hintPanel.setLayout(new BoxLayout(hintPanel, BoxLayout.LINE_AXIS));

        JLabel hintIcon = new JLabel(icon);
        hintIcon.setBorder(new EmptyBorder(4, 4, 4, 0));
        hintIcon.setAlignmentY(0.5f);
        hintPanel.add(hintIcon);

        JPanel hintInfo = ComponentUtilities.createTextPanel(text, 2);
        hintInfo.setBorder(new EmptyBorder(4, 0, 4, 4));
        hintInfo.setAlignmentY(0.5f);
        hintPanel.add(hintInfo);

        hintPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        Insets insets = new Insets(1, 0, 0, 0);
        return hintPanel;
    }

}
