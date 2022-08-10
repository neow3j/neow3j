package io.neow3j.contract;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.RecordType;
import io.neow3j.protocol.core.response.NameState;
import io.neow3j.protocol.exceptions.InvocationFaultStateException;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.test.TestProperties;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.transaction.WitnessScope;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.math.BigInteger;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.neow3j.test.WireMockTestHelper.setUpWireMockForCall;
import static io.neow3j.test.WireMockTestHelper.setUpWireMockForInvokeFunction;
import static io.neow3j.transaction.AccountSigner.calledByEntry;
import static io.neow3j.types.ContractParameter.array;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.string;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NeoNameServiceTest {

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

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

    @BeforeAll
    public void setUp() {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = wireMockExtension.getPort();
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
        setUpWireMockForInvokeFunction(DECIMALS, "nns_invokefunction_decimals.json");
        setUpWireMockForInvokeFunction(OWNER_OF, "nns_ownerof.json");

        assertThat(nameServiceContract.ownerOf("client1.neo"),
                is(new Hash160(TestProperties.defaultAccountScriptHash())));
    }

    @Test
    public void properties() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(PROPERTIES, "nns_invokefunction_properties.json");
        NameState nameState = nameServiceContract.getNameState("client1.neo");

        assertThat(nameState.getName(), is("client1.neo"));
        assertThat(nameState.getExpiration(), is(1646214292L));
        assertThat(nameState.getAdmin(), is(new Hash160("69ecca587293047be4c59159bf8bc399985c160d")));
    }

    @Test
    public void properties_noAdmin() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(PROPERTIES, "nns_invokefunction_properties_noAdmin.json");
        NameState nameState = nameServiceContract.getNameState("client2.neo");

        assertThat(nameState.getName(), is("client2.neo"));
        assertThat(nameState.getExpiration(), is(1677933305472L));
        assertNull(nameState.getAdmin());
    }

    @Test
    public void properties_unexpectedReturnType() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(PROPERTIES, "invokefunction_returnInt.json");

        UnexpectedReturnTypeException thrown = assertThrows(UnexpectedReturnTypeException.class,
                () -> nameServiceContract.getNameState("client1.neo"));
        assertThat(thrown.getMessage(), is("Got stack item of type Integer but expected Map."));
    }

    @Test
    public void balanceOf() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(BALANCE_OF, "nft_balanceof.json");

        assertThat(nameServiceContract.balanceOf(account1.getScriptHash()), is(new BigInteger("244")));
    }

    @Test
    public void addRoot() throws IOException {
        setUpWireMockForCall("invokescript", "nns_invokescript_addRoot.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(nameServiceHash, ADD_ROOT, singletonList(string("neow")))
                .toArray();

        TransactionBuilder b = nameServiceContract.addRoot("neow")
                .signers(calledByEntry(account1));

        assertThat(b.getSigners().get(0).getScriptHash(), is(account1.getScriptHash()));
        assertThat(b.getSigners().get(0).getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void addRoot_checkRegexMatch() {
        IllegalArgumentException thrown =
                assertThrows(IllegalArgumentException.class, () -> nameServiceContract.addRoot("invalid.root"));
        assertThat(thrown.getMessage(), is("The provided input does not match the required regex."));
    }

    @Test
    public void setPrice() throws IOException {
        setUpWireMockForCall("invokescript", "nns_returnAny.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(nameServiceHash, SET_PRICE,
                        asList(array(integer(BigInteger.valueOf(2_00000000L)),
                                integer(BigInteger.valueOf(1_00000000L)),
                                integer(BigInteger.valueOf(1_50000000L)))))
                .toArray();

        TransactionBuilder b = nameServiceContract.setPrice(asList(BigInteger.valueOf(2_00000000L),
                        BigInteger.valueOf(1_00000000L), BigInteger.valueOf(1_50000000L)))
                .signers(calledByEntry(account1));

        assertThat(b.getSigners().get(0).getScriptHash(), is(account1.getScriptHash()));
        assertThat(b.getSigners().get(0).getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void setPrice_negative() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new NeoNameService(nameServiceHash, neow).setPrice(asList(new BigInteger("-1"))));
        assertThat(thrown.getMessage(), containsString("The prices need to be greater than 0 and smaller than"));
    }

    @Test
    public void setPrice_zero() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> nameServiceContract.setPrice(asList(BigInteger.ZERO)));
        assertThat(thrown.getMessage(), containsString("The prices need to be greater than 0 and smaller than"));
    }

    @Test
    public void setPrice_tooHigh() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> nameServiceContract.setPrice(asList(BigInteger.valueOf(10000_00000001L))));
        assertThat(thrown.getMessage(), containsString("The prices need to be greater than 0 and smaller than"));
    }

    @Test
    public void getPrice() throws IOException {
        setUpWireMockForInvokeFunction(GET_PRICE, "nns_invokefunction_getPrice.json");

        assertThat(nameServiceContract.getPrice(1), is(new BigInteger("1000000000")));
    }

    @Test
    public void isAvailable() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");

        assertFalse(nameServiceContract.isAvailable("second.neo"));
    }

    @Test
    public void isAvailable_rootNotExisting() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "nns_noExistingRoot.json");

        InvocationFaultStateException thrown = assertThrows(InvocationFaultStateException.class,
                () -> nameServiceContract.isAvailable("client1.neow"));
        assertThat(thrown.getMessage(), containsString("An unhandled exception was thrown. The root does not exist."));
    }

    @Test
    public void isAvailable_invalidDomainName() {
        IllegalArgumentException thrown =
                assertThrows(IllegalArgumentException.class, () -> nameServiceContract.isAvailable("Test.Neo"));
        assertThat(thrown.getMessage(), is("The provided input does not match the required regex."));
    }

    @Test
    public void isAvailable_invalidDomainName_nameTooLong() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> nameServiceContract.isAvailable(
                        "thistextis63byteslonganditisnotvalidforadomainnametobeusedinneo.neo"));
        assertThat(thrown.getMessage(), is("The provided input does not match the required regex."));
    }

    @Test
    public void isAvailable_invalidDomainName_moreThanTwoLevels() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> nameServiceContract.isAvailable("third.second.first"));
        assertThat(thrown.getMessage(), is("Only second-level domain names are allowed to be registered."));
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

        TransactionBuilder b = nameServiceContract.register("client1.neo", account1.getScriptHash());

        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void register_domainNotAvailable() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> nameServiceContract.register("client1.neo", account2.getScriptHash()));
        assertThat(thrown.getMessage(), is("The domain name 'client1.neo' is already taken."));
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
                .contractCall(nameServiceHash, SET_ADMIN,
                        asList(string("client1.neo"), hash160(account2.getScriptHash())))
                .toArray();

        TransactionBuilder b = nameServiceContract.setAdmin("client1.neo", account2.getScriptHash());

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

        TransactionBuilder b = nameServiceContract.setRecord("client1.neo", RecordType.A, "127.0.0.1");

        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void setRecord_typeA_invalidType() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> nameServiceContract.setRecord("client1.neo", RecordType.A, "notipv4"));
        assertThat(thrown.getMessage(), is("The provided input does not match the required regex."));
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
        assertThrows(IllegalArgumentException.class,
                () -> nameServiceContract.setRecord("client1.neo", RecordType.A, "256.0.34.2"));
        assertThrows(IllegalArgumentException.class,
                () -> nameServiceContract.setRecord("client1.neo", RecordType.A, "127:0:0:1"));
        assertThrows(IllegalArgumentException.class,
                () -> nameServiceContract.setRecord("client1.neo", RecordType.A, "127.0.0.1.1"));
        assertThrows(IllegalArgumentException.class,
                () -> nameServiceContract.setRecord("client1.neo", RecordType.A, "0.0"));
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

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> nameServiceContract.setRecord("client1.neo", RecordType.CNAME, "notcname"));
        assertThat(thrown.getMessage(), is("The provided input does not match the required regex."));
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

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> nameServiceContract.setRecord("client1.neo", RecordType.TXT,
                        "thistextisintotal5onebyteslongtoberepeatedfivetimesthistextisintotal5onebyteslongtoberepeatedfivetimesthistextisintotal5onebyteslongtoberepeatedfivetimesthistextisintotal5onebyteslongtoberepeatedfivetimesthistextisintotal5onebyteslongtoberepeatedfivetimesx"));
        assertThat(thrown.getMessage(), is("The provided data is not valid for the record type TXT."));
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
        assertThrows(IllegalArgumentException.class,
                () -> nameServiceContract.setRecord("client1.neo", RecordType.AAAA, "1234:000::34::2"));
        assertThrows(IllegalArgumentException.class,
                () -> nameServiceContract.setRecord("client1.neo", RecordType.AAAA, "1234:000::34::2:"));
        assertThrows(IllegalArgumentException.class,
                () -> nameServiceContract.setRecord("client1.neo", RecordType.AAAA, "1234:0:0:0:0:0:0:1:1234"));
        assertThrows(IllegalArgumentException.class,
                () -> nameServiceContract.setRecord("client1.neo", RecordType.AAAA, ":1234:0:0:0:0:0:1234"));
    }

    @Test
    public void setRecord_typeAAAA_invalidType() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> nameServiceContract.setRecord("client1.neo", RecordType.AAAA, "12345::2"));
        assertThat(thrown.getMessage(), is("The provided input does not match the required regex."));
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
        setUpWireMockForInvokeFunction(GET_RECORD, "nns_noRecordOfDomain.json");

        InvocationFaultStateException thrown = assertThrows(InvocationFaultStateException.class,
                () -> nameServiceContract.getRecord("client1.neo", RecordType.AAAA));
        assertThat(thrown.getMessage(),
                containsString("Could not get any record of type AAAA for the domain name 'client1.neo'."));
    }

    @Test
    public void testDomainIsNotAvailableButShouldBe() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> nameServiceContract.checkDomainNameAvailability("client1.neo", true));
        assertThat(thrown.getMessage(), is("The domain name 'client1.neo' is already taken."));
    }

    @Test
    public void testDomainIsAvailableButShouldNot() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnTrue.json");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> nameServiceContract.checkDomainNameAvailability("yak.neo", false));
        assertThat(thrown.getMessage(), is("The domain name 'yak.neo' is not registered."));
    }

    @Test
    public void getRecord_noDomainRegistered() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnTrue.json");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> nameServiceContract.getRecord("client1.neow", RecordType.AAAA));
        assertThat(thrown.getMessage(), is("The domain name 'client1.neow' is not registered."));
    }

    @Test
    public void deleteRecord() throws IOException {
        setUpWireMockForCall("invokescript", "nns_returnAny.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(nameServiceHash, DELETE_RECORD, asList(string("client1.neo"), integer(16)))
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

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> nameServiceContract.resolve("client1.neo", RecordType.AAAA));
        assertThat(thrown.getMessage(), is("No record of type AAAA found for the domain name 'client1.neo'."));
    }

    @Test
    public void transfer() throws IOException {
        setUpWireMockForCall("invokescript", "nns_returnAny.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForInvokeFunction(OWNER_OF, "nns_invokefunction_ownerof.json");
        setUpWireMockForInvokeFunction(DECIMALS, "nns_invokefunction_decimals.json");
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(nameServiceHash, TRANSFER,
                        asList(
                                hash160(account2.getScriptHash()),
                                byteArray("636c69656e74312e6e656f"),
                                null))
                .toArray();

        TransactionBuilder b = nameServiceContract
                .transfer(account1, account2.getScriptHash(), "client1.neo");

        assertThat(b.getScript(), is(expectedScript));
    }

}
