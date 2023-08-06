package oth.shipeditor.representation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import oth.shipeditor.utility.text.StringConstants;

import java.util.Map;

/**
 * @author Ontheheavens
 * @since 05.08.2023
 */
@Getter
public class WeaponGroup {

    @JsonProperty("autofire")
    private boolean autofire;

    @JsonProperty("mode")
    private String mode;

    @JsonProperty(StringConstants.WEAPONS)
    private Map<String, String> weapons;

}
