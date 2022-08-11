package io.neow3j.wallet;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class WalletTestHelper {

    public static void setUpWireMockForCall(String call, String responseFile, String param)
            throws IOException {

        String responseBody = loadFile("/responses/" + responseFile);

        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/"))
                .withRequestBody(new RegexPattern(""
                        + ".*\"method\":\"" + call + "\".*"
                        + ".*\"params\":.*"
                        + ".*\"" + param + "\".*"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withBody(responseBody)));
    }

    public static String loadFile(String fileName) throws IOException {
        String absFileName = WalletTestHelper.class.getResource(fileName).getFile();
        return Files.lines(new File(absFileName).toPath(), StandardCharsets.UTF_8)
                .reduce((a, b) -> a + b).get();
    }

}
