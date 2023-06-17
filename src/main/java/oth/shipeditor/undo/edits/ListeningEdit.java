package oth.shipeditor.undo.edits;

/**
 * @author Ontheheavens
 * @since 17.06.2023
 */
public interface ListeningEdit {

    public void registerListeners();

    public void unregisterListeners();

}
