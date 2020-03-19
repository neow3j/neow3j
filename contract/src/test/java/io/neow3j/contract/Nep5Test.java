package io.neow3j.contract;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.exceptions.ErrorResponseException;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.wallet.Account;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.math.BigInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class Nep5Test {

    /**
     * The neow3j object configured to use default localhost and port 8080.
     */
    private final Neow3j NEOW3J = Neow3j.build(new HttpService("http://localhost:8080"));

    /**
     * An empty Neow3j instance for all tests which don't actually need to make the final invocation
     * call.
     */
    private static final Neow3j EMPTY_NEOW3J = Neow3j.build(null);

    /**
     * The script hash of the used NEP5 contract in the location: ./test/resources/contracts/nep5contract.py
     */
    private final ScriptHash NEP5_CONTRACT_SCRIPT_HASH = new ScriptHash(ContractTestUtils.NEP5_CONTRACT_SCRIPT_HASH);

    /**
     * The account with address AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y.
     */
    private final Account ACCOUNT = Account.fromWIF("KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr").build();

    /**
     * The script hash from the account with address AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y.
     * (e9eed8dc39332032dc22e5d6e86332c50327ba23)
     */
    private final ScriptHash ACCT_SCRIPTHASH = ACCOUNT.getScriptHash();

    /**
     * The script hash from the recipient's address.
     */
    private final ScriptHash TO_ACCT_SCRIPTHASH = ScriptHash.fromAddress("AWKECj9RD8rS8RPcpCgYVjk1DeYyHwxZm3");

    /**
     * The WireMockRule used for this test class.
     */
    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    /**
     * This sets up the WireMock. The WireMock catches and handles the RPC calls that are tested according to the
     * default host and port of the neow3j object in this test class.
     */
    @Before
    public void setUpTest() {
        WireMock.configure();
    }

    /**
     * Tests that the default constructor of the Nep5 class cannot be called.
     *
     * @throws NoSuchMethodException if a matching method is not found.
     * @throws IllegalAccessException if this {@code Constructor} object
     *              is enforcing Java language access control and the underlying
     *              constructor is inaccessible.
     * @throws InvocationTargetException if the underlying constructor
     *              throws an exception.
     * @exception InstantiationException if the class that declares the
     *              underlying constructor represents an abstract class.
     */
    @Test
    public void constructorIsPrivateTest() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<Nep5> constructor = Nep5.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    /**
     * Tests the RPC call with the operation name in the smart contract.
     *
     * @throws IOException              if a connection problem with the RPC node arises.
     * @throws ErrorResponseException   if the call to the node lead to an error. Not due to the
     *                                  contract invocation itself but due to the call in general.
     */
    @Test
    public void nameTest() throws IOException, ErrorResponseException {
        ContractTestUtils.setUpWireMockForInvokeFunction("name", "invokefunction_name.json");
        Nep5 nep5 = new Nep5.Builder(this.NEOW3J)
                .fromContract(this.NEP5_CONTRACT_SCRIPT_HASH)
                .build();
        assertThat(nep5.name(), is("Example"));
    }

    /**
     * Tests the RPC call with the operation totalSupply in the smart contract.
     *
     * @throws IOException              if a connection problem with the RPC node arises.
     * @throws ErrorResponseException   if the call to the node lead to an error. Not due to the
     *                                  contract invocation itself but due to the call in general.
     */
    @Test
    public void totalSupplyTest() throws IOException, ErrorResponseException {
        ContractTestUtils.setUpWireMockForInvokeFunction("totalSupply", "invokefunction_totalSupply.json");
        Nep5 nep5 = new Nep5.Builder(this.NEOW3J)
                .fromContract(this.NEP5_CONTRACT_SCRIPT_HASH)
                .build();
        assertThat(nep5.totalSupply(), is(new BigInteger("1000000000000000")));
    }

    /**
     * Tests the RPC call with the operation symbol in the smart contract.
     *
     * @throws IOException              if a connection problem with the RPC node arises.
     * @throws ErrorResponseException   if the call to the node lead to an error. Not due to the
     *                                  contract invocation itself but due to the call in general.
     */
    @Test
    public void symbolTest() throws IOException, ErrorResponseException {
        ContractTestUtils.setUpWireMockForInvokeFunction("symbol", "invokefunction_symbol.json");
        Nep5 nep5 = new Nep5.Builder(this.NEOW3J)
                .fromContract(this.NEP5_CONTRACT_SCRIPT_HASH)
                .build();
        assertThat(nep5.symbol(), is("EXP"));
    }

    /**
     * Tests the RPC call with the operation decimals in the smart contract.
     *
     * @throws IOException              if a connection problem with the RPC node arises.
     * @throws ErrorResponseException   if the call to the node lead to an error. Not due to the
     *                                  contract invocation itself but due to the call in general.
     */
    @Test
    public void decimalsTest() throws IOException, ErrorResponseException {
        ContractTestUtils.setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        Nep5 nep5 = new Nep5.Builder(this.NEOW3J)
                .fromContract(this.NEP5_CONTRACT_SCRIPT_HASH)
                .build();
        assertThat(nep5.decimals(), is(new BigInteger("8")));

    }

    /**
     * Tests the RPC call with the operation balanceOf in the smart contract.
     *
     * @throws IOException              if a connection problem with the RPC node arises.
     * @throws ErrorResponseException   if the call to the node lead to an error. Not due to the
     *                                  contract invocation itself but due to the call in general.
     */
    @Test
    public void balanceOfTest() throws IOException, ErrorResponseException {
        ContractTestUtils.setUpWireMockForInvokeFunction("balanceOf", "invokefunction_balanceOf.json");
        Nep5 nep5 = new Nep5.Builder(this.NEOW3J)
                .fromContract(this.NEP5_CONTRACT_SCRIPT_HASH)
                .build();
        assertThat(nep5.balanceOf(this.ACCT_SCRIPTHASH), is(new BigInteger("6500")));
    }

    /**
     * Tests the transfer operation in the smart contract.
     *
     * @throws IOException            if a connection problem with the RPC node arises.
     * @throws ErrorResponseException if the execution of the invocation lead to an error on the RPC
     *                                node.
     */
    @Test
    public void transferTest() throws IOException, ErrorResponseException {
        ContractTestUtils.setUpWireMockForSendRawTransaction();
        Nep5 nep5 = new Nep5.Builder(this.NEOW3J)
                .fromContract(this.NEP5_CONTRACT_SCRIPT_HASH)
                .build();
        assertThat(nep5.transfer(ACCOUNT, TO_ACCT_SCRIPTHASH, new BigInteger("20")), is(Boolean.TRUE));
    }

    /**
     * Tests that the builder throws an exception, if the amount to transfer is negative.
     */
    @Test(expected = IllegalArgumentException.class)
    public void noPositiveAmountTransferTest() throws IOException, ErrorResponseException {
        Nep5 nep5 = new Nep5.Builder(this.NEOW3J)
                .fromContract(this.NEP5_CONTRACT_SCRIPT_HASH)
                .build();
        nep5.transfer(ACCOUNT, TO_ACCT_SCRIPTHASH, new BigInteger("-5"));
    }

    /**
     * Tests the transfer operation in the smart contract with the parameter amount as a String.
     *
     * @throws IOException            if a connection problem with the RPC node arises.
     * @throws ErrorResponseException if the execution of the invocation lead to an error on the RPC
     *                                node.
     */
    @Test
    public void transferStringTest() throws IOException, ErrorResponseException {
        ContractTestUtils.setUpWireMockForSendRawTransaction();
        Nep5 nep5 = new Nep5.Builder(this.NEOW3J)
                .fromContract(this.NEP5_CONTRACT_SCRIPT_HASH)
                .build();
        assertThat(nep5.transfer(ACCOUNT, TO_ACCT_SCRIPTHASH, "20"), is(Boolean.TRUE));
    }

    /**
     * Tests that the builder throws an exception, if the amount to transfer is negative.
     * Tests the transfer method that takes the parameter amount as a String.
     */
    @Test(expected = IllegalArgumentException.class)
    public void noPositiveAmountTransferStringTest() throws IOException, ErrorResponseException {
        Nep5 nep5 = new Nep5.Builder(this.NEOW3J)
                .fromContract(this.NEP5_CONTRACT_SCRIPT_HASH)
                .build();
        nep5.transfer(ACCOUNT, TO_ACCT_SCRIPTHASH, "-5");
    }

    /**
     * Tests that the builder throws an exception, if the required script hash is not set.
     */
    @Test(expected = IllegalStateException.class)
    public void notAddingRequiredScriptHash() {
        new Nep5.Builder(EMPTY_NEOW3J)
                .build();
    }

    /**
     * Tests that the builder throws an exception, if the required neow3j is not set.
     */
    @Test(expected = IllegalStateException.class)
    public void notAddingRequiredNeow3j() {
        new Nep5.Builder(null)
                .fromContract(NEP5_CONTRACT_SCRIPT_HASH)
                .build();
    }
}
