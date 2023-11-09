package oth.shipeditor.utility.themes;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatVuesionIJTheme;
import lombok.Getter;

/**
 * @author Ontheheavens
 * @since 05.11.2023
 */
@Getter
public enum Theme {

    FLAT_INTELLIJ("Flat IntelliJ", FlatIntelliJLaf::setup),

    FLAT_DARK("Flat Dark", FlatDarkLaf::setup),

    ONE_DARK("One Dark", FlatOneDarkIJTheme::setup),

    ARC_DARK("Arc Dark", FlatArcDarkIJTheme::setup),

    VUESION("Vuesion", FlatVuesionIJTheme::setup);

    private final String displayedName;

    private final Runnable setterMethod;

    Theme(String name, Runnable setter) {
        this.displayedName = name;
        this.setterMethod = setter;
    }

}
