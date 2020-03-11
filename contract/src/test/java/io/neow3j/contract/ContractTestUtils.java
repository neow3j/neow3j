package io.neow3j.contract;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ContractTestUtils {

    public static final String NEP5_CONTRACT_REL_PATH = "./test/resources/contracts/nep5contract.py";
    // Script hash of nep5contract in big-endian format.
    public static final String NEP5_CONTRACT_SCRIPT_HASH = "2b019d92a2b0a3babc675a066cf85166f53572bc";

    public static void setUpWireMockForSendRawTransaction() throws IOException {
        String responseBody = loadFile("/responses/sendrawtransaction.json");

        WireMock.stubFor(post(urlEqualTo("/"))
                .withRequestBody(new ContainsPattern("\"method\":\"sendrawtransaction\""))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(responseBody)));
    }

    public static void setUpWireMockForInvokeFunction(String contractFunction, String responseFile)
            throws IOException {

        String responseBody = loadFile("/responses/" + responseFile);

        WireMock.stubFor(post(urlEqualTo("/"))
                .withRequestBody(new RegexPattern(""
                        + ".*\"method\":\"invokefunction\""
                        + ".*\"params\":.*\"" + contractFunction + "\".*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(responseBody)));
    }

    private static String loadFile(String fileName) throws IOException {
        String absFileName = ContractTestUtils.class.getResource(fileName).getFile();
        FileInputStream inStream = new FileInputStream(new File(absFileName));
        return Files.lines(new File(absFileName).toPath(), StandardCharsets.UTF_8)
                .reduce((a, b) -> a + b).get();
    }

}