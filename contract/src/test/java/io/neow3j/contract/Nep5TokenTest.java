package io.neow3j.contract;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class Nep5TokenTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private Neow3j neow;
    private ScriptHash contract;

    @Before
    public void setUp() {
        // Configuring WireMock to use default host and port "localhost:8080".
        WireMock.configure();
        neow = Neow3j.build(new HttpService("http://localhost:8080"));
        // Contract with hash "4b7f02924c1949b722ad8d89687ca70968b76e86" is at
        // ./test/resources/contracts/example-nep5.py
        this.contract = new ScriptHash("4b7f02924c1949b722ad8d89687ca70968b76e86");
    }

    @Test
    public void transferToken() throws Exception {
        Nep5Token nep5 = new Nep5Token(this.contract, this.neow);
        nep5.transfer(new ScriptHash("e9eed8dc39332032dc22e5d6e86332c50327ba23"),
            new ScriptHash("0f2b7a6ee34db32d9151c6028960ab2a8babea52"), BigDecimal.ONE);
    }

    @Test
    public void getName() throws IOException {
        setUpWireMockForInvokeFunction("name");
        Nep5Token nep5 = new Nep5Token(this.contract, this.neow);
        assertThat(nep5.getName(), is("Example"));
    }

    @Test
    public void getSymbol() throws IOException {
        setUpWireMockForInvokeFunction("symbol");
        Nep5Token nep5 = new Nep5Token(this.contract, this.neow);
        assertThat(nep5.getSymbol(), is("EXP"));
    }

    @Test
    public void getDecimals() throws Exception {
        setUpWireMockForInvokeFunction("decimals");
        Nep5Token nep5 = new Nep5Token(this.contract, this.neow);
        assertThat(nep5.getDecimals(), is(8));
    }

    @Test
    public void getTotalSupply() throws Exception {
        setUpWireMockForInvokeFunction("totalSupply");
        Nep5Token nep5 = new Nep5Token(this.contract, this.neow);
        assertThat(nep5.getTotalSupply(), is(new BigInteger("1000000000000000")));
    }

    private void setUpWireMockForInvokeFunction(String contractFunction) throws IOException {
        String responseBody = loadFile("/responses/invokefunction_" + contractFunction + ".json");

        WireMock.stubFor(post(urlEqualTo("/"))
            .withRequestBody(new RegexPattern(""
                + ".*\"method\":\"invokefunction\""
                + ".*\"params\":.*\"" + contractFunction + "\".*"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody(responseBody)));
    }

    private String loadFile(String fileName) throws IOException {
        String absFileName = this.getClass().getResource(fileName).getFile();
        FileInputStream inStream = new FileInputStream(new File(absFileName));
        return Files.lines(new File(absFileName).toPath(), StandardCharsets.UTF_8)
            .reduce((a, b) -> a + b).get();
    }
}
