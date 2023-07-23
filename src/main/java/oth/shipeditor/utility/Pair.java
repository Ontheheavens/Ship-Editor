package oth.shipeditor.utility;

import lombok.Getter;

/**
 * @author Ontheheavens
 * @since 15.07.2023
 */
@Getter
public class Pair<A, B> {
    private final A first;
    private final B second;

    @SuppressWarnings("WeakerAccess")
    public Pair(A firstInput, B secondInput) {
        this.first = firstInput;
        this.second = secondInput;
    }

}
