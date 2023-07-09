package oth.shipeditor.components.datafiles;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.representation.Skin;
import oth.shipeditor.utility.StringConstants;
import oth.shipeditor.utility.Utility;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author Ontheheavens
 * @since 18.06.2023
 */
@Log4j2
public class GameDataPanel extends JPanel {

    public GameDataPanel() {
        JTabbedPane container = new JTabbedPane(SwingConstants.BOTTOM);
        container.addTab("Hulls", new HullsTreePanel());
        container.addTab("Hullmods", new HullmodsTreePanel());
        this.setLayout(new BorderLayout());
        this.add(container, BorderLayout.CENTER);
    }

}
