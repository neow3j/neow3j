package io.neow3j.contract;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.neow3j.contract.types.NNSName;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.math.BigDecimal;
import java.math.BigInteger;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.neow3j.test.TestProperties.gasTokenHash;
import static io.neow3j.test.TestProperties.neoTokenHash;
import static io.neow3j.test.WireMockTestHelper.setUpWireMockForBalanceOf;
import static io.neow3j.test.WireMockTestHelper.setUpWireMockForCall;
import static io.neow3j.test.WireMockTestHelper.setUpWireMockForGetBlockCount;
import static io.neow3j.test.WireMockTestHelper.setUpWireMockForInvokeFunction;
import static io.neow3j.types.ContractParameter.any;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FungibleTokenTest {

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private FungibleToken neoToken;
    private FungibleToken gasToken;
    private Account account1;
    private Account account2;
    private static final Hash160 RECIPIENT_SCRIPT_HASH =
            new Hash160("969a77db482f74ce27105f760efa139223431394");

    private static final String NEP17_TRANSFER = "transfer";

    @BeforeAll
    public void setUp() {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = wireMockExtension.getPort();
        WireMock.configureFor(port);
        Neow3j neow = Neow3j.build(new HttpService("http://127.0.0.1:" + port));

        neoToken = new FungibleToken(new Hash160(neoTokenHash()), neow);
        gasToken = new FungibleToken(new Hash160(gasTokenHash()), neow);

        account1 = new Account(ECKeyPair.create(
                hexStringToByteArray(
                        "1dd37fba80fec4e6a6f13fd708d8dcb3b29def768017052f6c930fa1c5d90bbb")));
        account2 = new Account(ECKeyPair.create(
                hexStringToByteArray(
                        "b4b2b579cac270125259f08a5f414e9235817e7637b9a66cfeb3b77d90c8e7f9")));
    }

    @Test
    public void transferFromAccount() throws Throwable {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals_gas.json");
        setUpWireMockForInvokeFunction("balanceOf", "invokefunction_balanceOf_300000000.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(new Hash160(gasTokenHash()), NEP17_TRANSFER,
                        asList(hash160(account1.getScriptHash()),
                                hash160(RECIPIENT_SCRIPT_HASH),
                                integer(gasToken.toFractions(BigDecimal.ONE)),
                                any(null)))
                .toArray();

        // 1. Option: Sender is an account
        Transaction tx = gasToken.transfer(account1, RECIPIENT_SCRIPT_HASH, new BigInteger("100000000"))
                .getUnsignedTransaction();

        assertThat(tx.getScript(), is(expectedScript));
        assertThat(((AccountSigner) tx.getSigners().get(0)).getAccount(), is(account1));

        // 2. Option: Sender is a script hash
        TransactionBuilder builder = gasToken.transfer(
                account1.getScriptHash(), RECIPIENT_SCRIPT_HASH, new BigInteger("100000000"));

        assertThat(builder.getScript(), is(expectedScript));
        assertThat(builder.getSigners().size(), is(0));
    }

    @Test
    public void testGetBalanceOfAccount() throws Exception {
        setUpWireMockForBalanceOf(account1.getScriptHash().toString(),
                "invokefunction_balanceOf_300000000.json");

        assertThat(gasToken.getBalanceOf(account1.getScriptHash()),
                is(new BigInteger("300000000")));
    }

    @Test
    public void testGetBalanceOfAccount_address() throws Exception {
        setUpWireMockForBalanceOf(account1.getScriptHash().toString(),
                "invokefunction_balanceOf_300000000.json");

        assertThat(gasToken.getBalanceOf(account1), is(new BigInteger("300000000")));
    }

    @Test
    public void testGetBalanceOfAccount_account() throws Exception {
        setUpWireMockForBalanceOf(account1.getScriptHash().toString(),
                "invokefunction_balanceOf_300000000.json");

        assertThat(gasToken.getBalanceOf(account1), is(new BigInteger("300000000")));
    }

    @Test
    public void testGetBalanceOfWallet() throws Exception {
        setUpWireMockForCall("invokefunction", "invokefunction_balanceOf_300000000.json",
                "balanceOf", account1.getScriptHash().toString());
        setUpWireMockForCall("invokefunction", "invokefunction_balanceOf_300000000.json",
                "balanceOf", account2.getScriptHash().toString());

        assertThat(gasToken.getBalanceOf(Wallet.withAccounts(account1, account2)),
                is(new BigInteger("600000000")));
    }

    @Test
    public void testTransfer_illegalAmountProvided() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> neoToken.transfer(account1, RECIPIENT_SCRIPT_HASH, new BigInteger("-2")));
        assertThat(thrown.getMessage(), is("The amount must be greater than or equal to 0."));
    }

    // region transfer with NNS recipient

    @Test
    public void transferToNNSName() throws Throwable {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");
        setUpWireMockForGetBlockCount(1000);
        setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals_gas.json");
        setUpWireMockForInvokeFunction("balanceOf", "invokefunction_balanceOf_300000000.json");
        setUpWireMockForInvokeFunction("resolve", "nns_resolve_typeTXT.json");

        int amount = 3_00000000;
        NNSName nnsName = new NNSName("neow3j.neo");
        Hash160 recipient = Hash160.fromAddress("NTXJgQrqxnSFFqKe3oBejnnzjms61Yzb8r");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(new Hash160(gasTokenHash()), "transfer",
                        asList(hash160(account1.getScriptHash()),
                                hash160(recipient),
                                integer(amount),
                                any(null)))
                .toArray();

        // 1. Option: Sender is an account
        Transaction tx = gasToken.transfer(account1, nnsName, BigInteger.valueOf(amount))
                .getUnsignedTransaction();

        assertThat(tx.getScript(), is(expectedScript));
        assertThat(((AccountSigner) tx.getSigners().get(0)).getAccount(), is(account1));

        // 2. Option: Sender is a script hash
        TransactionBuilder builder = gasToken.transfer(account1.getScriptHash(), nnsName,
                BigInteger.valueOf(3_00000000));

        assertThat(builder.getScript(), is(expectedScript));
        assertThat(builder.getSigners().size(), is(0));
    }

    // endregion

}
