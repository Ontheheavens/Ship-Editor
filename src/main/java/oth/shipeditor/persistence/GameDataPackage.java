package oth.shipeditor.persistence;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Ontheheavens
 * @since 03.11.2023
 */
@SuppressWarnings({"ParameterHidesMemberVariable", "NegativelyNamedBooleanVariable", "BooleanParameter"})
@Getter @Setter
public class GameDataPackage {

    @JsonProperty("folderName")
    private String folderName;

    @JsonProperty("pinned")
    private boolean pinned;

    @SuppressWarnings("NegativelyNamedBooleanVariable")
    @JsonProperty("disabled")
    private boolean disabled;

    public GameDataPackage() {}

    public GameDataPackage(String folderName, boolean pinned, boolean disabled) {
        this.folderName = folderName;
        this.pinned = pinned;
        this.disabled = disabled;
    }

}
