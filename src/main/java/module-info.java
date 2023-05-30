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

    exports oth.shipeditor.representation;
    exports oth.shipeditor.representation.data;
    exports oth.shipeditor.parsing;
    exports oth.shipeditor.components;
    exports oth.shipeditor.components.painters;
    exports oth.shipeditor.components.entities;

    opens oth.shipeditor.representation.data;
    opens oth.shipeditor.parsing;
    opens oth.shipeditor.components;
    exports oth.shipeditor.components.control;
    opens oth.shipeditor.components.control;
}