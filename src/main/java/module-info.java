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
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;

    exports oth.shipeditor.data;
    exports oth.shipeditor.parsing;
    exports oth.shipeditor.components;

    opens oth.shipeditor.data;
    opens oth.shipeditor.parsing;
    opens oth.shipeditor.components;
}