package oth.shipeditor.components.logging;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;

/**
 * @author Ontheheavens
 * @since 10.10.2023
 */
@Log4j2
@Plugin(
        name = GUIAppender.DESIGNATION,
        category = Core.CATEGORY_NAME,
        elementType = Appender.ELEMENT_TYPE)
public final class GUIAppender extends AbstractAppender {

    private static final String DEFAULT_PATTERN = "%d{HH:mm:ss.SSS} %-5level %logger{1} - %msg%n";

    static final String DESIGNATION = "GUIAppender";

    private final PatternLayout patternLayout;

    private GUIAppender(String name, Filter filter, Layout<? extends Serializable> layout) {
        super(name, filter, layout, false, null);
        if (layout instanceof PatternLayout checked) {
            this.patternLayout = checked;
        } else {
            this.patternLayout = PatternLayout.newBuilder()
                    .withPattern(DEFAULT_PATTERN)
                    .build();
        }
    }

    @PluginFactory
    public static GUIAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Filter") Filter filter,
            @PluginElement("Layout") Layout<? extends Serializable> layout) {
        return new GUIAppender(name, filter, layout);
    }

    @Override
    public void append(LogEvent event) {
        String formattedMessage = patternLayout.toSerializable(event);
        LogsPanel.append(formattedMessage);
    }
}
