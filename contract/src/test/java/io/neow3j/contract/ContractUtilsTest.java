package io.neow3j.contract;

import io.neow3j.serialization.exceptions.DeserializationException;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.ContractParameterType;
import io.neow3j.protocol.core.response.ContractManifest;
import io.neow3j.protocol.core.response.ContractManifest.ContractABI;
import io.neow3j.protocol.core.response.ContractManifest.ContractABI.ContractEvent;
import io.neow3j.protocol.core.response.ContractManifest.ContractABI.ContractMethod;
import io.neow3j.protocol.core.response.ContractManifest.ContractGroup;
import io.neow3j.protocol.core.response.ContractManifest.ContractPermission;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import static io.neow3j.contract.ContractUtils.getContractManifestFilename;
import static io.neow3j.contract.ContractUtils.loadContractManifestFile;
import static io.neow3j.contract.ContractUtils.writeContractManifestFile;
import static io.neow3j.contract.ContractUtils.writeNefFile;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContractUtilsTest {

    private final static String TESTCONTRACT_WITH_TOKENS_FILE = "/contracts/TestContractWithMethodTokens.nef";

    @Test
    public void testWriteContractManifestFile() throws Exception {
        File tempDir = Files.createTempDirectory(
                ContractUtils.class.getSimpleName() + "-test-generate-manifest").toFile();
        tempDir.deleteOnExit();

        ContractManifest cm = buildContractManifest();

        writeContractManifestFile(cm, tempDir.toPath());

        File expectedOutputFile = Paths.get(tempDir.getAbsolutePath(),
                        "neowww" + "." + ContractUtils.MANIFEST_FILENAME_SUFFIX)
                .toFile();

        assertTrue(expectedOutputFile.exists());
        assertThat(expectedOutputFile.length(), greaterThan(0L));

        ContractManifest cmLoaded = loadContractManifestFile(
                expectedOutputFile.getAbsolutePath());

        assertThat(cmLoaded.getName(), is("neowww"));
        assertThat(cmLoaded.getExtra(), not(nullValue()));
        assertThat(((HashMap) cmLoaded.getExtra()).get("test-bool"), is(true));
        assertThat(((HashMap) cmLoaded.getExtra()).get("test-int"), is(1));
    }

    @Test
    public void testGetContractManifestFilenameHappyPath() {
        HashMap<String, String> extras = new HashMap<>();
        ContractManifest m = new ContractManifest("neowww", null, null, null, null, null, null,
                extras);
        String result = getContractManifestFilename(m);
        assertThat(result, is("neowww" + "." + ContractUtils.MANIFEST_FILENAME_SUFFIX));
    }

    @Test
    public void testGetContractManifestFilenameNoManifestName() {
        HashMap<String, String> extras = new HashMap<>();
        ContractManifest m = new ContractManifest(null, null, null, null, null, null, null, extras);
        String result = getContractManifestFilename(m);
        assertThat(result, is(ContractUtils.MANIFEST_FILENAME_SUFFIX));
    }

    @Test
    public void testGetContractManifestFilenameEmptyManifestName() {
        HashMap<String, String> extras = new HashMap<>();
        ContractManifest m = new ContractManifest("", null, null, null, null, null, null, extras);
        String result = getContractManifestFilename(m);
        assertThat(result, is(ContractUtils.MANIFEST_FILENAME_SUFFIX));
    }

    @Test
    public void testReadContractManifestFile_empty() throws Exception {
        Path manifestFilePath = Files.createTempFile(ContractUtils.class.getSimpleName(),
                "-test-read-manifest");

        String manifestContent = "{\n"
                + "  \"name\":\"\",\n"
                + "  \"groups\": [],\n"
                + "  \"supportedstandards\": [],\n"
                + "  \"abi\": {},\n"
                + "  \"permissions\": [],\n"
                + "  \"trusts\": [], \n"
                + "  \"extra\": {}\n"
                + "}";

        FileUtils.writeStringToFile(manifestFilePath.toFile(), manifestContent, "UTF-8");

        ContractManifest manifestObj = loadContractManifestFile(manifestFilePath.toString());
        assertThat(manifestObj.getName(), is(""));
        assertThat(manifestObj.getGroups(), is(empty()));
        assertThat(manifestObj.getSupportedStandards(), is(empty()));
        assertThat(manifestObj.getAbi(), is(notNullValue()));
        assertThat(manifestObj.getPermissions(), is(empty()));
        assertThat(manifestObj.getTrusts(), is(empty()));
        assertThat(manifestObj.getExtra(), is(notNullValue()));
    }

    @Test
    public void testReadContractManifestFile_case1() throws Exception {
        Path manifestFilePath = Files.createTempFile(ContractUtils.class.getSimpleName(),
                "-test-read-manifest");

        String manifestContent = "{\n"
                + "  \"name\":\"\",\n"
                + "  \"groups\": [\n"
                + "       {\n"
                + "          \"pubKey\": \"03e237d84371612e3d2ce2a71b3c150ded51be3e93d34c494d1424bdae349900a9\",\n"
                + "          \"signature\": \"lzrUouvaXRl0IM7dhN3PaIUZ9LL9AMw7/1ZknI60BMlPXRW99l246N69F5MW3kAiXFyk0N4cte//Ajfu1ZZ2KQ==\"\n"
                + "       }\n"
                + "   ],\n"
                + "  \"supportedstandards\": [],\n"
                + "  \"abi\": {},\n"
                + "  \"permissions\": [\n"
                + "       {\n"
                + "          \"contract\": \"contract1\",\n"
                + "          \"methods\": \"*\"\n"
                + "       }\n"
                + "   ],\n"
                + "  \"trusts\": \"*\",\n"
                + "  \"extra\": {}\n"
                + "}";

        FileUtils.writeStringToFile(manifestFilePath.toFile(), manifestContent, "UTF-8");

        ContractManifest manifestObj = loadContractManifestFile(manifestFilePath.toString());
        assertThat(manifestObj.getName(), is(""));
        assertThat(manifestObj.getGroups(), is(not(empty())));
        assertThat(manifestObj.getGroups(), hasSize(1));
        assertThat(

                manifestObj.getGroups(),
                hasItem(new ContractGroup("03e237d84371612e3d2ce2a71b3c150ded51be3e93d34c494d1424bdae349900a9",
                        "lzrUouvaXRl0IM7dhN3PaIUZ9LL9AMw7/1ZknI60BMlPXRW99l246N69F5MW3kAiXFyk0N4cte//Ajfu1ZZ2KQ=="))
        );
        assertThat(manifestObj.getSupportedStandards(), is(empty()));
        assertThat(manifestObj.getAbi(), is(notNullValue()));
        assertThat(manifestObj.getPermissions(), is(not(empty())));
        assertThat(manifestObj.getPermissions(), hasSize(1));
        assertThat(
                manifestObj.getPermissions(),
                hasItem(
                        new ContractPermission(
                                "contract1",
                                singletonList("*")
                        )
                )
        );
        assertThat(manifestObj.getTrusts(), is(not(empty())));
        assertThat(manifestObj.getTrusts(), hasSize(1));
        assertThat(manifestObj.getTrusts(), hasItem("*"));
        assertThat(manifestObj.getExtra(), is(notNullValue()));
    }

    @Test
    public void testReadContractManifestFile_case2() throws Exception {
        Path manifestFilePath = Files.createTempFile(ContractUtils.class.getSimpleName(),
                "-test-read-manifest");

        String manifestContent = "{\n"
                + "  \"name\":\"\",\n"
                + "  \"groups\": [\n"
                + "       {\n"
                + "          \"pubkey\": \"03e237d84371612e3d2ce2a71b3c150ded51be3e93d34c494d1424bdae349900a9\",\n"
                + "          \"signature\": \"lzrUouvaXRl0IM7dhN3PaIUZ9LL9AMw7/1ZknI60BMlPXRW99l246N69F5MW3kAiXFyk0N4cte//Ajfu1ZZ2KQ==\"\n"
                + "       }\n"
                + "   ],\n"
                + "  \"supportedstandards\": [],\n"
                + "  \"abi\": {},\n"
                + "  \"permissions\": [\n"
                + "       {\n"
                + "          \"contract\": \"contract1\",\n"
                + "          \"methods\": [ \"*\", \"main\", \"test\" ]\n"
                + "       }\n"
                + "   ],\n"
                +
                "  \"trusts\": [ \"contract1\", \"contract2\", \"contract3\" ],\n"
                + "  \"extra\": {}\n"
                + "}";

        FileUtils.writeStringToFile(manifestFilePath.toFile(), manifestContent, "UTF-8");

        ContractManifest manifestObj = loadContractManifestFile(manifestFilePath.toString());
        assertThat(manifestObj.getName(), is(""));
        assertThat(manifestObj.getGroups(), is(not(empty())));
        assertThat(manifestObj.getGroups(), hasSize(1));
        assertThat(
                manifestObj.getGroups(),
                hasItem(new ContractGroup("03e237d84371612e3d2ce2a71b3c150ded51be3e93d34c494d1424bdae349900a9",
                        "lzrUouvaXRl0IM7dhN3PaIUZ9LL9AMw7/1ZknI60BMlPXRW99l246N69F5MW3kAiXFyk0N4cte//Ajfu1ZZ2KQ=="))
        );
        assertThat(manifestObj.getSupportedStandards(), is(empty()));
        assertThat(manifestObj.getAbi(), is(notNullValue()));
        assertThat(manifestObj.getPermissions(), is(not(empty())));
        assertThat(manifestObj.getPermissions(), hasSize(1));
        assertThat(
                manifestObj.getPermissions(),
                hasItem(
                        new ContractPermission(
                                "contract1",
                                asList("*", "main", "test")
                        )
                )
        );
        assertThat(manifestObj.getTrusts(), is(not(empty())));
        assertThat(manifestObj.getTrusts(), hasSize(3));
        assertThat(
                manifestObj.getTrusts(),
                hasItems("contract1", "contract2", "contract3")
        );
        assertThat(manifestObj.getExtra(), is(notNullValue()));
    }

    @Test
    public void testWriteNefFile() throws DeserializationException, IOException {
        String contractName = "DotnetContract";
        Path outDir = Files.createTempDirectory(ContractUtils.class.getSimpleName()
                + "-test-write-nef");
        outDir.toFile().deleteOnExit();
        NefFile nefFile = NefFile.readFromFile(new File(ContractUtilsTest.class.getResource(
                TESTCONTRACT_WITH_TOKENS_FILE).getFile()));

        String nefFilePath = writeNefFile(nefFile, contractName, outDir);
        assertThat(nefFilePath,
                is(Paths.get(outDir.toFile().getAbsolutePath(), contractName + ".nef").toString()));
        File writtenFile = new File(nefFilePath);
        assertTrue(writtenFile.exists());
        assertThat(writtenFile.length(), greaterThan(0L));

        NefFile rereadNefFile = NefFile.readFromFile(writtenFile);
        assertThat(nefFile.getCheckSum(), is(rereadNefFile.getCheckSum()));
    }

    private ContractManifest buildContractManifest() {
        ContractGroup cg1 = new ContractGroup("03e237d84371612e3d2ce2a71b3c150ded51be3e93d34c494d1424bdae349900a9", "lzrUouvaXRl0IM7dhN3PaIUZ9LL9AMw7/1ZknI60BMlPXRW99l246N69F5MW3kAiXFyk0N4cte//Ajfu1ZZ2KQ==");
        ContractGroup cg2 = new ContractGroup("0x025f3953adaf5155d9ee63ce40643837219286636fe28d6024c4b1d28f675a12e2", "tBscf3to/EMw/lLSM07Ko9WPeegYJds76LIcZusDXpwPbvCJUdtiLf+Cf5rF41WuDyUoC5mfOkUOrKHS1y+tWQ==");
        String name = "neowww";
        List<ContractGroup> cgs = asList(cg1, cg2);
        HashMap<Object, Object> features = new HashMap<>();
        features.put("test-feature1", false);
        features.put("test-feature2", "test");

        List<String> supportedStandards = asList("nothing", "blah");

        HashMap<String, Object> extras = new HashMap<>();
        extras.put("test-bool", true);
        extras.put("test-int", 1);

        List<ContractMethod> contractMethods = asList(
                new ContractMethod(
                        "main",
                        asList(
                                new ContractParameter("param1", ContractParameterType.BOOLEAN),
                                new ContractParameter("param2", ContractParameterType.BYTE_ARRAY)),
                        0,
                        ContractParameterType.STRING,
                        false
                ),
                new ContractMethod(
                        "deploy",
                        emptyList(),
                        100,
                        ContractParameterType.BOOLEAN,
                        false
                )
        );
        List<ContractEvent> contractEvents = asList(
                new ContractEvent(
                        "event1",
                        asList(
                                new ContractParameter("eventParam1", ContractParameterType.INTEGER),
                                new ContractParameter("eventParam2", ContractParameterType.BOOLEAN)
                        )
                ),
                new ContractEvent(
                        "event2",
                        asList(
                                new ContractParameter("eventParam1",
                                        ContractParameterType.BYTE_ARRAY),
                                new ContractParameter("eventParam2", ContractParameterType.HASH160)
                        )
                )
        );
        ContractABI abi = new ContractABI(
                contractMethods,
                contractEvents
        );

        List<ContractPermission> contractPermissions = asList(
                new ContractPermission("contract1", asList("test1", "test2", "test3")),
                new ContractPermission("contract2", singletonList("test1")),
                new ContractPermission("contract2", emptyList())
        );

        List<String> trusts = asList("trust1", "trust2");

        return new ContractManifest(
                name,
                cgs,
                features,
                supportedStandards,
                abi,
                contractPermissions,
                trusts,
                extras
        );
    }

    private ContractManifest buildContractManifestWithCornerCases() {
        ContractGroup cg1 = new ContractGroup("pubKey1", "sign1");
        ContractGroup cg2 = new ContractGroup("pubKey2", "sign2");
        String name = "neowww";
        List<ContractGroup> cgs = asList(cg1, cg2);
        List<String> supportedStandards = asList("nothing", "blah");

        List<ContractPermission> contractPermissions = asList(
                new ContractPermission("contract1", asList("test1", "test2", "test3")),
                new ContractPermission("contract2", singletonList("test1")),
                new ContractPermission("contract2", emptyList())
        );

        List<String> trusts = asList("trust1", "trust2");

        return new ContractManifest(
                name,
                cgs,
                null,
                supportedStandards,
                null,
                contractPermissions,
                trusts,
                null
        );
    }

}
