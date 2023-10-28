package oth.shipeditor.components.datafiles.styles;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.EngineStylesLoaded;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.representation.EngineStyle;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.objects.Pair;

import javax.swing.*;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 19.08.2023
 */
public class EngineStylesPanel extends AbstractStylesPanel {

    @Override
    protected JPanel createTopPanel() {
        Action buttonAction = FileLoading.loadDataAsync(FileLoading.getLoadEngineStyles());
        Pair<JPanel, JButton> singleButtonPanel = ComponentUtilities.createLoaderButtonPanel("Engine styles data:", buttonAction);
        JButton button = singleButtonPanel.getSecond();
        button.setText("Reload engine styles data");
        return singleButtonPanel.getFirst();
    }

    @Override
    protected void initListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof EngineStylesLoaded checked) {
                populatePanel(checked.engineStyles());
            }
        });
    }

    private void populatePanel(Map<String, EngineStyle> engineStyles) {
        JPanel scrollerContent = getScrollerContent();
        scrollerContent.removeAll();
        for (EngineStyle style : engineStyles.values()) {
            scrollerContent.add(createStylePanel(style));
        }
    }

    protected JPanel createStyleTitlePanel(Object style) {
        if (style instanceof EngineStyle checked) {
            String styleID = checked.getEngineStyleID();
            Path filePath = checked.getFilePath();
            Path containingPackage = checked.getContainingPackage();
            return ComponentUtilities.createFileTitlePanel(filePath, containingPackage, styleID);
        } else {
            throw new IllegalArgumentException(ILLEGAL_STYLE_ARGUMENT);
        }
    }

    protected JPanel createStyleContentPanel(Object style) {
        if (style instanceof EngineStyle checked) {
            JPanel contentContainer = new JPanel();
            contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.PAGE_AXIS));

            JLabel engineLabel = new JLabel("Engine color:");
            JPanel engineColorPanel = ComponentUtilities.createColorPropertyPanel(engineLabel, checked.getEngineColor());
            contentContainer.add(engineColorPanel);

            JLabel contrailLabel = new JLabel("Contrail color:");
            JPanel contrailColorPanel = ComponentUtilities.createColorPropertyPanel(contrailLabel, checked.getContrailColor());
            contentContainer.add(contrailColorPanel);

            return contentContainer;
        } else {
            throw new IllegalArgumentException(ILLEGAL_STYLE_ARGUMENT);
        }
    }

}
