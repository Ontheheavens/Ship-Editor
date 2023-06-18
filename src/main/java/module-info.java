/**
* @author Ontheheavens
* @since 08.05.2023
*/module oth.shipeditor {

    requires viewer.core;
    requires java.desktop;
    requires lombok;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.swing;
    requires org.kordamp.ikonli.fluentui;
    requires org.apache.logging.log4j;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.formdev.flatlaf;
    requires geom;

    exports oth.shipeditor.representation;
    exports oth.shipeditor.parsing;
    exports oth.shipeditor.components;
    exports oth.shipeditor.components.viewer.painters;
    exports oth.shipeditor.components.viewer.entities;

    opens oth.shipeditor.parsing;
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

}