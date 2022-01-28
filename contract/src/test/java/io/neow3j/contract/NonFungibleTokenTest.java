package io.neow3j.contract;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.response.NFTokenState;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.test.TestProperties;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Account;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.neow3j.test.WireMockTestHelper.setUpWireMockForInvokeFunction;
import static io.neow3j.types.ContractParameter.any;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThrows;

public class NonFungibleTokenTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

    private Account account1;
    private Account account2;
    private static final Hash160 NF_TOKEN_SCRIPT_HASH =
            Hash160.fromAddress("NQyYa8wycZRkEvQKr5qRUvMUwyDgvQMqL7");
    private static final byte[] TOKEN_ID = new byte[]{1, 2, 3};
    private static final String TRANSFER = "transfer";
    private static NonFungibleToken nfTestToken;

    @Before
    public void setUp() {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = this.wireMockRule.port();
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
    public void failOnDivisibleTransferWithNonDivisibleNFT() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_5.json");

        assertThrows("only intended for non-divisible NFTs.", IllegalStateException.class,
                () -> nfTestToken.transfer(account2, account1.getScriptHash(), TOKEN_ID)
        );
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

        assertThrows("only intended for non-divisible NFTs.", IllegalStateException.class,
                () -> nfTestToken.ownerOf(TOKEN_ID)
        );
    }

    @Test
    public void testOwnerOfNonDivisible_returnNotScriptHash() throws IOException {
        setUpWireMockForInvokeFunction("ownerOf", "response_stack_integer.json");
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_0.json");

        assertThrows("but expected ByteString", UnexpectedReturnTypeException.class,
                () -> nfTestToken.ownerOf(new byte[]{1})
        );
    }

    @Test
    public void testOwnerOfNonDivisible_returnInvalidAddress() throws IOException {
        setUpWireMockForInvokeFunction("ownerOf", "response_invalid_address.json");
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_0.json");

        assertThrows("Return type did not contain script hash in expected format.",
                UnexpectedReturnTypeException.class,
                () -> nfTestToken.ownerOf(new byte[]{1})
        );
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
        setUpWireMockForInvokeFunction("tokensOf", "nft_tokensof.json");
        List<byte[]> tokens = nfTestToken.tokensOf(account1.getScriptHash());

        assertThat(tokens, hasSize(2));
        assertThat(tokens.get(0), is("tokenof1".getBytes(StandardCharsets.UTF_8)));
        assertThat(tokens.get(1), is("tokenof2".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testGetProperties() throws IOException {
        setUpWireMockForInvokeFunction("properties", "nft_properties.json");
        NFTokenState properties = nfTestToken.properties(new byte[]{1});

        assertThat(properties.getName(), is("A name"));
    }

    @Test
    public void testGetProperties_unexpectedReturnType() throws IOException {
        setUpWireMockForInvokeFunction("properties", "response_stack_integer.json");

        assertThrows("but expected Map", UnexpectedReturnTypeException.class,
                () -> nfTestToken.properties(new byte[]{1})
        );
    }

    @Test
    public void testTokens() throws IOException {
        setUpWireMockForInvokeFunction("tokens", "nft_tokens.json");
        List<byte[]> tokens = nfTestToken.tokens();

        assertThat(tokens, hasSize(2));
        assertThat(tokens.get(0), is("token1".getBytes(StandardCharsets.UTF_8)));
        assertThat(tokens.get(1), is("token2".getBytes(StandardCharsets.UTF_8)));
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
    public void failOnNonDivisibleTransferWithDivisibleNFT() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_0.json");

        assertThrows("only intended for divisible NFTs.", IllegalStateException.class,
                () -> nfTestToken.transfer(account1, account2.getScriptHash(), new BigInteger(
                        "25000"), TOKEN_ID)
        );
    }

    @Test
    public void testOwnersOf() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_5.json");
        setUpWireMockForInvokeFunction("ownerOf", "nft_ownersof.json");
        List<Hash160> owners = nfTestToken.ownersOf(TOKEN_ID);

        assertThat(owners, hasSize(2));
        assertThat(owners, contains(
                new Hash160("c6ae4518b51146820bef3df20bb89da05cfee3df"),
                new Hash160("20be08e5fd3cfa9eafefb85d0243291462e2800f")
        ));
    }

    @Test
    public void testOwnersOf_nonDivisible() throws IOException {
        setUpWireMockForInvokeFunction("decimals", "nft_decimals_0.json");

        assertThrows("only intended for divisible NFTs.", IllegalStateException.class,
                () -> nfTestToken.ownersOf(TOKEN_ID)
        );
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

        assertThrows("only intended for divisible NFTs.", IllegalStateException.class,
                () -> nfTestToken.balanceOf(account1.getScriptHash(), TOKEN_ID)
        );
    }

}
