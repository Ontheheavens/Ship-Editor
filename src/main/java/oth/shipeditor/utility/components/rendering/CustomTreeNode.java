package oth.shipeditor.utility.components.rendering;

import lombok.Getter;
import lombok.Setter;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Ontheheavens
 * @since 30.09.2023
 */
@Getter @Setter
public class CustomTreeNode extends DefaultMutableTreeNode {

    private String firstLineTip;

    private String secondLineTip;

    private String thirdLineTip;

    @SuppressWarnings("ParameterHidesMemberVariable")
    public CustomTreeNode(Object userObject) {
        super(userObject);
    };

}
