package oth.shipeditor.parsing.loading;

/**
 * @author Ontheheavens
 * @since 28.10.2023
 */
public abstract class DataLoadingAction {

    /**
     * @return code that is expected to publish the results of loading process.
     */
    public abstract Runnable perform();

}
