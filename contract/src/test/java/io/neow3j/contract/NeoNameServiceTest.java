package io.neow3j.contract;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.string;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForCall;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForInvokeFunction;
import static io.neow3j.transaction.Signer.calledByEntry;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.WitnessScope;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;

import java.io.IOException;
import java.math.BigInteger;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class NeoNameServiceTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private Account account1;
    private static final String NAMESERVICE_SCRIPTHASH = "a2b524b68dfe43a9d56af84f443c6b9843b8028c";

    private static final String TOTAL_SUPPLY = "totalSupply";
    private static final String SYMBOL = "symbol";
    private static final String DECIMALS = "decimals";

    private static final String OWNER_OF = "ownerOf";
    private static final String PROPERTIES = "properties";
    private static final String BALANCE_OF = "balanceOf";
    private static final String TRANSFER = "transfer";

    private static final String ADD_ROOT = "addRoot";
    private static final String SET_PRICE = "setPrice";
    private static final String GET_PRICE = "getPrice";
    private static final String IS_AVAILABLE = "isAvailable";
    private static final String REGISTER = "register";
    private static final String RENEW = "renew";
    private static final String SET_ADMIN = "setAdmin";
    private static final String SET_RECORD = "setRecord";
    private static final String GET_RECORD = "getRecord";
    private static final String DELETE_RECORD = "deleteRecord";
    private static final String RESOLVE = "resolve";

    private Neow3j neow;

    @Before
    public void setUp() {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = wireMockRule.port();
        WireMock.configureFor(port);
        neow = Neow3j.build(new HttpService("http://127.0.0.1:" + port));
        account1 = new Account(ECKeyPair.create(Numeric.hexStringToByteArray(
                "e6e919577dd7b8e97805151c05ae07ff4f752654d6d8797597aca989c02c4cb3")));
    }

    @Test
    public void scriptHash() {
        assertThat(new NeoNameService(neow).getScriptHash().toString(), is(NAMESERVICE_SCRIPTHASH));
    }

    @Test
    public void getName() {
        assertThat(new NeoNameService(neow).getName(), is("NameService"));
    }

    @Test
    public void getTotalSupply() throws IOException {
        setUpWireMockForInvokeFunction(TOTAL_SUPPLY, "nns_invokefunction_totalSupply.json");
        assertThat(new NeoNameService(neow).getTotalSupply(), is(new BigInteger("25001")));
    }

    @Test
    public void getSymbol() throws IOException {
        setUpWireMockForInvokeFunction(SYMBOL, "nns_invokefunction_symbol.json");
        assertThat(new NeoNameService(neow).getSymbol(), is("NNS"));
    }

    @Test
    public void getDecimals() {
        assertThat(new NeoNameService(neow).getDecimals(), is(0));
    }

    @Test
    public void ownerOf() {
        fail();
    }

    @Test
    public void properties() {
        fail();
    }

    @Test
    public void balanceOf() {
        fail();
    }

    @Test
    public void transfer() {
        fail();
    }

    @Test
    public void addRoot() throws IOException {
        setUpWireMockForCall("invokescript", "nns_invokescript_addRoot.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(NeoNameService.SCRIPT_HASH,
                ADD_ROOT, singletonList(string("neow")))
                .toArray();

        Wallet w = Wallet.withAccounts(account1);
        TransactionBuilder b = new NeoNameService(neow).addRoot("neow")
                .wallet(w)
                .signers(calledByEntry(account1.getScriptHash()));

        assertThat(b.getSigners().get(0).getScriptHash(), is(account1.getScriptHash()));
        assertThat(b.getSigners().get(0).getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void setPrice() throws IOException {
        setUpWireMockForCall("invokescript", "nns_invokescript_setPrice.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(NeoNameService.SCRIPT_HASH,
                SET_PRICE, singletonList(integer(new BigInteger("1000000000000"))))
                .toArray();

        Wallet w = Wallet.withAccounts(account1);
        TransactionBuilder b = new NeoNameService(neow).setPrice(new BigInteger("1000000000000"))
                .wallet(w)
                .signers(calledByEntry(account1.getScriptHash()));

        assertThat(b.getSigners().get(0).getScriptHash(), is(account1.getScriptHash()));
        assertThat(b.getSigners().get(0).getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void getPrice() throws IOException {
        setUpWireMockForInvokeFunction(GET_PRICE, "nns_invokefunction_getPrice.json");
        assertThat(new NeoNameService(neow).getPrice(), is(new BigInteger("1000000000")));
    }

    @Test
    public void isAvailable() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");

        assertFalse(new NeoNameService(neow).isAvailable("second.neo"));
    }

    @Test
    public void register() {
        fail();
    }

    @Test
    public void renew() {
        fail();
    }

    @Test
    public void setAdmin() {
        fail();
    }

    @Test
    public void setRecord() {
        fail();
    }

    @Test
    public void getRecord() {
        fail();
    }

    @Test
    public void deleteRecord() {
        fail();
    }

    @Test
    public void resolve() {
        fail();
    }
}
