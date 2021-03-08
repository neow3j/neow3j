package io.neow3j.contract;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.protocol.Neow3j;
import org.junit.Test;

public class GasTokenTest {

    // The tests don't need an actual connection to a node.
    private final Neow3j neow = Neow3j.build(null);
    private final static String GASTOKEN_SCRIPTHASH = "70e2301955bf1e74cbb31d18c2f96972abadb328";

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
