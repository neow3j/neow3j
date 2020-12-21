package io.neow3j.contract;

import static io.neow3j.contract.ContractUtils.generateContractManifestFile;
import static io.neow3j.contract.ContractUtils.getContractManifestFilename;
import static io.neow3j.contract.ContractUtils.loadContractManifestFile;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertTrue;

import io.neow3j.model.types.ContractParameterType;
import io.neow3j.protocol.core.methods.response.ContractManifest;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI.ContractEvent;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI.ContractMethod;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractGroup;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractPermission;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;

public class ContractUtilsTest {

    @Test
    public void testGenerateContractManifestFile() throws Exception {
        File tempDir = Files.createTempDirectory(
                ContractUtils.class.getSimpleName() + "-test-generate-manifest").toFile();
        tempDir.deleteOnExit();

        ContractManifest cm = buildContractManifest();

        generateContractManifestFile(cm, tempDir);

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
        ContractManifest m = new ContractManifest("neowww", null, null, null, null, null, extras);
        String result = getContractManifestFilename(m);
        assertThat(result, is("neowww" + "." + ContractUtils.MANIFEST_FILENAME_SUFFIX));
    }

    @Test
    public void testGetContractManifestFilenameNoManifestName() {
        HashMap<String, String> extras = new HashMap<>();
        ContractManifest m = new ContractManifest(null, null, null, null, null, null, extras);
        String result = getContractManifestFilename(m);
        assertThat(result, is(ContractUtils.MANIFEST_FILENAME_SUFFIX));
    }

    @Test
    public void testGetContractManifestFilenameEmptyManifestName() {
        HashMap<String, String> extras = new HashMap<>();
        ContractManifest m = new ContractManifest("", null, null, null, null, null, extras);
        String result = getContractManifestFilename(m);
        assertThat(result, is(ContractUtils.MANIFEST_FILENAME_SUFFIX));
    }

    private ContractManifest buildContractManifest() {
        ContractGroup cg1 = new ContractGroup("pubKey1", "sign1");
        ContractGroup cg2 = new ContractGroup("pubKey2", "sign2");
        String name = "neowww";
        List<ContractGroup> cgs = Arrays.asList(cg1, cg2);
        List<String> supportedStandards = Arrays.asList("nothing", "blah");

        HashMap<String, Object> extras = new HashMap<>();
        extras.put("test-bool", true);
        extras.put("test-int", 1);

        List<ContractMethod> contractMethods = Arrays.asList(
                new ContractMethod(
                        "main",
                        Arrays.asList(
                                new ContractParameter("param1", ContractParameterType.BOOLEAN,
                                        null),
                                new ContractParameter("param2", ContractParameterType.BYTE_ARRAY,
                                        null)
                        ),
                        0,
                        ContractParameterType.STRING,
                        false
                ),
                new ContractMethod(
                        "deploy",
                        Arrays.asList(),
                        100,
                        ContractParameterType.BOOLEAN,
                        false
                )
        );
        List<ContractEvent> contractEvents = Arrays.asList(
                new ContractEvent(
                        "event1",
                        Arrays.asList(
                                new ContractParameter("eventParam1", ContractParameterType.INTEGER),
                                new ContractParameter("eventParam2", ContractParameterType.BOOLEAN)
                        )
                ),
                new ContractEvent(
                        "event2",
                        Arrays.asList(
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

        List<ContractPermission> contractPermissions = Arrays.asList(
                new ContractPermission("contract1", Arrays.asList("test1", "test2", "test3")),
                new ContractPermission("contract2", Arrays.asList("test1")),
                new ContractPermission("contract2", Arrays.asList())
        );

        List<String> trusts = Arrays.asList("trust1", "trust2");

        return new ContractManifest(
                name,
                cgs,
                supportedStandards,
                abi,
                contractPermissions,
                trusts,
                extras
        );
    }
}
