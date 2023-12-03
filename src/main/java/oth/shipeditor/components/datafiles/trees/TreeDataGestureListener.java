package oth.shipeditor.components.datafiles.trees;

import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;
import oth.shipeditor.components.datafiles.entities.WingCSVEntry;
import oth.shipeditor.components.datafiles.entities.transferable.*;
import oth.shipeditor.components.viewer.ViewerDragListener;
import oth.shipeditor.components.viewer.ViewerDropReceiver;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;

/**
 * @author Ontheheavens
 * @since 05.11.2023
 */
public class TreeDataGestureListener implements DragGestureListener {

    private final JTree tree;

    TreeDataGestureListener(JTree inputTree) {
        this.tree = inputTree;
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        TreePath path = tree.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            Object userObject = node.getUserObject();

            Transferable transferable;
            switch (userObject) {
                case ShipCSVEntry shipEntry -> {
                    transferable = new TransferableShip(shipEntry, tree);
                    ViewerDropReceiver.commenceDragToViewer(shipEntry, TransferableEntry.TRANSFERABLE_SHIP);
                }
                case WeaponCSVEntry weaponEntry -> {
                    transferable = new TransferableWeapon(weaponEntry, tree);
                    ViewerDropReceiver.commenceDragToViewer(weaponEntry, TransferableEntry.TRANSFERABLE_WEAPON);
                }
                case HullmodCSVEntry hullmodEntry -> transferable = new TransferableHullmod(hullmodEntry, tree);
                case WingCSVEntry wingEntry -> transferable = new TransferableWing(wingEntry, tree);
                default -> {
                    ViewerDropReceiver.finishDragToViewer();
                    return;
                }
            }
            dge.startDrag(DragSource.DefaultMoveDrop, transferable, new ViewerDragListener());
        }
    }

}
