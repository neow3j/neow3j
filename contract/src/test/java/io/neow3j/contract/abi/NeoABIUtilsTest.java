package io.neow3j.contract.abi;

import io.neow3j.contract.abi.exceptions.NEP3ParsingException;
import io.neow3j.contract.abi.model.NeoContractEvent;
import io.neow3j.contract.abi.model.NeoContractFunction;
import io.neow3j.contract.abi.model.NeoContractInterface;
import java.util.Arrays;
import org.bouncycastle.util.Strings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class NeoABIUtilsTest {

    private static final String TEST1_SMARTCONTRACT_ABI_FILENAME = "/test1-smartcontract.abi.json";
    private static final String TEST2_SMARTCONTRACT_ABI_FILENAME = "/test2-smartcontract.abi.json";
    private static final String TEST3_SMARTCONTRACT_ABI_FILENAME = "/test3-smartcontract.abi.json";
    private static final String TEST4_SMARTCONTRACT_ABI_FILENAME = "/test4-smartcontract.abi.json";
    private static final String TEST5_SMARTCONTRACT_ABI_FILENAME = "/test5-smartcontract.abi.json";

    private File tempDir;

    @Before
    public void setUp() throws Exception {
        tempDir = createTempDir();
    }

    @After
    public void tearDown() {
        for (File file : tempDir.listFiles()) {
            file.delete();
        }
        tempDir.delete();
    }

    @Test
    public void testLoadCredentialsFromFile_SmartContract1() throws Exception {
        NeoContractInterface neoContractABI = NeoABIUtils.loadABIFile(
                NeoABIUtilsTest.class.getResource(
                        TEST1_SMARTCONTRACT_ABI_FILENAME
                ).getFile());

        assertThat(neoContractABI, notNullValue());
        assertThat(neoContractABI.getHash(), is("0x5944fc67643207920ec129d13181297fed10350c"));
        assertThat(neoContractABI.getEntryPoint(), is("Main"));
        assertThat(neoContractABI.getFunctions(), hasSize(3));
        assertThat(neoContractABI.getEvents(), emptyCollectionOf(NeoContractEvent.class));
    }

    @Test
    public void testLoadCredentialsFromFile_SmartContract2() throws Exception {
        NeoContractInterface neoContractABI = NeoABIUtils.loadABIFile(
                NeoABIUtilsTest.class.getResource(
                        TEST2_SMARTCONTRACT_ABI_FILENAME
                ).getFile());

        assertThat(neoContractABI, notNullValue());
        assertThat(neoContractABI.getHash(), is("0x5944fc67643207920ec129d13181297fed10350c"));
        assertThat(neoContractABI.getEntryPoint(), is("Main"));
        assertThat(neoContractABI.getFunctions(), hasSize(3));
        assertThat(neoContractABI.getEvents(), hasSize(2));
    }

    @Test(expected = NEP3ParsingException.class)
    public void testLoadCredentialsFromFile_SmartContract3() throws Exception {
        NeoABIUtils.loadABIFile(
                NeoABIUtilsTest.class.getResource(
                        TEST3_SMARTCONTRACT_ABI_FILENAME
                ).getFile());
    }

    @Test
    public void testLoadCredentialsFromFile_SmartContract4() throws Exception {
        NeoContractInterface neoContractABI = NeoABIUtils.loadABIFile(
                NeoABIUtilsTest.class.getResource(
                        TEST4_SMARTCONTRACT_ABI_FILENAME
                ).getFile());

        assertThat(neoContractABI, notNullValue());
        assertThat(neoContractABI.getHash(), is("0x5944fc67643207920ec129d13181297fed10350c"));
        assertThat(neoContractABI.getEntryPoint(), nullValue());
    }

    @Test
    public void testLoadCredentialsFromStream_SmartContract4() throws Exception {
        NeoContractInterface neoContractABI = NeoABIUtils.loadABIFile(
                NeoABIUtilsTest.class.getResourceAsStream(
                        TEST4_SMARTCONTRACT_ABI_FILENAME)
        );

        assertThat(neoContractABI, notNullValue());
        assertThat(neoContractABI.getHash(), is("0x5944fc67643207920ec129d13181297fed10350c"));
        assertThat(neoContractABI.getEntryPoint(), nullValue());
    }

    @Test
    public void testLoadCredentialsFromStream_SmartContract5() throws Exception {
        NeoContractInterface neoContractABI = NeoABIUtils.loadABIFile(
            NeoABIUtilsTest.class.getResourceAsStream(
                TEST5_SMARTCONTRACT_ABI_FILENAME)
        );

        assertThat(neoContractABI, notNullValue());
        assertThat(neoContractABI.getHash(), is("0x5944fc67643207920ec129d13181297fed10350c"));
        assertThat(neoContractABI.getEntryPoint(), is("Main"));
        assertThat(
            neoContractABI.getFunctions().stream()
                .filter(f -> f.getName().equals(neoContractABI.getEntryPoint()))
                .findFirst()
                .get(),
            is(new NeoContractFunction("Main", Arrays.asList(), null)));
    }

    @Test
    public void testGenerateNeoContractABIFile() throws Exception {
        // load test smartcontract ABI 2
        NeoContractInterface neoContractABI = NeoABIUtils.loadABIFile(
                NeoABIUtilsTest.class.getResource(
                        TEST2_SMARTCONTRACT_ABI_FILENAME
                ).getFile());
        // generate the file based on the NeoContractInterface class
        String fileName = NeoABIUtils.generateNeoContractInterface(neoContractABI, tempDir);

        assertThat(getResultFileContent(fileName), is(expectedTest2ABI()));
        assertThat(getResultFileContent(fileName), is(getTestFileContent(TEST2_SMARTCONTRACT_ABI_FILENAME)));
    }

    private static File createTempDir() throws Exception {
        File file = Files.createTempDirectory(
                NeoABIUtilsTest.class.getSimpleName() + "-abifiles").toFile();
        file.deleteOnExit();
        return file;
    }

    private String getTestFileContent(String testFile) throws IOException, URISyntaxException {
        return trimWhiteSpaces(
                Strings.fromByteArray(
                        Files.readAllBytes(Paths.get(NeoABIUtilsTest.class.getResource(testFile).toURI()))));
    }

    private String getResultFileContent(String fileName) throws IOException {
        return trimWhiteSpaces(
                Strings.fromByteArray(
                        Files.readAllBytes(Paths.get(tempDir.getAbsolutePath(), fileName))));
    }

    private String trimWhiteSpaces(String s) {
        return s.trim()
                .replaceAll("\n", "")
                .replaceAll("\\s+", "");
    }

    private File getResultFile(String fileName) {
        return Paths.get(tempDir.getAbsolutePath(), fileName).toFile();
    }

    private String expectedTest2ABI() {
        return trimWhiteSpaces("{\n" +
                "  \"hash\" : \"0x5944fc67643207920ec129d13181297fed10350c\",\n" +
                "  \"entrypoint\" : \"Main\",\n" +
                "  \"functions\" : [\n" +
                "    {\n" +
                "      \"name\" : \"Name\",\n" +
                "      \"parameters\" : [\n" +
                "        {\n" +
                "          \"name\" : \"nameParam1\",\n" +
                "          \"type\" : \"Integer\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"returntype\" : \"Array\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\" : \"Description\",\n" +
                "      \"parameters\" : [\n" +
                "        {\n" +
                "          \"name\" : \"descriptionParam1\",\n" +
                "          \"type\" : \"String\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"returntype\" : \"String\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\" : \"Main\",\n" +
                "      \"parameters\" : [\n" +
                "        {\n" +
                "          \"name\" : \"operation\",\n" +
                "          \"type\" : \"String\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\" : \"args\",\n" +
                "          \"type\" : \"Array\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"returntype\" : \"ByteArray\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"events\" : [\n" +
                "    {\n" +
                "      \"name\" : \"event1\",\n" +
                "      \"parameters\" : [\n" +
                "        {\n" +
                "          \"name\" : \"event1Param1\",\n" +
                "          \"type\" : \"String\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\" : \"event1Param2\",\n" +
                "          \"type\" : \"Array\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\" : \"event2\",\n" +
                "      \"parameters\" : [\n" +
                "        {\n" +
                "          \"name\" : \"event2Param1\",\n" +
                "          \"type\" : \"Integer\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\" : \"event2Param2\",\n" +
                "          \"type\" : \"Array\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}");
    }

}
