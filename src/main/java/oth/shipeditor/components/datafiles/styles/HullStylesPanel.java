package oth.shipeditor.components.datafiles.styles;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullStylesLoaded;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.representation.HullStyle;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.objects.Pair;

import javax.swing.*;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 23.07.2023
 */
public class HullStylesPanel extends AbstractStylesPanel {

    @Override
    protected JPanel createTopPanel() {
        Action buttonAction = FileLoading.loadDataAsync(FileLoading.getLoadHullStyles());
        Pair<JPanel, JButton> singleButtonPanel = ComponentUtilities.createLoaderButtonPanel("Hullstyle data:", buttonAction);
        JButton button = singleButtonPanel.getSecond();
        button.setText("Reload hullstyle data");
        return singleButtonPanel.getFirst();
    }

    @Override
    protected void initListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof HullStylesLoaded checked) {
                populatePanel(checked.hullStyles());
            }
        });
    }

    private void populatePanel(Map<String, HullStyle> hullStyles) {
        JPanel scrollerContent = getScrollerContent();
        scrollerContent.removeAll();
        for (HullStyle style : hullStyles.values()) {
            scrollerContent.add(this.createStylePanel(style));
        }
    }

    @Override
    protected  JPanel createStyleTitlePanel(Object style) {
        if (style instanceof HullStyle checked) {
            String hullStyleID = checked.getHullStyleID();
            Path filePath = checked.getFilePath();
            Path containingPackage = checked.getContainingPackage();
            return ComponentUtilities.createFileTitlePanel(filePath, containingPackage, hullStyleID);
        } else {
            throw new IllegalArgumentException(ILLEGAL_STYLE_ARGUMENT);
        }

    }

    @Override
    protected JPanel createStyleContentPanel(Object style) {
        if (style instanceof HullStyle checked) {
            JPanel contentContainer = new JPanel();
            contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.PAGE_AXIS));

            JLabel ringLabel = new JLabel("Shield ring color:");
            JPanel ringColorPanel = ComponentUtilities.createColorPropertyPanel(ringLabel, checked.getShieldRingColor());
            contentContainer.add(ringColorPanel);

            JLabel innerLabel = new JLabel("Shield inner color:");
            JPanel innerColorPanel = ComponentUtilities.createColorPropertyPanel(innerLabel, checked.getShieldInnerColor());
            contentContainer.add(innerColorPanel);

            return contentContainer;
        } else {
            throw new IllegalArgumentException(ILLEGAL_STYLE_ARGUMENT);
        }
    }

}
