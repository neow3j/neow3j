package io.neow3j.compiler.contracts;

import io.neow3j.devpack.framework.annotations.EntryPoint;

public class RelationalOperators {

    @EntryPoint
    public static boolean[] integerRelationalOperators(int i, int j) {
        boolean[] b = new boolean[6];
        b[0] = i == j;
        b[1] = i != j;
        b[2] = i < j;
        b[3] = i <= j;
        b[4] = i > j;
        b[5] = i >= j;
        return b;
    }

}
