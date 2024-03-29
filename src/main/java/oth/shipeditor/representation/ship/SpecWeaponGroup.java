package oth.shipeditor.representation.ship;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.utility.text.StringConstants;

import java.util.Map;

/**
 * @author Ontheheavens
 * @since 05.08.2023
 */
@Getter @Setter
public class SpecWeaponGroup {

    @JsonProperty("autofire")
    private boolean autofire;

    @JsonProperty("mode")
    private String mode;

    @JsonProperty(StringConstants.WEAPONS)
    private Map<String, String> weapons;

}
