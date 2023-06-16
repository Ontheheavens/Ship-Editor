package oth.shipeditor.undo;

/**
 * Entity interface for the Undo/Redo functionality; this is essentially Command Pattern.
 * @author Ontheheavens
 * @since 16.06.2023
 */
public interface Edit {

    public void add(Edit edit);

    public void undo();

    public void redo();

    public String getName();

}
