package io.neow3j.contract;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.string;
import static io.neow3j.ContractTestHelper.setUpWireMockForCall;
import static io.neow3j.ContractTestHelper.setUpWireMockForInvokeFunction;
import static io.neow3j.transaction.Signer.calledByEntry;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.TestProperties;
import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.RecordType;
import io.neow3j.protocol.core.methods.response.NameState;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.transaction.WitnessScope;
import io.neow3j.types.Hash160;
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
    private Account account2;

    private NeoNameService nameServiceContract;
    private static final Hash160 nameServiceHash = Hash160.ZERO;

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
        account1 = Account.fromWIF(TestProperties.defaultAccountWIF());
        account2 = Account.fromWIF(TestProperties.client1AccountWIF());
        nameServiceContract = new NeoNameService(Hash160.ZERO, neow);
    }

    @Test
    public void getName() {
        assertThat(nameServiceContract.getName(), is("NameService"));
    }

    @Test
    public void getTotalSupply() throws IOException {
        setUpWireMockForInvokeFunction(TOTAL_SUPPLY, "nns_invokefunction_totalSupply.json");
        assertThat(nameServiceContract.getTotalSupply(), is(new BigInteger("25001")));
    }

    @Test
    public void getSymbol() throws IOException {
        setUpWireMockForInvokeFunction(SYMBOL, "nns_invokefunction_symbol.json");
        assertThat(nameServiceContract.getSymbol(), is("NNS"));
    }

    @Test
    public void getDecimals() throws IOException {
        setUpWireMockForInvokeFunction(DECIMALS, "nns_invokefunction_decimals.json");
        assertThat(nameServiceContract.getDecimals(), is(0));
    }

    @Test
    public void ownerOf() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(OWNER_OF, "nns_ownerof.json");
        assertThat(nameServiceContract.ownerOf("client1.neo"),
                is(new Hash160(TestProperties.defaultAccountScriptHash())));
    }

    @Test
    public void properties() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(PROPERTIES, "nns_invokefunction_properties.json");
        NameState nameState = nameServiceContract.properties("client1.neo");
        assertThat(nameState.getName(), is("client1.neo"));
        assertThat(nameState.getExpiration(), is(1646214292L));
    }

    @Test
    public void properties_unexpectedReturnType() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(PROPERTIES, "invokefunction_returnInt.json");
        expectedException.expect(UnexpectedReturnTypeException.class);
        expectedException.expectMessage("Integer but expected Map");
        nameServiceContract.properties("client1.neo");
    }

    @Test
    public void balanceOf() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(BALANCE_OF, "nft_balanceof.json");
        assertThat(nameServiceContract.balanceOf(account1.getScriptHash()),
                is(new BigInteger("244")));
    }

    @Test
    public void addRoot() throws IOException {
        setUpWireMockForCall("invokescript", "nns_invokescript_addRoot.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(nameServiceHash, ADD_ROOT, singletonList(string("neow")))
                .toArray();

        Wallet w = Wallet.withAccounts(account1);
        TransactionBuilder b = nameServiceContract.addRoot("neow")
                .wallet(w)
                .signers(calledByEntry(account1.getScriptHash()));

        assertThat(b.getSigners().get(0).getScriptHash(), is(account1.getScriptHash()));
        assertThat(b.getSigners().get(0).getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void addRoot_checkRegexMatch() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The provided input does not match the required regex.");
        nameServiceContract.addRoot("invalid.root");
    }

    @Test
    public void setPrice() throws IOException {
        setUpWireMockForCall("invokescript", "nns_returnAny.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(nameServiceHash, SET_PRICE,
                        singletonList(integer(new BigInteger("1000000000000"))))
                .toArray();

        Wallet w = Wallet.withAccounts(account1);
        TransactionBuilder b = nameServiceContract.setPrice(new BigInteger("1000000000000"))
                .wallet(w)
                .signers(calledByEntry(account1.getScriptHash()));

        assertThat(b.getSigners().get(0).getScriptHash(), is(account1.getScriptHash()));
        assertThat(b.getSigners().get(0).getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void setPrice_negative() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The price needs to be");
        new NeoNameService(nameServiceHash, neow).setPrice(new BigInteger("-1"));
    }

    @Test
    public void setPrice_zero() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The price needs to be");
        nameServiceContract.setPrice(new BigInteger("0"));
    }

    @Test
    public void setPrice_tooHigh() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The price needs to be");
        nameServiceContract.setPrice(new BigInteger("1000000000001"));
    }

    @Test
    public void getPrice() throws IOException {
        setUpWireMockForInvokeFunction(GET_PRICE, "nns_invokefunction_getPrice.json");
        assertThat(nameServiceContract.getPrice(), is(new BigInteger("1000000000")));
    }

    @Test
    public void isAvailable() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        assertFalse(nameServiceContract.isAvailable("second.neo"));
    }

    @Test
    public void isAvailable_rootNotExisting() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "nns_invalidOperation_dueToState.json");
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The root domain 'neow' does not exist");
        nameServiceContract.isAvailable("client1.neow");
    }

    @Test
    public void isAvailable_invalidDomainName() throws IOException {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The provided input does not match the required regex.");
        nameServiceContract.isAvailable("Test.Neo");
    }

    @Test
    public void isAvailable_invalidDomainName_nameTooLong() throws IOException {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The provided input does not match the required regex.");
        nameServiceContract.isAvailable(
                "thistextis63byteslonganditisnotvalidforadomainnametobeusedinneo.neo");
    }

    @Test
    public void isAvailable_invalidDomainName_moreThanTwoLevels() throws IOException {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Only second-level domain names are allowed to be " +
                "registered");
        nameServiceContract.isAvailable("third.second.first");
    }

    @Test
    public void register() throws IOException {
        setUpWireMockForCall("invokescript", "nns_returnAny.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnTrue.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(nameServiceHash, REGISTER,
                        asList(string("client1.neo"), hash160(account1.getScriptHash())))
                .toArray();

        TransactionBuilder b = nameServiceContract
                .register("client1.neo", account1.getScriptHash());
        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void register_domainNotAvailable() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The domain name 'client1.neo' is already taken.");
        nameServiceContract.register("client1.neo", account2.getScriptHash());
    }

    @Test
    public void renew() throws IOException {
        setUpWireMockForCall("invokescript", "nns_returnAny.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(nameServiceHash, RENEW, singletonList(string("client1.neo")))
                .toArray();

        TransactionBuilder b = nameServiceContract.renew("client1.neo");
        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void setAdmin() throws IOException {
        setUpWireMockForCall("invokescript", "nns_returnAny.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(nameServiceHash, SET_ADMIN, asList(string("client1.neo"),
                        hash160(account2.getScriptHash())))
                .toArray();

        TransactionBuilder b = nameServiceContract
                .setAdmin("client1.neo", account2.getScriptHash());
        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void setRecord_typeA() throws IOException {
        setUpWireMockForCall("invokescript", "nns_returnAny.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(nameServiceHash, SET_RECORD,
                        asList(string("client1.neo"), integer(1), string("127.0.0.1")))
                .toArray();

        TransactionBuilder b = nameServiceContract
                .setRecord("client1.neo", RecordType.A, "127.0.0.1");
        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void setRecord_typeA_invalidType() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("input does not match the required regex.");
        nameServiceContract.setRecord("client1.neo", RecordType.A, "notipv4");
    }

    @Test
    public void setRecord_typeA_regexMatching() throws IOException {
        setUpWireMockForCall("invokescript", "nns_returnAny.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");

        // valid IPv6 records
        nameServiceContract.setRecord("client1.neo", RecordType.A, "127.3.5.4");
        nameServiceContract.setRecord("client1.neo", RecordType.A, "123.13.34.65");
        nameServiceContract.setRecord("client1.neo", RecordType.A, "0.0.0.0");
        nameServiceContract.setRecord("client1.neo", RecordType.A, "255.255.255.255");

        // invalid IPv6 records
        try {
            nameServiceContract.setRecord("client1.neo", RecordType.A, "256.0.34.2");
            fail();
        } catch (IllegalArgumentException ignore) {
        }
        try {
            nameServiceContract.setRecord("client1.neo", RecordType.A, "127:0:0:1");
            fail();
        } catch (IllegalArgumentException ignore) {
        }
        try {
            nameServiceContract.setRecord("client1.neo", RecordType.A, "127.0.0.1.1");
            fail();
        } catch (IllegalArgumentException ignore) {
        }
        try {
            nameServiceContract.setRecord("client1.neo", RecordType.A, "0.0");
            fail();
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void setRecord_typeCNAME() throws IOException {
        setUpWireMockForCall("invokescript", "nns_returnAny.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(nameServiceHash, SET_RECORD,
                asList(string("client1.neo"), integer(5), string("firstlevel.client1.neo")))
                .toArray();

        TransactionBuilder b = nameServiceContract
                .setRecord("client1.neo", RecordType.CNAME, "firstlevel.client1.neo");
        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void setRecord_typeCNAME_invalidType() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("input does not match the required regex.");
        nameServiceContract.setRecord("client1.neo", RecordType.CNAME, "notcname");
    }

    @Test
    public void setRecord_typeTXT() throws IOException {
        setUpWireMockForCall("invokescript", "nns_returnAny.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(nameServiceHash, SET_RECORD,
                        asList(string("client1.neo"), integer(16), string("textRecord")))
                .toArray();

        TransactionBuilder b = nameServiceContract
                .setRecord("client1.neo", RecordType.TXT, "textRecord");
        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void setRecord_typeTXT_tooLong() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("data is not valid for the record type TXT.");
        nameServiceContract.setRecord("client1.neo", RecordType.TXT,
                "thistextisintotal5onebyteslongtoberepeatedfivetimesthistextisintotal5onebyteslongtoberepeatedfivetimesthistextisintotal5onebyteslongtoberepeatedfivetimesthistextisintotal5onebyteslongtoberepeatedfivetimesthistextisintotal5onebyteslongtoberepeatedfivetimesx");
    }

    @Test
    public void setRecord_typeAAAA() throws IOException {
        setUpWireMockForCall("invokescript", "nns_returnAny.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");

        byte[] expectedScript =
                new ScriptBuilder().contractCall(nameServiceHash, SET_RECORD,
                        asList(string("client1.neo"), integer(28), string("1234::1234")))
                        .toArray();

        TransactionBuilder b = nameServiceContract
                .setRecord("client1.neo", RecordType.AAAA, "1234::1234");
        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void setRecord_typeAAAA_regexMatching() throws IOException {
        setUpWireMockForCall("invokescript", "nns_returnAny.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");

        // valid IPv6 records
        nameServiceContract.setRecord("client1.neo", RecordType.AAAA, "1234:000:34::2");
        nameServiceContract.setRecord("client1.neo", RecordType.AAAA, "1234:0:0:0:0:0:0:1234");
        nameServiceContract.setRecord("client1.neo", RecordType.AAAA, "1234:0:34::");

        // invalid IPv6 records
        try {
            nameServiceContract.setRecord("client1.neo", RecordType.AAAA, "1234:000::34::2");
            fail();
        } catch (IllegalArgumentException ignore) {
        }
        try {
            nameServiceContract.setRecord("client1.neo", RecordType.AAAA, "1234:000::34::2:");
            fail();
        } catch (IllegalArgumentException ignore) {
        }
        try {
            nameServiceContract.setRecord("client1.neo", RecordType.AAAA, "1234:0:0:0:0:0:0:1" +
                    ":1234");
            fail();
        } catch (IllegalArgumentException ignore) {
        }
        try {
            nameServiceContract.setRecord("client1.neo", RecordType.AAAA, ":1234:0:0:0:0:0:1234");
            fail();
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void setRecord_typeAAAA_invalidType() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("input does not match the required regex.");
        nameServiceContract.setRecord("client1.neo", RecordType.AAAA, "12345::2");
    }

    @Test
    public void getRecord_typeA() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(GET_RECORD, "nns_getRecord_typeA.json");
        String record = nameServiceContract.getRecord("client1.neo", RecordType.A);
        assertThat(record, is("127.0.0.1"));
    }

    @Test
    public void getRecord_typeCNAME() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(GET_RECORD, "nns_getRecord_typeCNAME.json");
        String record = nameServiceContract.getRecord("client1.neo", RecordType.CNAME);
        assertThat(record, is("second.client1.neo"));
    }

    @Test
    public void getRecord_typeTXT() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(GET_RECORD, "nns_getRecord_typeTXT.json");
        String record = nameServiceContract.getRecord("client1.neo", RecordType.TXT);
        assertThat(record, is("textRecord"));
    }

    @Test
    public void getRecord_typeAAAA() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(GET_RECORD, "nns_getRecord_typeAAAA.json");
        String record = nameServiceContract.getRecord("client1.neo", RecordType.AAAA);
        assertThat(record, is("2001:0db8:0000:0000:0000:ff00:0042:8329"));
    }

    @Test
    public void getRecord_noRecord() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(GET_RECORD, "nns_returnAny.json");
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("No record of type AAAA found for the domain name " +
                "'client1.neo'.");
        nameServiceContract.getRecord("client1.neo", RecordType.AAAA);
    }

    @Test
    public void getRecord_noDomainRegistered() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnTrue.json");
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The domain name 'client1.neow' is not registered.");
        nameServiceContract.getRecord("client1.neow", RecordType.AAAA);
    }

    @Test
    public void deleteRecord() throws IOException {
        setUpWireMockForCall("invokescript", "nns_returnAny.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(nameServiceHash, DELETE_RECORD,
                        asList(string("client1.neo"), integer(16)))
                .toArray();

        TransactionBuilder b = nameServiceContract.deleteRecord("client1.neo", RecordType.TXT);
        assertThat(b.getScript(), is(expectedScript));
    }


    @Test
    public void resolve_typeA() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(RESOLVE, "nns_getRecord_typeA.json");
        String record = nameServiceContract.resolve("client1.neo", RecordType.A);
        assertThat(record, is("127.0.0.1"));
    }

    @Test
    public void resolve_typeCNAME() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(RESOLVE, "nns_getRecord_typeCNAME.json");
        String record = nameServiceContract.resolve("client1.neo", RecordType.CNAME);
        assertThat(record, is("second.client1.neo"));
    }

    @Test
    public void resolve_typeTXT() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(RESOLVE, "nns_getRecord_typeTXT.json");
        String record = nameServiceContract.resolve("client1.neo", RecordType.TXT);
        assertThat(record, is("textRecord"));
    }

    @Test
    public void resolve_typeAAAA() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(RESOLVE, "nns_getRecord_typeAAAA.json");
        String record = nameServiceContract.resolve("client1.neo", RecordType.AAAA);
        assertThat(record, is("2001:0db8:0000:0000:0000:ff00:0042:8329"));
    }

    @Test
    public void resolve_noRecord() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(RESOLVE, "nns_returnAny.json");
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("No record of type AAAA found for the domain name " +
                "'client1.neo'.");
        nameServiceContract.resolve("client1.neo", RecordType.AAAA);
    }

    @Test
    public void transfer() throws IOException {
        setUpWireMockForCall("invokescript", "nns_returnAny.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForInvokeFunction(OWNER_OF, "nns_invokefunction_ownerof.json");
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(nameServiceHash, TRANSFER,
                asList(
                        hash160(account2.getScriptHash()),
                        byteArray("636c69656e74312e6e656f"),
                        null))
                .toArray();

        Wallet wallet = Wallet.withAccounts(account1);
        TransactionBuilder b = nameServiceContract
                .transfer(wallet, account2.getScriptHash(), "client1.neo");
        assertThat(b.getScript(), is(expectedScript));
    }

}
