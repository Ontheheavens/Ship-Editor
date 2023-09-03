package oth.shipeditor.components.viewer.layers;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Ontheheavens
 * @since 28.07.2023
 */
@SuppressWarnings("AbstractClassWithoutAbstractMethods")
@Getter @Setter
public abstract class ViewerLayer {

    private String spriteFileName = "";

    private LayerPainter painter;

    @Override
    public String toString() {
        if (!spriteFileName.isEmpty()) {
            return spriteFileName;
        }
        return super.toString();
    }

}
