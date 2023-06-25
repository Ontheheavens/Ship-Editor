package oth.shipeditor.components.datafiles;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullFolderWalked;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.representation.Hull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 25.06.2023
 */
@Log4j2
class HullsTree extends JPanel {

    private final DefaultMutableTreeNode hullsRoot;

    private DefaultMutableTreeNode cachedSelectForMenu;

    @Getter
    private final JTree hullsTree;

    private final GameDataPanel parent;

    HullsTree(GameDataPanel gameDataPanel) {
        this.parent = gameDataPanel;
        hullsRoot = new DefaultMutableTreeNode("Hull files");
        hullsTree = new JTree(hullsRoot);
        JScrollPane scrollContainer = new JScrollPane(hullsTree);
        this.setLayout(new BorderLayout());
        this.add(scrollContainer);
        this.initBusListening();
        this.initComponentListeners();
    }

    private void initBusListening() {
        EventBus.subscribe(event -> {
            if (event instanceof HullFolderWalked checked) {
                List<Map<String, String>> tableData = checked.csvData();
                if (tableData == null) return;
                DefaultMutableTreeNode node = loadHullList(tableData, checked.hullFiles(), checked.folder());
                hullsTree.expandPath(new TreePath(new DefaultMutableTreeNode[]{hullsRoot, node}));
                hullsTree.repaint();
            }
        });
    }

    private void initComponentListeners() {
        hullsTree.addMouseListener(new ContextMenuListener());
        hullsTree.addTreeSelectionListener(e -> {
            TreePath selectedNode = e.getNewLeadSelectionPath();
            if (selectedNode == null) return;
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedNode.getLastPathComponent();
            if (node.getUserObject() instanceof ShipCSVEntry checked) {
                parent.updateEntryPanel(checked);
            }
        });
    }

    private DefaultMutableTreeNode loadHullList(Iterable<Map<String, String>> tableData,
                                                Iterable<Hull> hullFiles, Path packagePath) {
        DefaultMutableTreeNode packageRoot = new DefaultMutableTreeNode(packagePath.getFileName().toString());
        for (Map<String, String> row : tableData) {
            Hull matching = null;
            for (Hull shipFile : hullFiles) {
                String hullId = shipFile.getHullId();
                if (hullId.equals(row.get("id"))) {
                    matching = shipFile;
                }
            }
            if (matching != null) {
                MutableTreeNode shipNode = new DefaultMutableTreeNode(new ShipCSVEntry(row, matching, packagePath));
                packageRoot.add(shipNode);
            }
        }
        hullsRoot.add(packageRoot);
        return packageRoot;
    }

    private class LoadLayerFromTree extends AbstractAction {
        @Override
        public boolean isEnabled() {
            return super.isEnabled() && cachedSelectForMenu.getUserObject() instanceof ShipCSVEntry;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            DefaultMutableTreeNode packageNode = (DefaultMutableTreeNode) cachedSelectForMenu.getParent();
            String packageName = (String) packageNode.getUserObject();
            if (cachedSelectForMenu.getUserObject() instanceof ShipCSVEntry checked) {
                Map<String, String> data = checked.getRowData();
                String hullID = data.get("id");
                // TODO: implement layer loading.
                log.info(packageName);
                log.info(hullID);
            }
        }

    }

    private class ContextMenuListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if(e.getButton() == MouseEvent.BUTTON3){
                TreePath pathForLocation = hullsTree.getPathForLocation(e.getPoint().x, e.getPoint().y);
                if(pathForLocation != null){
                    cachedSelectForMenu = (DefaultMutableTreeNode) pathForLocation.getLastPathComponent();
                    JPopupMenu contextMenu = createContextMenu();
                    if (cachedSelectForMenu.getUserObject() instanceof ShipCSVEntry) {
                        hullsTree.setSelectionPath(pathForLocation);
                        contextMenu.show(hullsTree, e.getPoint().x, e.getPoint().y);
                    }
                } else{
                    cachedSelectForMenu = null;
                }
            }
            super.mousePressed(e);
        }

        private JPopupMenu createContextMenu() {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem loadAsLayer = new JMenuItem("Load as layer");
            loadAsLayer.addActionListener(new LoadLayerFromTree());
            menu.add(loadAsLayer);
            return menu;
        }

    }

}
