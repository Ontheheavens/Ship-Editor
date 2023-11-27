package oth.shipeditor.utility.components.rendering;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.utility.components.ComponentUtilities;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 24.09.2023
 */
@SuppressWarnings("ClassWithTooManyFields")
public class TreePanelCellRenderer extends JPanel implements TreeCellRenderer {

    protected boolean selected;

    private Color treeBGColor;

    private Color focusBGColor;

    @Getter @Setter
    protected transient Icon closedIcon;

    @Getter @Setter
    protected transient Icon leafIcon;

    @Getter @Setter
    protected transient Icon openIcon;


    @Getter @Setter
    protected Color textSelectionColor;


    @Getter @Setter
    protected Color textNonSelectionColor;


    @Getter @Setter
    protected Color backgroundSelectionColor;


    @Getter @Setter
    protected Color backgroundNonSelectionColor;

    @Getter @Setter
    protected Color borderSelectionColor;

    @Getter
    private boolean isDropCell;

    @Getter @Setter
    private boolean fillBackground;

    private static final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);

    private final boolean initialized;

    @Getter
    private final JPanel leftContainer;

    @Getter
    private final JPanel rightContainer;

    @Getter
    private final JLabel iconLabel;

    @Getter
    private final JLabel textLabel;

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    TreePanelCellRenderer() {
        this.setOpaque(false);
        setBorder(DEFAULT_NO_FOCUS_BORDER);
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        leftContainer = new JPanel();
        leftContainer.setOpaque(false);
        leftContainer.setLayout(new BoxLayout(leftContainer, BoxLayout.LINE_AXIS));
        rightContainer = new JPanel();
        rightContainer.setOpaque(false);
        rightContainer.setLayout(new BoxLayout(rightContainer, BoxLayout.LINE_AXIS));

        Border border = new EmptyBorder(0, 2, 0, 0);
        rightContainer.setBorder(border);

        iconLabel = new JLabel();
        textLabel = new JLabel();
        textLabel.setBorder(border);

        leftContainer.add(iconLabel);
        leftContainer.add(textLabel);

        ComponentUtilities.layoutAsOpposites(this, leftContainer, rightContainer, 4);

        initialized = true;
    }

    public void updateUI() {
        super.updateUI();

        setUIProperties();

        fillBackground = UIManager.getBoolean("Tree.rendererFillBackground");
        if (!initialized || getBorder() instanceof UIResource)  {
            Insets margins = UIManager.getInsets("Tree.rendererMargins");
            if (margins != null) {
                setBorder(new BorderUIResource.EmptyBorderUIResource(margins));
            } else {
                setBorder(new BorderUIResource.EmptyBorderUIResource(0, 0, 0, 0));
            }
        }

        setName("Tree.cellRenderer");
    }

    @SuppressWarnings({"OverlyComplexMethod", "ChainOfInstanceofChecks"})
    private void setUIProperties() {
        if (!initialized || (getLeafIcon() instanceof UIResource)) {
            setLeafIcon(UIManager.getIcon("Tree.leafIcon"));
        }
        if (!initialized || (getClosedIcon() instanceof UIResource)) {
            setClosedIcon(UIManager.getIcon("Tree.closedIcon"));
        }
        if (!initialized || (getOpenIcon() instanceof UIResource)) {
            setOpenIcon(UIManager.getIcon("Tree.openIcon"));
        }
        if (!initialized || (getTextSelectionColor() instanceof UIResource)) {
            setTextSelectionColor(UIManager.getColor("Tree.selectionForeground"));
        }
        if (!initialized || (getTextNonSelectionColor() instanceof UIResource)) {
            setTextNonSelectionColor(UIManager.getColor("Tree.textForeground"));
        }
        if (!initialized || (getBackgroundSelectionColor() instanceof UIResource)) {
            setBackgroundSelectionColor(UIManager.getColor("Tree.selectionBackground"));
        }
        if (!initialized || (getBackgroundNonSelectionColor() instanceof UIResource)) {
            setBackgroundNonSelectionColor(UIManager.getColor("Tree.textBackground"));
        }
        if (!initialized || (getBorderSelectionColor() instanceof UIResource)) {
            setBorderSelectionColor(UIManager.getColor("Tree.selectionBorderColor"));
        }
    }

    @Override
    public void setForeground(Color fg) {
        super.setForeground(fg);
        if (leftContainer != null) {
            leftContainer.setForeground(fg);
        }
        if (rightContainer != null) {
            rightContainer.setForeground(fg);
        }
        if (textLabel != null) {
            textLabel.setForeground(fg);
        }
    }

    @SuppressWarnings("ParameterHidesMemberVariable")
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean selected,
                                                  boolean expanded,
                                                  boolean leaf, int row,
                                                  boolean hasFocus) {
        String stringValue = tree.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
        textLabel.setText(stringValue);
        Color fg;
        isDropCell = false;

        JTree.DropLocation dropLocation = tree.getDropLocation();
        if (dropLocation != null
                && dropLocation.getChildIndex() == -1
                && tree.getRowForPath(dropLocation.getPath()) == row) {
            Color col = UIManager.getColor("Tree.dropCellForeground");
            if (col != null) {
                fg = col;
            } else {
                fg = getTextSelectionColor();
            }

            isDropCell = true;
        } else if (selected && hasFocus) {
            fg = getTextSelectionColor();
        } else {
            fg = getTextNonSelectionColor();
        }

        setForeground(fg);

        Icon icon;
        if (leaf) {
            icon = getLeafIcon();
        } else if (expanded) {
            icon = getOpenIcon();
        } else {
            icon = getClosedIcon();
        }

        if (tree.isEnabled()) {
            setEnabled(true);
            iconLabel.setIcon(icon);
        } else {
            setEnabled(false);
            LookAndFeel laf = UIManager.getLookAndFeel();
            Icon disabledIcon = laf.getDisabledIcon(tree, icon);
            if (disabledIcon != null) {
                iconLabel.setIcon(disabledIcon);
            }
        }
        setComponentOrientation(tree.getComponentOrientation());

        this.selected = selected;

        return this;
    }

    public void paint(Graphics g) {
        Color bColor;

        if (isDropCell) {
            bColor = UIManager.getColor("Tree.dropCellBackground");
            if (bColor == null) {
                bColor = getBackgroundSelectionColor();
            }
        } else if (selected) {
            bColor = getBackgroundSelectionColor();
        } else {
            bColor = getBackgroundNonSelectionColor();
            if (bColor == null) {
                bColor = getBackground();
            }
        }

        int imageOffset;
        ComponentOrientation componentOrientation = getComponentOrientation();
        if (bColor != null && fillBackground) {
            imageOffset = 0;
            g.setColor(bColor);
            if(componentOrientation.isLeftToRight()) {
                g.fillRect(imageOffset, 0, getWidth() - imageOffset,
                        getHeight());
            } else {
                g.fillRect(0, 0, getWidth() - imageOffset,
                        getHeight());
            }
        }
        super.paint(g);
    }

    public void repaint(long tm, int x, int y, int width, int height) {}

    public void repaint(Rectangle r) {}

    public void repaint() {}

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}

    public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {}

    public void firePropertyChange(String propertyName, char oldValue, char newValue) {}

    public void firePropertyChange(String propertyName, short oldValue, short newValue) {}

    public void firePropertyChange(String propertyName, int oldValue, int newValue) {}

    public void firePropertyChange(String propertyName, long oldValue, long newValue) {}

    public void firePropertyChange(String propertyName, float oldValue, float newValue) {}

    public void firePropertyChange(String propertyName, double oldValue, double newValue) {}

    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}

}
