package io.neow3j.contract;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.contract.exceptions.UnresolvableDomainNameException;
import io.neow3j.contract.types.NNSName;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.stackitem.ByteStringStackItem;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.test.TestProperties;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.types.Hash160;
import io.neow3j.types.StackItemType;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.neow3j.test.WireMockTestHelper.setUpWireMockForCall;
import static io.neow3j.test.WireMockTestHelper.setUpWireMockForInvokeFunction;
import static io.neow3j.types.ContractParameter.any;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("unchecked")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NonFungibleTokenTest {

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private Account account1;
    private Account account2;
    private static final Hash160 NF_TOKEN_SCRIPT_HASH = Hash160.fromAddress("NQyYa8wycZRkEvQKr5qRUvMUwyDgvQMqL7");
    private static final byte[] TOKEN_ID = new byte[]{1, 2, 3};
    private static final String TRANSFER = "transfer";
    private static NonFungibleToken nfTestToken;

    @BeforeEach
    public void setUp() {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = wireMockExtension.getPort();
        WireMock.configureFor(port);

        Neow3j neow = Neow3j.build(new HttpService("http://127.0.0.1:" + port));
        nfTestToken = new NonFungibleToken(NF_TOKEN_SCRIPT_HASH, neow);

        account1 = Account.fromWIF(TestProperties.defaultAccountWIF());
        account2 = Account.fromWIF(TestProperties.client1AccountWIF());
    }

    @Test
    public void testTransferNonDivisible() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_0.json");
        setUpWireMockForInvokeFunction("ownerOf", "nft_ownerof.json");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(NF_TOKEN_SCRIPT_HASH, TRANSFER,
                        asList(
                                hash160(account2.getScriptHash()),
                                byteArray(TOKEN_ID),
                                any(null)))
                .toArray();

        TransactionBuilder b = nfTestToken.transfer(account1, account2.getScriptHash(), TOKEN_ID);
        assertThat(b.getScript(), is(expectedScript));
        assertThat(((AccountSigner) b.getSigners().get(0)).getAccount(), is(account1));

        b = nfTestToken.transfer(account2.getScriptHash(), TOKEN_ID);
        assertThat(b.getScript(), is(expectedScript));
        assertThat(b.getSigners().size(), is(0));
    }

    @Test
    public void testTransferNonDivisibleToNNS() throws IOException, UnresolvableDomainNameException {
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_0.json");
        setUpWireMockForInvokeFunction("ownerOf", "nft_ownerof.json");
        setUpWireMockForInvokeFunction("resolve", "nns_resolve_typeTXT.json");

        NNSName nnsName = new NNSName("neow3j.neo");
        Hash160 recipient = Hash160.fromAddress("NTXJgQrqxnSFFqKe3oBejnnzjms61Yzb8r");
        byte[] expectedScript = new ScriptBuilder()
                .contractCall(NF_TOKEN_SCRIPT_HASH, TRANSFER,
                        asList(
                                hash160(recipient),
                                byteArray(TOKEN_ID),
                                any(null)))
                .toArray();

        TransactionBuilder b = nfTestToken.transfer(account1, nnsName, TOKEN_ID);
        assertThat(b.getScript(), is(expectedScript));
        assertThat(((AccountSigner) b.getSigners().get(0)).getAccount(), is(account1));

        b = nfTestToken.transfer(nnsName, TOKEN_ID);
        assertThat(b.getScript(), is(expectedScript));
        assertThat(b.getSigners().size(), is(0));
    }

    @Test
    public void failOnDivisibleTransferWithNonDivisibleNFT() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_5.json");

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> nfTestToken.transfer(account2, account1.getScriptHash(), TOKEN_ID));
        assertThat(thrown.getMessage(), is("This method is only intended for non-divisible NFTs."));
    }

    @Test
    public void testOwnerOfNonDivisible() throws IOException {
        setUpWireMockForInvokeFunction("ownerOf", "nft_ownerof.json");
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_0.json");
        Hash160 owner = nfTestToken.ownerOf(TOKEN_ID);

        assertThat(owner, is(account1.getScriptHash()));
    }

    @Test
    public void testOwnerOfNonDivisible_Divisible() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_5.json");

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> nfTestToken.ownerOf(TOKEN_ID));
        assertThat(thrown.getMessage(), containsString("only intended for non-divisible NFTs."));
    }

    @Test
    public void testOwnerOfNonDivisible_returnNotScriptHash() throws IOException {
        setUpWireMockForInvokeFunction("ownerOf", "response_stack_integer.json");
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_0.json");

        UnexpectedReturnTypeException thrown =
                assertThrows(UnexpectedReturnTypeException.class, () -> nfTestToken.ownerOf(new byte[]{1}));
        assertThat(thrown.getMessage(),
                containsString(format("but expected %s.", StackItemType.BYTE_STRING.jsonValue())));
    }

    @Test
    public void testOwnerOfNonDivisible_returnInvalidAddress() throws IOException {
        setUpWireMockForInvokeFunction("ownerOf", "response_invalid_address.json");
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_0.json");

        UnexpectedReturnTypeException thrown =
                assertThrows(UnexpectedReturnTypeException.class, () -> nfTestToken.ownerOf(new byte[]{1}));
        assertThat(thrown.getMessage(), is("Return type did not contain script hash in expected format."));
    }

    @Test
    public void testGetDecimals() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_5.json");

        assertThat(nfTestToken.getDecimals(), is(5));
    }

    @Test
    public void testBalanceOf() throws IOException {
        setUpWireMockForInvokeFunction("balanceOf", "nft_balanceof.json");
        BigInteger balance = nfTestToken.balanceOf(account1.getScriptHash());

        assertThat(balance, is(new BigInteger("244")));
    }

    @Test
    public void testTokensOf() throws IOException {
        setUpWireMockForInvokeFunction("tokensOf", "invokefunction_iterator_session.json");
        setUpWireMockForCall("traverseiterator", "nft_tokensof_traverseiterator.json");

        Iterator tokensIterator = nfTestToken.tokensOf(account1.getScriptHash());
        List<byte[]> tokens = (List<byte[]>) tokensIterator.traverse(100);

        assertThat(tokens, hasSize(2));
        assertThat(tokens.get(0), is("tokenof1".getBytes(UTF_8)));
        assertThat(tokens.get(1), is("tokenof2".getBytes(UTF_8)));
    }

    @Test
    public void testGetProperties() throws IOException {
        setUpWireMockForInvokeFunction("properties", "nft_properties.json");
        Map<String, String> properties = nfTestToken.properties(new byte[]{1});

        assertThat(properties.get("name"), is("A name"));
    }

    @Test
    public void testGetProperties_unexpectedReturnType() throws IOException {
        setUpWireMockForInvokeFunction("properties", "response_stack_integer.json");

        UnexpectedReturnTypeException thrown =
                assertThrows(UnexpectedReturnTypeException.class, () -> nfTestToken.properties(new byte[]{1}));
        assertThat(thrown.getMessage(), containsString(format("but expected %s.", StackItemType.MAP.jsonValue())));
    }

    @Test
    public void testGetCustomProperties() throws IOException {
        setUpWireMockForInvokeFunction("properties", "nft_customProperties.json");
        Map<String, StackItem> properties = nfTestToken.customProperties(new byte[]{1});

        assertThat(properties.size(), is(4));
        ArrayList<String> keys = new ArrayList<>(properties.keySet());
        assertThat(keys, containsInAnyOrder("name", "map1", "array1", "array2"));
        StackItem nameProperty = properties.get("name");
        assertThat(nameProperty.getType(), is(StackItemType.BYTE_STRING));
        assertThat(nameProperty.getString(), is("yak"));

        StackItem mapProperty1 = properties.get("map1");
        assertThat(mapProperty1.getType(), is(StackItemType.MAP));
        Map<StackItem, StackItem> map1 = mapProperty1.getMap();
        assertThat(map1.get(new ByteStringStackItem("key1".getBytes(UTF_8))).getString(), is("value1"));
        assertThat(map1.get(new ByteStringStackItem("key2".getBytes(UTF_8))).getInteger(), is(BigInteger.valueOf(42)));

        StackItem arrayProperty1 = properties.get("array1");
        assertThat(arrayProperty1.getType(), is(StackItemType.ARRAY));
        List<StackItem> arr1 = arrayProperty1.getList();
        assertThat(arr1.get(0).getString(), is("hello1"));
        assertThat(arr1.get(1).getString(), is("hello2"));

        StackItem arrayProperty2 = properties.get("array2");
        assertThat(arrayProperty2.getType(), is(StackItemType.ARRAY));
        List<StackItem> arr2 = arrayProperty2.getList();
        assertThat(arr2.get(0).getString(), is("b0"));
        assertThat(arr2.get(1).getInteger(), is(BigInteger.valueOf(12)));
    }

    @Test
    public void testTokens() throws IOException {
        setUpWireMockForInvokeFunction("tokens", "invokefunction_iterator_session.json");
        setUpWireMockForCall("traverseiterator", "nft_tokens_traverseiterator.json");

        Iterator<byte[]> iterator = nfTestToken.tokens();
        List<byte[]> tokens = iterator.traverse(20);

        assertThat(tokens, hasSize(2));
        assertThat(tokens.get(0), is("neow#1".getBytes()));
        assertThat(tokens.get(1), is("neow#2".getBytes()));
    }

    @Test
    public void testTransferDivisible() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_5.json");
        byte[] expectedScript = new ScriptBuilder()
                .contractCall(NF_TOKEN_SCRIPT_HASH, TRANSFER,
                        asList(
                                hash160(account1.getScriptHash()),
                                hash160(account2.getScriptHash()),
                                integer(25000), // 0.25
                                byteArray(TOKEN_ID),
                                any(null)))
                .toArray();

        TransactionBuilder b = nfTestToken.transfer(account1, account2.getScriptHash(),
                new BigInteger("25000"), TOKEN_ID);

        assertThat(b.getScript(), is(expectedScript));
        assertThat(((AccountSigner) b.getSigners().get(0)).getAccount(), is(account1));

        b = nfTestToken.transfer(account1.getScriptHash(), account2.getScriptHash(),
                new BigInteger("25000"), TOKEN_ID);

        assertThat(b.getScript(), is(expectedScript));
        assertThat(b.getSigners().size(), is(0));
    }

    @Test
    public void testTransferDivisibleToNNS() throws IOException, UnresolvableDomainNameException {
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_5.json");
        setUpWireMockForInvokeFunction("resolve", "nns_resolve_typeTXT.json");

        NNSName nnsName = new NNSName("neow3j.neo");
        Hash160 recipient = Hash160.fromAddress("NTXJgQrqxnSFFqKe3oBejnnzjms61Yzb8r");

        byte[] expectedScript = new ScriptBuilder()
                .contractCall(NF_TOKEN_SCRIPT_HASH, TRANSFER,
                        asList(
                                hash160(account1.getScriptHash()),
                                hash160(recipient),
                                integer(25000), // 0.25
                                byteArray(TOKEN_ID),
                                any(null)))
                .toArray();

        TransactionBuilder b = nfTestToken.transfer(account1, nnsName, new BigInteger("25000"), TOKEN_ID);

        assertThat(b.getScript(), is(expectedScript));
        assertThat(((AccountSigner) b.getSigners().get(0)).getAccount(), is(account1));

        b = nfTestToken.transfer(account1.getScriptHash(), nnsName, new BigInteger("25000"), TOKEN_ID);

        assertThat(b.getScript(), is(expectedScript));
        assertThat(b.getSigners().size(), is(0));
    }

    @Test
    public void failOnNonDivisibleTransferWithDivisibleNFT() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_0.json");

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> nfTestToken.transfer(account1, account2.getScriptHash(), new BigInteger("25000"), TOKEN_ID));
        assertThat(thrown.getMessage(), is("This method is only intended for divisible NFTs."));
    }

    @Test
    public void testOwnersOf() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_5.json");
        setUpWireMockForInvokeFunction("ownerOf", "invokefunction_iterator_session.json");
        setUpWireMockForCall("traverseiterator", "nft_ownersof_traverseiterator.json");

        Iterator<Hash160> iterator = nfTestToken.ownersOf("tokenId".getBytes());

        List<Hash160> owners = iterator.traverse(100);
        assertThat(owners.get(0), is(new Hash160("88c48eaef7e64b646440da567cd85c9060efbf63")));
        assertThat(owners.get(1), is(new Hash160("739b39ff986ca3839861bbfb443364975c4e59a2")));
    }

    @Test
    public void testOwnersOf_nonDivisible() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_0.json");

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> nfTestToken.ownersOf(TOKEN_ID));
        assertThat(thrown.getMessage(), is("This method is only intended for divisible NFTs."));
    }

    @Test
    public void testBalanceOfDivisible() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_5.json");
        setUpWireMockForInvokeFunction("balanceOf", "nft_balanceof.json");
        BigInteger balance = nfTestToken.balanceOf(account1.getScriptHash(), TOKEN_ID);

        assertThat(balance, is(new BigInteger("244")));
    }

    @Test
    public void testBalanceOfDivisible_NonDivisible() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_0.json");

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> nfTestToken.balanceOf(account1.getScriptHash(), TOKEN_ID));
        assertThat(thrown.getMessage(), is("This method is only intended for divisible NFTs."));
    }

}
