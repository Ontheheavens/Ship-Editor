package oth.shipeditor.utility;

import lombok.Getter;

/**
 * @author Ontheheavens
 * @since 15.07.2023
 */
@Getter
public class Pair<T, U> {
    private final T first;
    private final U second;

    public Pair(T firstInput, U secondInput) {
        this.first = firstInput;
        this.second = secondInput;
    }

}
