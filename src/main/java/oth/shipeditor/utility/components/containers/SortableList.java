package oth.shipeditor.utility.components.containers;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.utility.Errors;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * @author Terai Atsuhiro, refactored by Ontheheavens
 * @since 25.07.2023
 */
@SuppressWarnings("NoopMethodInAbstractClass")
public abstract class SortableList<E> extends JList<E> implements DragGestureListener, DragSourceListener {

    private static final Color EVEN_BGC = new Color(0xF0_F0_F0);
    private final Rectangle targetLine = new Rectangle();
    private int draggedIndex = -1;
    private int targetIndex = -1;

    @Getter @Setter
    private boolean dragEnabled;

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    protected SortableList(ListModel<E> dataModel) {
        super(dataModel);
        int actionCopyOrMove = DnDConstants.ACTION_COPY_OR_MOVE;
        new DropTarget(this, actionCopyOrMove, new ItemDropTargetListener(), true);
        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this, actionCopyOrMove, this);
    }

    protected abstract void sortListModel();

    protected abstract Transferable createTransferableFromEntry(E entry);

    protected abstract boolean isSupported(Transferable transferable);

    @SuppressWarnings({"ParameterHidesMemberVariable", "BooleanMethodNameMustStartWithQuestion"})
    protected boolean confirmDrop(int targetIndex, E entry) {
        DefaultListModel<E> model = (DefaultListModel<E>) getModel();
        model.add(targetIndex, entry);
        setSelectedIndex(targetIndex);
        return true;
    }

    protected void actOnSelectedEntry(Consumer<E> action) {
        int index = this.getSelectedIndex();
        if (index != -1) {
            ListModel<E> listModel = this.getModel();
            E feature = listModel.getElementAt(index);
            action.accept(feature);
        }
    }

    @Override
    public void updateUI() {
        setCellRenderer(null);
        super.updateUI();
        ListCellRenderer<? super E> renderer = getCellRenderer();
        setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            Component c = renderer.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            if (isSelected) {
                c.setForeground(list.getSelectionForeground());
                c.setBackground(list.getSelectionBackground());
            }
            else {
                c.setForeground(list.getForeground());
                c.setBackground(index % 2 == 0 ? EVEN_BGC : list.getBackground());
            }
            return c;
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (targetIndex >= 0) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(Color.GRAY);
            g2.fill(targetLine);
            g2.dispose();
        }
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        if (!dragEnabled) return;
        boolean moreThanOne = getSelectedIndices().length > 1;
        draggedIndex = locationToIndex(dge.getDragOrigin());
        if (moreThanOne || draggedIndex < 0) {
            return;
        }
        try {
            ListModel<E> listModel = this.getModel();
            E toDrag = listModel.getElementAt(draggedIndex);
            Transferable transferable = createTransferableFromEntry(toDrag);
            dge.startDrag(DragSource.DefaultMoveDrop, transferable, this);
        } catch (InvalidDnDOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void dragEnter(DragSourceDragEvent dsde) {
        DragSourceContext dragSourceContext = dsde.getDragSourceContext();
        dragSourceContext.setCursor(DragSource.DefaultMoveDrop);
    }

    @Override
    public void dragExit(DragSourceEvent dse) {
        DragSourceContext dragSourceContext = dse.getDragSourceContext();
        dragSourceContext.setCursor(DragSource.DefaultMoveNoDrop);
    }

    @Override
    public void dragOver(DragSourceDragEvent dsde) {
    }

    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {
    }

    @Override
    public void dropActionChanged(DragSourceDragEvent dsde) {
    }

    private final class ItemDropTargetListener extends DropTargetAdapter {

        @Override
        public void dragExit(DropTargetEvent dte) {
            targetIndex = -1;
            repaint();
        }

        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
            if (isDragAcceptable(dtde)) {
                dtde.acceptDrag(dtde.getDropAction());
            }
            else {
                dtde.rejectDrag();
            }
        }

        @Override
        public void dragOver(DropTargetDragEvent dtde) {
            if (isDragAcceptable(dtde)) {
                dtde.acceptDrag(dtde.getDropAction());
            }
            else {
                dtde.rejectDrag();
                return;
            }
            initTargetLine(dtde.getLocation());
            repaint();
        }

        private void initTargetLine(Point p) {
            Rectangle rect = getCellBounds(0, 0);
            int lineHeight = 2;
            if (rect == null) {
                targetIndex = 0;
                targetLine.setSize(SortableList.this.getWidth(), lineHeight);
                targetLine.setLocation(0, 0);
                return;
            }
            int cellHeight = rect.height;
            ListModel<E> model = getModel();
            int modelSize = model.getSize();
            targetIndex = -1;
            targetLine.setSize(rect.width, lineHeight);
            for (int i = 0; i < modelSize; i++) {
                rect.setLocation(0, cellHeight * i - cellHeight / 2);
                if (rect.contains(p)) {
                    targetIndex = i;
                    targetLine.setLocation(0, i * cellHeight);
                    break;
                }
            }
            if (targetIndex < 0) {
                targetIndex = modelSize;
                targetLine.setLocation(0, targetIndex * cellHeight - lineHeight);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public void drop(DropTargetDropEvent dtde) {
            DefaultListModel<E> model = (DefaultListModel<E>) getModel();
            if (isDropAcceptable(dtde) && targetIndex >= 0) {
                Transferable transferable = dtde.getTransferable();
                DataFlavor[] dataFlavors = transferable.getTransferDataFlavors();

                String hashCodeContainer = dataFlavors[1].getHumanPresentableName();
                int hash = SortableList.this.hashCode();
                String hashCodeText = String.valueOf(hash);
                boolean fromOutside = !hashCodeContainer.equals(hashCodeText);

                if (fromOutside || draggedIndex == -1 || model.isEmpty()) {
                    E entry;
                    try {
                        entry = (E) transferable.getTransferData(dataFlavors[0]);
                    } catch (UnsupportedFlavorException | IOException e) {
                        Errors.printToStream(e);
                        dtde.dropComplete(false);
                        targetIndex = -1;
                        return;
                    }
                    boolean confirmed = confirmDrop(targetIndex, entry);
                    if (!confirmed) {
                        dtde.dropComplete(false);
                        targetIndex = -1;
                        return;
                    };
                } else {
                    E draggedElement = model.get(draggedIndex);
                    if (targetIndex == draggedIndex) {
                        setSelectedIndex(targetIndex);
                    }
                    else if (targetIndex < draggedIndex) {
                        model.remove(draggedIndex);
                        model.add(targetIndex, draggedElement);
                        setSelectedIndex(targetIndex);
                    }
                    else {
                        model.add(targetIndex, draggedElement);
                        model.remove(draggedIndex);
                        setSelectedIndex(targetIndex - 1);
                    }
                }
                sortListModel();
                dtde.dropComplete(true);
            }
            else {
                dtde.dropComplete(false);
            }
            dtde.dropComplete(false);
            targetIndex = -1;
            draggedIndex = -1;
            repaint();
        }

        private boolean isDragAcceptable(DropTargetDragEvent e) {
            return isSupported(e.getTransferable());
        }

        private boolean isDropAcceptable(DropTargetDropEvent e) {
            return isSupported(e.getTransferable());
        }

    }

}
