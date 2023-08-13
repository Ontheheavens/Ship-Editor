/**
* @author Ontheheavens
* @since 08.05.2023
*/module oth.shipeditor {

    // These packages are integral for the editor functionality.
    requires java.desktop;
    requires geom;
    requires viewer.core;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.csv;

    // These packages are tightly intertwined with the app and are important, but can be removed with some work.
    requires lombok;
    requires com.formdev.flatlaf;
    requires org.apache.logging.log4j;

    // These packages are mostly cosmetic.
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.swing;
    requires org.kordamp.ikonli.fluentui;
    requires org.kordamp.ikonli.boxicons;
    requires filters;

    exports oth.shipeditor;
    exports oth.shipeditor.representation;
    exports oth.shipeditor.components;
    exports oth.shipeditor.components.viewer.painters;
    exports oth.shipeditor.components.viewer.entities;
    exports oth.shipeditor.components.viewer.entities.bays;
    exports oth.shipeditor.communication;
    exports oth.shipeditor.communication.events;

    opens oth.shipeditor.components;
    exports oth.shipeditor.components.viewer.control;
    opens oth.shipeditor.components.viewer.control;
    exports oth.shipeditor.components.viewer.layers;
    opens oth.shipeditor.representation;
    exports oth.shipeditor.components.viewer;
    opens oth.shipeditor.components.viewer;
    exports oth.shipeditor.components.layering;
    opens oth.shipeditor.components.layering;
    exports oth.shipeditor.components.instrument;
    opens oth.shipeditor.components.instrument;
    exports oth.shipeditor.persistence to com.fasterxml.jackson.databind;
    opens oth.shipeditor.persistence to com.fasterxml.jackson.databind;
    exports oth.shipeditor.parsing.deserialize;
    opens oth.shipeditor.parsing.deserialize;
    exports oth.shipeditor.parsing.serialize;
    opens oth.shipeditor.parsing.serialize;
    exports oth.shipeditor.components.datafiles.entities;
    exports oth.shipeditor.utility;
    exports oth.shipeditor.utility.graphics;
    exports oth.shipeditor.components.viewer.painters.points;
    exports oth.shipeditor.representation.weapon;
    opens oth.shipeditor.representation.weapon;
    exports oth.shipeditor.utility.text;
    exports oth.shipeditor.components.viewer.layers.ship;
    exports oth.shipeditor.components.instrument.ship;
    opens oth.shipeditor.components.instrument.ship;
    exports oth.shipeditor.components.instrument.ship.weaponslots;
    opens oth.shipeditor.components.instrument.ship.weaponslots;
    exports oth.shipeditor.components.instrument.ship.skins;
    opens oth.shipeditor.components.instrument.ship.skins;
    exports oth.shipeditor.components.viewer.entities.weapon;
    exports oth.shipeditor.components.viewer.layers.ship.data;
    exports oth.shipeditor.representation.weapon.animation;
    opens oth.shipeditor.representation.weapon.animation;

}