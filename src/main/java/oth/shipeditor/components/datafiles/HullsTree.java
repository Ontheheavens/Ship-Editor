package oth.shipeditor.components.datafiles;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.ShipCSVOpened;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 25.06.2023
 */
class HullsTree extends JPanel {

    private final DefaultMutableTreeNode hullsRoot;
    private final JTree hullsTree;

    HullsTree() {
        hullsRoot = new DefaultMutableTreeNode("Hull files");
        hullsTree = new JTree(hullsRoot);
        JScrollPane scrollContainer = new JScrollPane(hullsTree);
        this.setLayout(new BorderLayout());
        this.add(scrollContainer);
        this.initListening();
    }

    private void initListening() {
        EventBus.subscribe(event -> {
            if (event instanceof ShipCSVOpened checked) {
                String packageName = checked.packageName();
                List<Map<String, String>> tableData = checked.csvData();
                if (tableData == null || packageName.isEmpty()) return;
                DefaultMutableTreeNode node = loadHullList(tableData, packageName);
                hullsTree.expandPath(new TreePath(new DefaultMutableTreeNode[]{hullsRoot, node}));
                hullsTree.repaint();
            }
        });
    }

    private DefaultMutableTreeNode loadHullList(Iterable<? extends Map<String, String>> tableData, String packageName) {
        DefaultMutableTreeNode packageRoot = new DefaultMutableTreeNode(packageName);
        for (Map<String, String> row : tableData) {
            MutableTreeNode shipNode = new DefaultMutableTreeNode(new ShipCSVEntry(row));
            packageRoot.add(shipNode);
        }
        hullsRoot.add(packageRoot);
        return packageRoot;
    }

}
