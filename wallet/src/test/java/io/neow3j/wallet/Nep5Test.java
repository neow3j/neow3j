package io.neow3j.wallet;

import io.neow3j.contract.ScriptHash;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoInvoke;
import io.neow3j.protocol.http.HttpService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.math.BigInteger;

import static junit.framework.TestCase.assertEquals;

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
    public void balanceOfTest() throws IOException {
        Mockito.doReturn(new BigInteger("66554545")).when(nep5).balanceOf(new byte[20]);
        assertEquals(new BigInteger("66554545"), nep5.balanceOf(new byte[20]));
    }

    @Test
    public void decimalsTest() throws IOException {
        Mockito.doReturn(new BigInteger("66554545")).when(nep5).decimals();
        assertEquals(new BigInteger("66554545"), nep5.decimals());
    }

    @Test
    public void invokeContractTest() throws IOException {
        NeoInvoke neoInvoke = Mockito.spy(NeoInvoke.class);
        Mockito.doReturn(neoInvoke).when(nep5).invokeContract("symbol");
        assertEquals(neoInvoke, nep5.invokeContract("symbol"));
    }

    @Test
    public void invokeContractWithParamsTest() throws IOException {
        NeoInvoke neoInvoke = Mockito.spy(NeoInvoke.class);
        Mockito.doReturn(neoInvoke).when(nep5).invokeContract("balanceOf", new byte[20]);
        assertEquals(neoInvoke, nep5.invokeContract("balanceOf", new byte[20]));
    }

    @Test (expected = IllegalStateException.class)
    public void failWithoutNeow3j() {
        new Nep5.Builder(null).fromContract(contractScriptHash).build();
    }

    @Test(expected = IllegalStateException.class)
    public void failWithoutContract() {
        new Nep5.Builder(this.neow3j).fromContract(null).build();
    }

    @Test (expected = IllegalStateException.class)
    public void failInvokeContract() throws IOException {
        new Nep5.Builder(this.neow3j).fromContract(contractScriptHash).build().invokeContract(null);;
    }

    @Test (expected = IllegalStateException.class)
    public void failInvokeContractForMethodName() throws IOException {
        new Nep5.Builder(this.neow3j).fromContract(contractScriptHash).build().invokeContract(null, new byte[20]);
    }

    @Test (expected = IllegalStateException.class)
    public void failInvokeContractForRequiredParams() throws IOException {
        new Nep5.Builder(this.neow3j).fromContract(contractScriptHash).build().invokeContract("balanceOf", null);
    }
}
