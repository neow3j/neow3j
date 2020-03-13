package io.neow3j.contract;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.exceptions.ErrorResponseException;
import io.neow3j.protocol.http.HttpService;
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

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private Neow3j neow3j;
    private ScriptHash contract;
    private final ScriptHash ACCT_SCRIPTHASH = new ScriptHash("e9eed8dc39332032dc22e5d6e86332c50327ba23");

    @Before
    public void setUp() {
        // Configuring WireMock to use default host and port "localhost:8080".
        WireMock.configure();
        neow3j = Neow3j.build(new HttpService("http://localhost:8080"));
        this.contract = new ScriptHash(ContractTestUtils.NEP5_CONTRACT_SCRIPT_HASH);
    }

    @Test
    public void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<Nep5> constructor = Nep5.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void name() throws IOException, ErrorResponseException {
        ContractTestUtils.setUpWireMockForInvokeFunction("name", "invokefunction_name.json");
        Nep5 nep5 = new Nep5.Builder(this.neow3j)
                .fromContract(this.contract)
                .build();
        assertThat(nep5.name(), is("Example"));
    }

    @Test
    public void totalSupply() throws IOException, ErrorResponseException {
        ContractTestUtils.setUpWireMockForInvokeFunction("totalSupply", "invokefunction_totalSupply.json");
        Nep5 nep5 = new Nep5.Builder(this.neow3j)
                .fromContract(this.contract)
                .build();
        assertThat(nep5.totalSupply(), is(new BigInteger("1000000000000000")));
    }

    @Test
    public void symbol() throws IOException, ErrorResponseException {
        ContractTestUtils.setUpWireMockForInvokeFunction("symbol", "invokefunction_symbol.json");
        Nep5 nep5 = new Nep5.Builder(this.neow3j)
                .fromContract(this.contract)
                .build();
        assertThat(nep5.symbol(), is("EXP"));
    }

    @Test
    public void decimals() throws IOException, ErrorResponseException {
        ContractTestUtils.setUpWireMockForInvokeFunction("decimals", "invokefunction_decimals.json");
        Nep5 nep5 = new Nep5.Builder(this.neow3j)
                .fromContract(this.contract)
                .build();
        assertThat(nep5.decimals(), is(new BigInteger("8")));

    }

    @Test
    public void balanceOf() throws IOException, ErrorResponseException {
        ContractTestUtils.setUpWireMockForInvokeFunction("balanceOf", "invokefunction_balanceOf.json");
        Nep5 nep5 = new Nep5.Builder(this.neow3j)
                .fromContract(this.contract)
                .build();
        assertThat(nep5.balanceOf(this.ACCT_SCRIPTHASH), is(new BigInteger("6500")));
    }

//    @Test
//    public void transfer() throws IOException {
//        ContractTestUtils.setUpWireMockForSendRawTransaction();
//
//        rawtrans=d1014d02dc0514f2ae394071b7e30d48edbb7d04a520516e8fe1201423ba2703c53263e8d6e522dc32203339dcd8eee953c1087472616e73666572672c2a9b9bdc6b01b8ace2e6c4a4546bbfde4e0d470000000000000000022023ba2703c53263e8d6e522dc32203339dcd8eee9f00c00000170d4cb9413869f3af80000014140d34842917cc3c07fc85181798cd8f3e79e791f37a2ab08da3b8ed52786c983733f4076b78fb4f084ab109b03c81c67d386f59154e9a97ba8cbd79a92970f37792321031a6c6fbbdf02ca351745fa86b9ba5a9452d785ac4f7fc2b7548ca2a46c4fcf4aac
//    }
//
//    @Test
//    public void transfer() {
//      test: normal transfer
//      test: negative amount value to throw Exception
//    }
}
