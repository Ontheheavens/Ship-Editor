package oth.shipeditor.parsing.loading;

import lombok.extern.log4j.Log4j2;

/**
 * @author Ontheheavens
 * @since 28.10.2023
 */
@Log4j2
public abstract class DataLoadingAction {

    /**
     * @return code that is expected to publish the results of loading process.
     */
    public abstract Runnable perform();

}
