package io.neow3j.wallet;

import io.neow3j.contract.ScriptHash;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;

public class Nep5Test {

    private Neow3j neow3j;
    private ScriptHash contractScriptHash;

    @Before
    public void setUp() {
        this.neow3j = Neow3j.build(new HttpService("http://localhost:30333"));
        this.contractScriptHash = new ScriptHash("0x646171fd57e768f473f516d0c931f8d58359b5ea");
    }

    @Test
    public void totalSupplyTest() throws IOException {
        Nep5 nep5 = new Nep5.Builder(this.neow3j).fromContract(contractScriptHash).build();
        BigInteger totalSupply = nep5.totalSupply();
        Assert.assertEquals(totalSupply, new BigInteger("2000"));
    }

    @Test
    public void nameTest() throws IOException {
        Nep5 nep5 = new Nep5.Builder(this.neow3j).fromContract(contractScriptHash).build();
        String name = nep5.name();
        Assert.assertTrue(name.equalsIgnoreCase("demo"));
    }

    @Test
    public void symbolTest() throws IOException {
        Nep5 nep5 = new Nep5.Builder(this.neow3j).fromContract(contractScriptHash).build();
        String symbol = nep5.symbol();
        Assert.assertTrue(symbol.equalsIgnoreCase("demo"));
    }

    @Test
    public void decimalsTest() throws IOException {
        Nep5 nep5 = new Nep5.Builder(this.neow3j).fromContract(contractScriptHash).build();
        BigInteger decimals = nep5.decimals();
        Assert.assertEquals(decimals, new BigInteger("8"));
    }

}
