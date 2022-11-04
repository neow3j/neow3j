package io.neow3j.contract;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.contract.exceptions.UnresolvableDomainNameException;
import io.neow3j.contract.types.NNSName;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.RecordType;
import io.neow3j.protocol.core.response.NameState;
import io.neow3j.protocol.core.response.RecordState;
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
import java.util.List;
import java.util.Map;

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

    private NeoNameService nameService;
    private static Hash160 nameServiceHash;

    private static final String TOTAL_SUPPLY = "totalSupply";
    private static final String SYMBOL = "symbol";
    private static final String DECIMALS = "decimals";

    private static final String OWNER_OF = "ownerOf";
    private static final String BALANCE_OF = "balanceOf";
    private static final String TRANSFER = "transfer";
    private static final String TOKENS = "tokens";
    private static final String PROPERTIES = "properties";

    private static final String ADD_ROOT = "addRoot";
    private static final String ROOTS = "roots";
    private static final String SET_PRICE = "setPrice";
    private static final String GET_PRICE = "getPrice";
    private static final String IS_AVAILABLE = "isAvailable";
    private static final String REGISTER = "register";
    private static final String RENEW = "renew";
    private static final String SET_ADMIN = "setAdmin";
    private static final String SET_RECORD = "setRecord";
    private static final String GET_RECORD = "getRecord";
    private static final String GET_ALL_RECORDS = "getAllRecords";
    private static final String DELETE_RECORD = "deleteRecord";
    private static final String RESOLVE = "resolve";

    @BeforeAll
    public void setUp() {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = wireMockExtension.getPort();
        WireMock.configureFor(port);
        Neow3j neow3j = Neow3j.build(new HttpService("http://127.0.0.1:" + port));
        account1 = Account.fromWIF(TestProperties.defaultAccountWIF());
        account2 = Account.fromWIF(TestProperties.client1AccountWIF());
        nameService = new NeoNameService(neow3j);
        nameServiceHash = nameService.getScriptHash();
    }

    // region NEP-11 methods

    @Test
    public void getName() {
        assertThat(nameService.getName(), is("NameService"));
    }

    @Test
    public void getSymbol() throws IOException {
        setUpWireMockForInvokeFunction(SYMBOL, "nns_invokefunction_symbol.json");
        assertThat(nameService.getSymbol(), is("NNS"));
    }

    @Test
    public void getDecimals() throws IOException {
        setUpWireMockForInvokeFunction(DECIMALS, "nns_invokefunction_decimals.json");
        assertThat(nameService.getDecimals(), is(0));
    }

    @Test
    public void getTotalSupply() throws IOException {
        setUpWireMockForInvokeFunction(TOTAL_SUPPLY, "nns_invokefunction_totalSupply.json");
        assertThat(nameService.getTotalSupply(), is(new BigInteger("25001")));
    }

    @Test
    public void balanceOf() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(BALANCE_OF, "nft_balanceof.json");
        assertThat(nameService.balanceOf(account1.getScriptHash()), is(new BigInteger("244")));
    }

    @Test
    public void ownerOf() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(DECIMALS, "nns_invokefunction_decimals.json");
        setUpWireMockForInvokeFunction(OWNER_OF, "nns_ownerof.json");

        assertThat(nameService.ownerOf(new NNSName("client1.neo")),
                is(new Hash160(TestProperties.defaultAccountScriptHash())));
    }

    @Test
    public void testProperties() throws IOException {
        setUpWireMockForInvokeFunction(PROPERTIES, "nns_properties.json");

        Map<String, String> properties = nameService.properties(new NNSName("neow3j.neo"));
        assertThat(properties.get("image"), is("https://neo3.azureedge.net/images/neons.png"));
        assertThat(properties.get("expiration"), is("1698166908502"));
        assertThat(properties.get("name"), is("neow3j.neo"));
        assertNull(properties.get("admin"));
    }

    @Test
    public void testTransfer() throws IOException {
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

        TransactionBuilder b = nameService.transfer(account1, account2.getScriptHash(), new NNSName("client1.neo"));

        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void testTokens() throws IOException {
        setUpWireMockForInvokeFunction(TOKENS, "invokefunction_iterator_session.json");

        Iterator<byte[]> tokensIterator = nameService.tokens();
        assertThat(tokensIterator.getIteratorId(), is("190d19ca-e935-4ad0-95c9-93b8cf6d115c"));
        assertThat(tokensIterator.getSessionId(), is("a7b35b13-bdfc-4ab3-a398-88a9db9da4fe"));
    }

    @Test
    public void properties() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(PROPERTIES, "nns_invokefunction_properties.json");
        NameState nameState = nameService.getNameState(new NNSName("client1.neo"));

        assertThat(nameState.getName(), is("client1.neo"));
        assertThat(nameState.getExpiration(), is(1646214292L));
        assertThat(nameState.getAdmin(), is(new Hash160("69ecca587293047be4c59159bf8bc399985c160d")));
    }

    @Test
    public void properties_noAdmin() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(PROPERTIES, "nns_invokefunction_properties_noAdmin.json");
        NameState nameState = nameService.getNameState(new NNSName("client2.neo"));

        assertThat(nameState.getName(), is("client2.neo"));
        assertThat(nameState.getExpiration(), is(1677933305472L));
        assertNull(nameState.getAdmin());
    }

    @Test
    public void properties_unexpectedReturnType() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(PROPERTIES, "invokefunction_returnInt.json");

        UnexpectedReturnTypeException thrown = assertThrows(UnexpectedReturnTypeException.class,
                () -> nameService.getNameState(new NNSName("client1.neo")));
        assertThat(thrown.getMessage(), is("Got stack item of type Integer but expected Map."));
    }

    // endregion
    // region custom NNS methods

    @Test
    public void addRoot() throws IOException {
        setUpWireMockForCall("invokescript", "nns_invokescript_addRoot.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(nameServiceHash, ADD_ROOT, asList(string("neow")))
                .toArray();

        TransactionBuilder b = nameService.addRoot(new NNSName.NNSRoot("neow"))
                .signers(calledByEntry(account1));

        assertThat(b.getSigners().get(0).getScriptHash(), is(account1.getScriptHash()));
        assertThat(b.getSigners().get(0).getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void testGetRoots() throws IOException {
        setUpWireMockForInvokeFunction(ROOTS, "invokefunction_iterator_session.json");

        Iterator<String> rootsIterator = nameService.getRoots();
        assertThat(rootsIterator.getIteratorId(), is("190d19ca-e935-4ad0-95c9-93b8cf6d115c"));
        assertThat(rootsIterator.getSessionId(), is("a7b35b13-bdfc-4ab3-a398-88a9db9da4fe"));
    }

    @Test
    public void testUnwrapRoots() throws IOException {
        setUpWireMockForCall("invokescript", "nns_unwrapRoots.json");

        List<String> roots = nameService.getRootsUnwrapped();
        assertThat(roots.get(0), is("eth"));
        assertThat(roots.get(1), is("neo"));
    }

    @Test
    public void testSetPrice() throws IOException {
        setUpWireMockForCall("invokescript", "nns_returnAny.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(nameServiceHash, SET_PRICE,
                        asList(array(
                                integer(BigInteger.valueOf(2_00000000L)),
                                integer(BigInteger.valueOf(1_00000000L)),
                                integer(BigInteger.valueOf(1_50000000L))))
                ).toArray();

        TransactionBuilder b = nameService.setPrice(
                asList(
                        BigInteger.valueOf(2_00000000L),
                        BigInteger.valueOf(1_00000000L),
                        BigInteger.valueOf(1_50000000L)
                )
        ).signers(calledByEntry(account1));

        assertThat(b.getSigners().get(0).getScriptHash(), is(account1.getScriptHash()));
        assertThat(b.getSigners().get(0).getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void testGetPrice() throws IOException {
        setUpWireMockForInvokeFunction(GET_PRICE, "nns_invokefunction_getPrice.json");
        assertThat(nameService.getPrice(1), is(new BigInteger("1000000000")));
    }

    @Test
    public void testIsAvailable() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        assertFalse(nameService.isAvailable(new NNSName("second.neo")));
    }

    @Test
    public void isAvailable_rootNotExisting() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "nns_noExistingRoot.json");

        InvocationFaultStateException thrown = assertThrows(InvocationFaultStateException.class,
                () -> nameService.isAvailable(new NNSName("client1.neow")));
        assertThat(thrown.getMessage(), containsString("An unhandled exception was thrown. The root does not exist."));
    }

    @Test
    public void testRegister() throws IOException {
        setUpWireMockForCall("invokescript", "nns_returnAny.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnTrue.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(nameServiceHash, REGISTER,
                        asList(string("client1.neo"), hash160(account1.getScriptHash())))
                .toArray();

        TransactionBuilder b = nameService.register(new NNSName("client1.neo"), account1.getScriptHash());

        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void testRegister_domainNotAvailable() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> nameService.register(new NNSName("client1.neo"), account2.getScriptHash()));
        assertThat(thrown.getMessage(), is("The domain name 'client1.neo' is already taken."));
    }

    @Test
    public void testRenew() throws IOException {
        setUpWireMockForCall("invokescript", "nns_returnAny.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(nameServiceHash, RENEW, asList(string("client1.neo")))
                .toArray();

        TransactionBuilder b = nameService.renew(new NNSName("client1.neo"));

        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void testRenewYears() throws IOException {
        setUpWireMockForCall("invokescript", "nns_returnAny.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(nameServiceHash, RENEW, asList(string("client1.neo"), integer(3)))
                .toArray();

        TransactionBuilder b = nameService.renew(new NNSName("client1.neo"), 3);

        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void testRenewYears_invalidRange() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> nameService.renew(new NNSName("client1.neo"), 0));
        assertThat(thrown.getMessage(), is("Domain names can only be renewed by at least 1, and at most 10 years."));

        thrown = assertThrows(IllegalArgumentException.class,
                () -> nameService.renew(new NNSName("client1.neo"), 11));
        assertThat(thrown.getMessage(), is("Domain names can only be renewed by at least 1, and at most 10 years."));
    }

    @Test
    public void testSetAdmin() throws IOException {
        setUpWireMockForCall("invokescript", "nns_returnAny.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(nameServiceHash, SET_ADMIN,
                        asList(string("client1.neo"), hash160(account2.getScriptHash())))
                .toArray();

        TransactionBuilder b = nameService.setAdmin(new NNSName("client1.neo"), account2.getScriptHash());

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

        TransactionBuilder b = nameService.setRecord(new NNSName("client1.neo"), RecordType.A, "127.0.0.1");

        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void setRecord_typeCNAME() throws IOException {
        setUpWireMockForCall("invokescript", "nns_returnAny.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(nameServiceHash, SET_RECORD,
                        asList(string("client1.neo"), integer(5), string("firstlevel.client1.neo")))
                .toArray();

        TransactionBuilder b = nameService
                .setRecord(new NNSName("client1.neo"), RecordType.CNAME, "firstlevel.client1.neo");

        assertThat(b.getScript(), is(expectedScript));
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

        TransactionBuilder b = nameService
                .setRecord(new NNSName("client1.neo"), RecordType.TXT, "textRecord");

        assertThat(b.getScript(), is(expectedScript));
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

        TransactionBuilder b = nameService
                .setRecord(new NNSName("client1.neo"), RecordType.AAAA, "1234::1234");

        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void getRecord_typeA() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(GET_RECORD, "nns_getRecord_typeA.json");
        String record = nameService.getRecord(new NNSName("client1.neo"), RecordType.A);

        assertThat(record, is("127.0.0.1"));
    }

    @Test
    public void getRecord_typeCNAME() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(GET_RECORD, "nns_getRecord_typeCNAME.json");
        String record = nameService.getRecord(new NNSName("client1.neo"), RecordType.CNAME);

        assertThat(record, is("second.client1.neo"));
    }

    @Test
    public void getRecord_typeTXT() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(GET_RECORD, "nns_getRecord_typeTXT.json");
        String record = nameService.getRecord(new NNSName("client1.neo"), RecordType.TXT);

        assertThat(record, is("textRecord"));
    }

    @Test
    public void getRecord_typeAAAA() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(GET_RECORD, "nns_getRecord_typeAAAA.json");
        String record = nameService.getRecord(new NNSName("client1.neo"), RecordType.AAAA);

        assertThat(record, is("2001:0db8:0000:0000:0000:ff00:0042:8329"));
    }

    @Test
    public void getRecord_noRecord() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(GET_RECORD, "nns_noRecordOfDomain.json");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> nameService.getRecord(new NNSName("client1.neo"), RecordType.AAAA));
        assertThat(thrown.getMessage(),
                containsString("Could not get a record of type 'AAAA' for the domain name 'client1.neo'."));
    }

    @Test
    public void getRecord_notRegistered() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(GET_RECORD, "nns_getRecord_notRegistered.json");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> nameService.getRecord(new NNSName("client1.neo"), RecordType.AAAA));
        assertThat(thrown.getMessage(),
                containsString("might not be registered or is in an invalid format."));
    }

    @Test
    public void testGetAllRecords() throws IOException {
        setUpWireMockForInvokeFunction(GET_ALL_RECORDS, "invokefunction_iterator_session.json");

        Iterator<RecordState> tokensIterator = nameService.getAllRecords(new NNSName("test.neo"));
        assertThat(tokensIterator.getIteratorId(), is("190d19ca-e935-4ad0-95c9-93b8cf6d115c"));
        assertThat(tokensIterator.getSessionId(), is("a7b35b13-bdfc-4ab3-a398-88a9db9da4fe"));
    }

    @Test
    public void testUnwrapAllRecords() throws IOException {
        setUpWireMockForCall("invokescript", "nns_unwrapAllRecords.json");
        List<RecordState> recordStates = nameService.getAllRecordsUnwrapped(new NNSName("test.neo"));

        RecordState recordState1 = recordStates.get(0);
        assertThat(recordState1.getName(), is("unwrapallrecords.neo"));
        assertThat(recordState1.getRecordType(), is(RecordType.CNAME));
        assertThat(recordState1.getData(), is("neow3j.neo"));

        RecordState recordState2 = recordStates.get(1);
        assertThat(recordState2.getName(), is("unwrapallrecords.neo"));
        assertThat(recordState2.getRecordType(), is(RecordType.TXT));
        assertThat(recordState2.getData(), is("unwrapAllRecordsTXT"));
    }

    @Test
    public void testDeleteRecord() throws IOException {
        setUpWireMockForCall("invokescript", "nns_returnAny.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(nameServiceHash, DELETE_RECORD, asList(string("client1.neo"), integer(16)))
                .toArray();

        TransactionBuilder b = nameService.deleteRecord(new NNSName("client1.neo"), RecordType.TXT);

        assertThat(b.getScript(), is(expectedScript));
    }

    @Test
    public void resolve_typeA() throws IOException, UnresolvableDomainNameException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(RESOLVE, "nns_resolve_typeA.json");

        String record = nameService.resolve(new NNSName("client1.neo"), RecordType.A);
        assertThat(record, is("157.0.0.1"));
    }

    @Test
    public void resolve_typeCNAME() throws IOException, UnresolvableDomainNameException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(RESOLVE, "nns_resolve_typeCNAME.json");

        String record = nameService.resolve(new NNSName("client1.neo"), RecordType.CNAME);
        assertThat(record, is("neow3j.io"));
    }

    @Test
    public void resolve_typeTXT() throws IOException, UnresolvableDomainNameException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(RESOLVE, "nns_resolve_typeTXT.json");

        String record = nameService.resolve(new NNSName("client1.neo"), RecordType.TXT);
        assertThat(record, is("NTXJgQrqxnSFFqKe3oBejnnzjms61Yzb8r"));
    }

    @Test
    public void resolve_typeAAAA() throws IOException, UnresolvableDomainNameException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(RESOLVE, "nns_resolve_typeAAAA.json");

        String record = nameService.resolve(new NNSName("client1.neo"), RecordType.AAAA);
        assertThat(record, is("3001:2:3:4:5:6:7:8"));
    }

    @Test
    public void resolve_noRecord() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");
        setUpWireMockForInvokeFunction(RESOLVE, "nns_returnAny.json");

        UnresolvableDomainNameException thrown = assertThrows(UnresolvableDomainNameException.class,
                () -> nameService.resolve(new NNSName("client1.neo"), RecordType.AAAA));
        assertThat(thrown.getMessage(), containsString(" 'client1.neo' could not be resolved."));
    }

    @Test
    public void testGetNameState() throws IOException {
        setUpWireMockForInvokeFunction(PROPERTIES, "nns_getNameState.json");

        NameState nameState = nameService.getNameState(new NNSName("namestate.neo"));
        assertThat(nameState.getName(), is("getnamestatewithbytes.neo"));
        assertThat(nameState.getExpiration(), is(1698165160330L));
        assertThat(nameState.getAdmin(), is(Hash160.fromAddress("NV1Q1dTdvzPbThPbSFz7zudTmsmgnCwX6c")));
    }

    // endregion
    // region availability check

    @Test
    public void testDomainIsNotAvailableButShouldBe() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnFalse.json");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> nameService.checkDomainNameAvailability(new NNSName("client1.neo"), true));
        assertThat(thrown.getMessage(), is("The domain name 'client1.neo' is already taken."));
    }

    @Test
    public void testDomainIsAvailableButShouldNot() throws IOException {
        setUpWireMockForInvokeFunction(IS_AVAILABLE, "invokefunction_returnTrue.json");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> nameService.checkDomainNameAvailability(new NNSName("yak.neo"), false));
        assertThat(thrown.getMessage(), is("The domain name 'yak.neo' is not registered."));
    }

    // endregion

}
