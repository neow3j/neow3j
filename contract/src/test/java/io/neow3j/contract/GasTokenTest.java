package io.neow3j.contract;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import io.neow3j.protocol.Neow3j;
import org.junit.jupiter.api.Test;

public class GasTokenTest {

    // The tests don't need an actual connection to a node.
    private final Neow3j neow = Neow3j.build(null);
    private final static String GASTOKEN_SCRIPTHASH = "d2a4cff31913016155e38e474a2c06d08be276cf";

    @Test
    public void getName() {
        assertThat(new GasToken(neow).getName(), is("GasToken"));
    }

    @Test
    public void getSymbol() {
        assertThat(new GasToken(neow).getSymbol(), is("GAS"));
    }

    @Test
    public void getDecimals() {
        assertThat(new GasToken(neow).getDecimals(), is(8));
    }

    @Test
    public void scriptHash() {
        assertThat(new GasToken(neow).getScriptHash().toString(), is(GASTOKEN_SCRIPTHASH));
    }

}
