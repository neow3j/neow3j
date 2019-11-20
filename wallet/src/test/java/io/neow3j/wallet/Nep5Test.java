package io.neow3j.wallet;

import io.neow3j.contract.ScriptHash;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.math.BigInteger;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

public class Nep5Test {

    private Neow3j neow3j;
    private ScriptHash contractScriptHash;
    private Nep5 nep5;

    @Before
    public void setUp() {
        nep5 = Mockito.spy(Nep5.class);
        this.neow3j = Neow3j.build(new HttpService(""));
        this.contractScriptHash = new ScriptHash("0x646171fd57e768f473f516d0c931f8d58359b5ea");
    }

    @Test
    public void totalSupplyTest() throws IOException {
        Mockito.doReturn(new BigInteger("66554545")).when(nep5).totalSupply();
        assertEquals(new BigInteger("66554545"), nep5.totalSupply());
    }

    @Test
    public void nameTest() throws IOException {
        Mockito.doReturn("Nep5 test Name").when(nep5).name();
        assertEquals("Nep5 test Name", nep5.name());
    }

    @Test
    public void symbolTest() throws IOException {
        Mockito.doReturn("Nep5 test for Symbol").when(nep5).symbol();
        assertEquals("Nep5 test for Symbol", nep5.symbol());
    }

    @Test
    public void decimalsTest() throws IOException {
        Mockito.doReturn(new BigInteger("66554545")).when(nep5).decimals();
        assertEquals(new BigInteger("66554545"), nep5.decimals());
    }

    @Test
    public void failWithoutNeow3j() {
        try {
            new Nep5.Builder(null).fromContract(contractScriptHash).build();
        } catch (IllegalStateException e) {
            if (e.getMessage().equals("Neow3j not set.")) return;
        }
        fail();
    }

    @Test
    public void failWithoutContract() {
        try {
            new Nep5.Builder(this.neow3j).fromContract(null).build();
        } catch (IllegalStateException e) {
            if (e.getMessage().equals("Contract not set.")) return;
        }
        fail();
    }
}
