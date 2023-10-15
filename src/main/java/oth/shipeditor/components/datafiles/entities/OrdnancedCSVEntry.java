package oth.shipeditor.components.datafiles.entities;

import oth.shipeditor.representation.HullSize;

import javax.swing.*;

/**
 * @author Ontheheavens
 * @since 12.10.2023
 */
public interface OrdnancedCSVEntry extends CSVEntry {

    JLabel getIconLabel();

    JLabel getIconLabel(int maxSize);

    int getOrdnanceCost(HullSize size);

}
