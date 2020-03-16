package io.neow3j.contract;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ContractTestUtils {

    /**
     * The script hash of the used NEP5 contract in big-endian format.
     * (The contract is located in ./test/resources/contracts/nep5contract.py)
     */
    public static final String NEP5_CONTRACT_SCRIPT_HASH = "2b019d92a2b0a3babc675a066cf85166f53572bc";

    /**
     * This method mocks the SendRawTransaction request and refers to the sendrawtransaction.json as the response.
     *
     * @throws IOException if the response .json is not found.
     */
    public static void setUpWireMockForSendRawTransaction() throws IOException {
        String responseBody = loadFile("/responses/sendrawtransaction.json");

        WireMock.stubFor(post(urlEqualTo("/"))
                .withRequestBody(new ContainsPattern("\"method\":\"sendrawtransaction\""))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(responseBody)));
    }

    /**
     * Mocks the RPC calls and refers to the according .json as the response.
     *
     * @param contractFunction The operation function that is called on the contract.
     * @param responseFile The response file according to the function called.
     * @throws IOException if the response file is not found.
     */
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

    /**
     * Loads a file from a relative path.
     *
     * @param fileName The file to be loaded.
     * @return The file in one single String.
     * @throws IOException if the file is not found.
     */
    private static String loadFile(String fileName) throws IOException {
        String absFileName = ContractTestUtils.class.getResource(fileName).getFile();
        return Files.lines(new File(absFileName).toPath(), StandardCharsets.UTF_8)
                .reduce((a, b) -> a + b).get();
    }
}
