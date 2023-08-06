package oth.shipeditor.utility.components;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.plaf.metal.MetalTabbedPaneUI;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * @author Terai Atsuhiro, refactored by Ontheheavens
 * @since 24.07.2023
 */
@SuppressWarnings("AbstractClassWithOnlyOneDirectInheritor")
public abstract class SortableTabbedPane extends JTabbedPane {

    private static final String SCROLL_TABS_BACKWARD_ACTION = "scrollTabsBackwardAction";
    private static final String SCROLL_TABS_FORWARD_ACTION = "scrollTabsForwardAction";
    private static final String TABBED_PANE_TEXT_ICON_GAP = "TabbedPane.textIconGap";
    private final TabbedGlassPane glassPane;

    @Getter
    private final DropTarget dropTarget;

    @Getter @Setter
    private int dragTabIndex = -1;

    private Rectangle backwardScroller = new Rectangle();
    private Rectangle forwardScroller = new Rectangle();

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    protected SortableTabbedPane() {
        glassPane = new TabbedGlassPane(this);
        int ops = DnDConstants.ACTION_COPY_OR_MOVE;
        dropTarget = new DropTarget(glassPane, ops, new TabDropTargetListener(), true);
        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
                this, ops, new TabDragGestureListener());
    }

    @SuppressWarnings("MethodOnlyUsedFromInnerClass")
    private int getTargetTabIndex(Point glassPoint) {
        Point tabPoint = SwingUtilities.convertPoint(glassPane, glassPoint, this);
        Point point;
        if (SortableTabbedPane.isTopBottomTabPlacement(getTabPlacement())) {
            point = new Point(1, 0);
        } else {
            point = new Point(0, 1);
        }
        IntStream intStream = IntStream.range(0, getTabCount()).filter(index -> {
            Rectangle bounds = getBoundsAt(index);
            bounds.translate(-bounds.width * point.x / 2, -bounds.height * point.y / 2);
            return bounds.contains(tabPoint);
        });
        IntSupplier intSupplier = () -> {
            int count = getTabCount();
            Rectangle bounds = getBoundsAt(count - 1);
            bounds.translate(bounds.width * point.x / 2, bounds.height * point.y / 2);
            return bounds.contains(tabPoint) ? count : -1;
        };
        OptionalInt first = intStream.findFirst();
        return first.orElseGet(intSupplier);
    }

    @SuppressWarnings("MethodOnlyUsedFromInnerClass")
    private void initGlassPane(Point tabPoint) {
        JRootPane rootPane = getRootPane();
        rootPane.setGlassPane(glassPane);

        Component component = this.getTabComponentAt(dragTabIndex);
        Supplier<Component> componentSupplier = () -> {
            String title = getTitleAt(dragTabIndex);
            Icon icon = getIconAt(dragTabIndex);
            JLabel label = new JLabel(title, icon, SwingConstants.LEADING);
            label.setIconTextGap(UIManager.getInt(TABBED_PANE_TEXT_ICON_GAP));
            return label;
        };
        Component copy = Optional.ofNullable(component).orElseGet(componentSupplier);
        Dimension preferredSize = copy.getPreferredSize();
        BufferedImage image = new BufferedImage(preferredSize.width, preferredSize.height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = image.createGraphics();
        SwingUtilities.paintComponent(graphics2D, copy, glassPane, 0, 0,
                preferredSize.width, preferredSize.height);
        graphics2D.dispose();
        glassPane.setDraggingGhost(image);
        if (component != null) {
            setTabComponentAt(dragTabIndex, component);
        }

        Point glassPt = SwingUtilities.convertPoint(this, tabPoint, glassPane);
        glassPane.setPoint(glassPt);
        glassPane.setVisible(true);
    }

    private static boolean isTopBottomTabPlacement(int tabPlacement) {
        return tabPlacement == SwingConstants.TOP || tabPlacement == SwingConstants.BOTTOM;
    }

    protected abstract void sortTabObjects();

    private class TabDropTargetListener extends DropTargetAdapter {
        private static final Point HIDDEN_POINT = new Point(0, -1000);

        private static Optional<TabbedGlassPane> getGlassPane(Component component) {
            Class<TabbedGlassPane> paneClass = TabbedGlassPane.class;
            Predicate<Component> isInstance = paneClass::isInstance;
            Function<Component, TabbedGlassPane> cast = paneClass::cast;
            return Optional.ofNullable(component).filter(isInstance).map(cast);
        }

        @Override public void dragEnter(DropTargetDragEvent dtde) {
            DropTargetContext dropTargetContext = dtde.getDropTargetContext();
            Component component = dropTargetContext.getComponent();
            Optional<TabbedGlassPane> glassPaneOptional = TabDropTargetListener.getGlassPane(component);
            glassPaneOptional.ifPresent(pane -> {
                Transferable transferable = dtde.getTransferable();
                DataFlavor[] dataFlavors = dtde.getCurrentDataFlavors();
                if (transferable.isDataFlavorSupported(dataFlavors[0])) {
                    dtde.acceptDrag(dtde.getDropAction());
                } else {
                    dtde.rejectDrag();
                }
            });
        }

        @Override public void dragExit(DropTargetEvent dte) {
            DropTargetContext dropTargetContext = dte.getDropTargetContext();
            Component component = dropTargetContext.getComponent();
            Optional<TabbedGlassPane> glassPaneOptional = TabDropTargetListener.getGlassPane(component);
            glassPaneOptional.ifPresent(pane -> {
                pane.setPoint(HIDDEN_POINT);
                pane.setTargetRect(0, 0, 0, 0);
                pane.repaint();
            });
        }

        @Override public void dragOver(DropTargetDragEvent dtde) {
            DropTargetContext dropTargetContext = dtde.getDropTargetContext();
            Component component = dropTargetContext.getComponent();
            Optional<TabbedGlassPane> glassPaneOptional = TabDropTargetListener.getGlassPane(component);
            glassPaneOptional.ifPresent(pane -> {
                Point glassPt = dtde.getLocation();

                SortableTabbedPane tabbedPane = pane.getTabbedPane();
                this.initTargetLine(tabbedPane.getTargetTabIndex(glassPt));
                this.autoScrollTest(glassPt);

                pane.setPoint(glassPt);
                pane.repaint();
            });
        }

        @Override public void drop(DropTargetDropEvent dtde) {
            DropTargetContext dropTargetContext = dtde.getDropTargetContext();
            Component c = dropTargetContext.getComponent();
            Optional<TabbedGlassPane> tabbedGlassPane = TabDropTargetListener.getGlassPane(c);
            tabbedGlassPane.ifPresent(pane -> {
                SortableTabbedPane tabbedPane = pane.getTabbedPane();
                Transferable transferable = dtde.getTransferable();
                DataFlavor[] dataFlavors = transferable.getTransferDataFlavors();
                int dragged = tabbedPane.getDragTabIndex();
                int target = tabbedPane.getTargetTabIndex(dtde.getLocation());
                if (transferable.isDataFlavorSupported(dataFlavors[0]) && dragged != target) {
                    this.convertTab(dragged, target);
                    dtde.dropComplete(true);
                } else {
                    dtde.dropComplete(false);
                }
                pane.setVisible(false);
            });
        }

        private void convertTab(int dragged, int target) {
            if (target < 0 || dragged == target) {
                return;
            }
            final Component component = getComponentAt(dragged);
            final Component tab = getTabComponentAt(dragged);
            final String title = getTitleAt(dragged);
            final Icon icon = getIconAt(dragged);
            final String tip = getToolTipTextAt(dragged);
            final boolean isEnabled = isEnabledAt(dragged);
            int targetIndex = dragged > target ? target : target - 1;
            remove(dragged);
            insertTab(title, icon, component, tip, targetIndex);
            setEnabledAt(targetIndex, isEnabled);
            if (isEnabled) {
                setSelectedIndex(targetIndex);
            }
            setTabComponentAt(targetIndex, tab);
            sortTabObjects();
        }

        private void autoScrollTest(Point glassPoint) {
            Rectangle bounds = getTabAreaBounds();
            int scrollerSize = 20;
            int buttonSize = 30;
            if (SortableTabbedPane.isTopBottomTabPlacement(getTabPlacement())) {
                backwardScroller.setBounds(bounds.x, bounds.y, scrollerSize, bounds.height);
                forwardScroller.setBounds(bounds.x + bounds.width - scrollerSize - buttonSize, bounds.y,
                        scrollerSize + buttonSize, bounds.height);
            } else {
                backwardScroller.setBounds(bounds.x, bounds.y, bounds.width, scrollerSize);
                forwardScroller.setBounds(bounds.x, bounds.y + bounds.height - scrollerSize - buttonSize,
                        bounds.width, scrollerSize + buttonSize);
            }
            backwardScroller = SwingUtilities.convertRectangle(getParent(),
                    backwardScroller, glassPane);
            forwardScroller = SwingUtilities.convertRectangle(getParent(),
                    forwardScroller, glassPane);
            if (backwardScroller.contains(glassPoint)) {
                clickArrowButton(SCROLL_TABS_BACKWARD_ACTION);
            } else if (forwardScroller.contains(glassPoint)) {
                clickArrowButton(SCROLL_TABS_FORWARD_ACTION);
            }
        }

        private Rectangle getTabAreaBounds() {
            Rectangle bounds = getBounds();

            Component selectedComponent = getSelectedComponent();
            Function<Component, Rectangle> getBounds = Component::getBounds;
            Supplier<Rectangle> supplier = Rectangle::new;
            Rectangle selectedComponentBounds = Optional.ofNullable(selectedComponent)
                    .map(getBounds)
                    .orElseGet(supplier);

            int placement = getTabPlacement();
            if (SortableTabbedPane.isTopBottomTabPlacement(placement)) {
                bounds.height = bounds.height - selectedComponentBounds.height;
                if (placement == SwingConstants.BOTTOM) {
                    bounds.y += selectedComponentBounds.y + selectedComponentBounds.height;
                }
            } else {
                bounds.width = bounds.width - selectedComponentBounds.width;
                if (placement == SwingConstants.RIGHT) {
                    bounds.x += selectedComponentBounds.x + selectedComponentBounds.width;
                }
            }

            bounds.grow(2, 2);
            return bounds;
        }

        private void clickArrowButton(String actionKey) {
            JButton forwardButton = null;
            JButton backwardButton = null;
            for (Component c : getComponents()) {
                if (c instanceof JButton) {
                    if (Objects.isNull(forwardButton)) {
                        forwardButton = (JButton) c;
                    } else if (Objects.isNull(backwardButton)) {
                        backwardButton = (JButton) c;
                    }
                }
            }
            JButton b = SCROLL_TABS_FORWARD_ACTION.equals(actionKey) ? forwardButton : backwardButton;
            Optional.ofNullable(b)
                    .filter(JButton::isEnabled)
                    .ifPresent(JButton::doClick);
        }

        private void initTargetLine(int next) {
            boolean isSideNeighbor = next < 0 || dragTabIndex == next || next - dragTabIndex == 1;
            if (isSideNeighbor) {
                glassPane.setTargetRect(0, 0, 0, 0);
                return;
            }
            int max = Math.max(0, next - 1);
            Rectangle boundsAt = getBoundsAt(max);
            Optional.ofNullable(boundsAt).ifPresent(boundsRect -> {
                final Rectangle converted = SwingUtilities.convertRectangle(SortableTabbedPane.this,
                        boundsRect, glassPane);
                int min = Math.min(next, 1);
                int lineSize = 3;
                if (SortableTabbedPane.isTopBottomTabPlacement(getTabPlacement())) {
                    glassPane.setTargetRect(converted.x + converted.width * min - lineSize / 2,
                            converted.y, lineSize, converted.height);
                } else {
                    glassPane.setTargetRect(converted.x, converted.y + converted.height * min - lineSize / 2,
                            converted.width, lineSize);
                }
            });
        }

    }

    private static class TabbedGlassPane extends JComponent {

        @Getter
        public final SortableTabbedPane tabbedPane;
        private final Rectangle line = new Rectangle();
        private final Point location = new Point();
        @Getter @Setter
        private transient BufferedImage draggingGhost;

        TabbedGlassPane(SortableTabbedPane pane) {
            this.tabbedPane = pane;
        }

        void setTargetRect(int x, int y, int width, int height) {
            line.setBounds(x, y, width, height);
        }

        public void setPoint(Point point) {
            this.location.setLocation(point);
        }

        @Override
        public boolean isOpaque() {
            return false;
        }

        @Override
        public void setVisible(boolean aFlag) {
            super.setVisible(aFlag);
            if (!aFlag) {
                this.setTargetRect(0, 0, 0, 0);
                this.setDraggingGhost(null);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            if (draggingGhost != null) {
                double x = location.getX() - draggingGhost.getWidth(this) / 2.0d;
                double y = location.getY() - draggingGhost.getHeight(this) / 2.0d;
                g2.drawImage(draggingGhost, (int) x, (int) y, this);
            }
            g2.setPaint(Color.BLACK);
            g2.fill(line);
            g2.dispose();
        }

    }

    private static class TabDragGestureListener implements DragGestureListener {
        private final DragSourceListener handler = new TabDragSourceListener();

        @Override public void dragGestureRecognized(DragGestureEvent dge) {
            Optional.ofNullable(dge.getComponent())
                    .filter(SortableTabbedPane.class::isInstance)
                    .map(SortableTabbedPane.class::cast)
                    .filter(tabbedPane -> tabbedPane.getTabCount() > 1)
                    .ifPresent(pane -> startDrag(dge, pane));
        }

        private void startDrag(DragGestureEvent e, SortableTabbedPane tabbedPane) {
            Point tabPoint = e.getDragOrigin();
            int indexAtLocation = tabbedPane.indexAtLocation(tabPoint.x, tabPoint.y);
            int selectedIndex = tabbedPane.getSelectedIndex();
            boolean isTabRunsRotated = !(tabbedPane.getUI() instanceof MetalTabbedPaneUI)
                    && tabbedPane.getTabLayoutPolicy() == JTabbedPane.WRAP_TAB_LAYOUT
                    && indexAtLocation != selectedIndex;
            tabbedPane.dragTabIndex = isTabRunsRotated ? selectedIndex : indexAtLocation;
            if (tabbedPane.dragTabIndex >= 0 && tabbedPane.isEnabledAt(tabbedPane.dragTabIndex)) {
                tabbedPane.initGlassPane(tabPoint);
                try {
                    e.startDrag(DragSource.DefaultMoveDrop, new TabTransferable(tabbedPane), handler);
                } catch (InvalidDnDOperationException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }

        @SuppressWarnings("InnerClassTooDeeplyNested")
        private static class TabTransferable implements Transferable {
            private static final String NAME = "SortableTab";
            private final Component tabbedPane;

            TabTransferable(Component pane) {
                this.tabbedPane = pane;
            }

            @Override public Object getTransferData(DataFlavor flavor) {
                return tabbedPane;
            }

            @Override public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[] {new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME)};
            }

            @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
                return NAME.equals(flavor.getHumanPresentableName());
            }
        }

        @SuppressWarnings({"PackageVisibleInnerClass", "InnerClassTooDeeplyNested"})
        static class TabDragSourceListener extends DragSourceAdapter {

            @Override public void dragEnter(DragSourceDragEvent dsde) {
                DragSourceContext dragSourceContext = dsde.getDragSourceContext();
                dragSourceContext.setCursor(DragSource.DefaultMoveDrop);
            }

            @Override public void dragExit(DragSourceEvent dse) {
                DragSourceContext dragSourceContext = dse.getDragSourceContext();
                dragSourceContext.setCursor(DragSource.DefaultMoveNoDrop);
            }

            @Override public void dragDropEnd(DragSourceDropEvent dsde) {
                DragSourceContext dragSourceContext = dsde.getDragSourceContext();
                Component component = dragSourceContext.getComponent();
                if (component instanceof JComponent) {
                    JRootPane rp = ((JComponent) component).getRootPane();
                    Optional.ofNullable(rp.getGlassPane()).ifPresent(gp -> gp.setVisible(false));
                }
            }

        }

    }

}
