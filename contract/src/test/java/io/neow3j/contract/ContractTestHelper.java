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

public class ContractTestHelper {

    public static void setUpWireMockForCall(String call, String responseFile, String... params)
            throws IOException {

        String responseBody = loadFile("/responses/" + responseFile);

        StringBuilder regexPattern = new StringBuilder()
                .append(".*\"method\":\"").append(call).append("\".*")
                .append(".*\"params\":.*");
        for (String param : params) {
            regexPattern.append(".*").append(param).append(".*");
        }
        WireMock.stubFor(post(urlEqualTo("/"))
                .withRequestBody(new RegexPattern(regexPattern.toString()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(responseBody)));
    }

    public static void setUpWireMockForGetBlockCount(long blockCount) throws IOException {
        String responseBody = loadFile("/responses/getblockcount_" + blockCount + ".json");

        WireMock.stubFor(post(urlEqualTo("/"))
                .withRequestBody(new ContainsPattern("\"method\":\"getblockcount\""))
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
                                                  + ".*\"params\":.*\"" + contractFunction +
                                                  "\".*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(responseBody)));
    }

    public static void setUpWireMockForBalanceOf(Hash160 account, String responseFile)
            throws IOException {

        String responseBody = loadFile("/responses/" + responseFile);

        WireMock.stubFor(post(urlEqualTo("/"))
                .withRequestBody(new RegexPattern(""
                                                  + ".*\"method\":\"invokefunction\""
                                                  + ".*\"params\":.*\"balanceOf\".*"
                                                  + ".*\"" + account.toString() + "\".*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(responseBody)));
    }

    public static String loadFile(String fileName) throws IOException {
        String absFileName = ContractTestHelper.class.getResource(fileName).getFile();
        FileInputStream inStream = new FileInputStream(new File(absFileName));
        return Files.lines(new File(absFileName).toPath(), StandardCharsets.UTF_8)
                .reduce((a, b) -> a + b).get();
    }

}
