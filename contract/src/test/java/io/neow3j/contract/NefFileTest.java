package io.neow3j.contract;

import io.neow3j.contract.NefFile.MethodToken;
import io.neow3j.protocol.core.stackitem.ByteStringStackItem;
import io.neow3j.serialization.NeoSerializableInterface;
import io.neow3j.serialization.exceptions.DeserializationException;
import io.neow3j.types.CallFlags;
import io.neow3j.types.Hash160;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static io.neow3j.utils.Numeric.reverseHexString;
import static io.neow3j.utils.Numeric.toHexString;
import static io.neow3j.utils.Numeric.toHexStringNoPrefix;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NefFileTest {

    // Same for both test contracts:
    //  0x3346454e;
    private static final String MAGIC = reverseHexString("3346454e");
    private final static String TESTCONTRACT_COMPILER = "neon-3.0.0.0";
    private final static String TESTCONTRACT_COMPILER_HEX =
            "6e656f77336a2d332e302e3000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
    private final static String RESERVED_BYTES = "0000";

    // Test contract without method tokens:
    private final static String TESTCONTRACT_FILE = "contracts/TestContract.nef"; // no tokens
    private static final String TESTCONTRACT_SCRIPT_SIZE = "05";
    private static final String TESTCONTRACT_SCRIPT = "5700017840";
    private static final String TESTCONTRACT_CHECKSUM = "760f39a0";

    // Test contract with method tokens:
    private final static String TESTCONTRACT_WITH_TOKENS_FILE = "contracts/TestContractWithMethodTokens.nef";
    private static final String TESTCONTRACT_WITH_TOKENS_SCRIPT = "213701004021370000405700017840";
    private final static List<MethodToken> TESTCONTRACT_METHOD_TOKENS = asList(
            new MethodToken(new Hash160("f61eebf573ea36593fd43aa150c055ad7906ab83"), "getGasPerBlock", 0, true,
                    CallFlags.ALL),
            new MethodToken(new Hash160("70e2301955bf1e74cbb31d18c2f96972abadb328"), "totalSupply", 0, true,
                    CallFlags.ALL));
    private static final String TESTCONTRACT_WITH_TOKENS_CHECKSUM = "b559a069";

    @Test
    public void newNefFile() {
        byte[] script = hexStringToByteArray(TESTCONTRACT_SCRIPT);
        NefFile nef = new NefFile(TESTCONTRACT_COMPILER, null, script);

        assertThat(nef.getCompiler(), is(TESTCONTRACT_COMPILER));
        assertThat(nef.getScript(), is(script));
        assertThat(nef.getMethodTokens(), is(empty()));
        assertThat(toHexStringNoPrefix(nef.getCheckSum()), is(TESTCONTRACT_CHECKSUM));
    }

    @Test
    public void newNefFileWithMethodTokens() {
        byte[] script = hexStringToByteArray(TESTCONTRACT_WITH_TOKENS_SCRIPT);
        NefFile nef = new NefFile(TESTCONTRACT_COMPILER, TESTCONTRACT_METHOD_TOKENS, script);

        assertThat(nef.getCompiler(), is(TESTCONTRACT_COMPILER));
        assertThat(nef.getScript(), is(script));
        assertThat(nef.getMethodTokens(), containsInAnyOrder(TESTCONTRACT_METHOD_TOKENS.toArray(new MethodToken[]{})));
        assertThat(toHexStringNoPrefix(nef.getCheckSum()), is(TESTCONTRACT_WITH_TOKENS_CHECKSUM));
    }

    @Test
    public void failConstructorWithToLongCompilerName() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new NefFile("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", // 65 bytes
                        null, hexStringToByteArray(TESTCONTRACT_SCRIPT)));
        assertThat(thrown.getMessage(), containsString("The compiler name and version string can be max"));
    }

    @Test
    public void readFromFileShouldProduceCorrectNefFileWhenReadingValidFile()
            throws URISyntaxException, DeserializationException, IOException {

        File file = new File(Objects.requireNonNull(NefFileTest.class.getClassLoader()
                .getResource(TESTCONTRACT_FILE)).toURI());
        NefFile nef = NefFile.readFromFile(file);

        assertThat(toHexStringNoPrefix(nef.getCheckSum()), is(TESTCONTRACT_CHECKSUM));
        assertThat(nef.getScript(), is(hexStringToByteArray(TESTCONTRACT_SCRIPT)));
    }

    @Test
    public void readFromFileThatIsTooLarge() throws URISyntaxException {
        File file = new File(Objects.requireNonNull(NefFileTest.class.getClassLoader()
                .getResource("contracts/too_large.nef")).toURI());

        IllegalArgumentException thrown =
                assertThrows(IllegalArgumentException.class, () -> NefFile.readFromFile(file));
        assertThat(thrown.getMessage(), containsString("The given NEF file is too large."));
    }

    @Test
    public void deserializeAndSerialize_ContractWithMethodTokens()
            throws DeserializationException, IOException, URISyntaxException {

        byte[] nefBytes = Files.readAllBytes(Paths.get(NefFileTest.class.getClassLoader()
                .getResource(TESTCONTRACT_WITH_TOKENS_FILE).toURI()));

        // deserialize
        NefFile nef = NeoSerializableInterface.from(nefBytes, NefFile.class);

        assertThat(nef.getCompiler(), is(TESTCONTRACT_COMPILER));
        assertThat(nef.getScript(), is(hexStringToByteArray(TESTCONTRACT_WITH_TOKENS_SCRIPT)));
        assertThat(nef.getMethodTokens(), containsInAnyOrder(TESTCONTRACT_METHOD_TOKENS.toArray(new MethodToken[]{})));
        assertThat(toHexStringNoPrefix(nef.getCheckSum()), is(TESTCONTRACT_WITH_TOKENS_CHECKSUM));

        // serialize
        assertThat(nef.toArray(), is(nefBytes));
    }

    @Test
    public void deserializeAndSerialize_ContractWithoutMethodTokens()
            throws DeserializationException, IOException, URISyntaxException {

        byte[] nefBytes = Files.readAllBytes(Paths.get(NefFileTest.class.getClassLoader()
                .getResource(TESTCONTRACT_FILE).toURI()));

        // deserialize
        NefFile nef = NeoSerializableInterface.from(nefBytes, NefFile.class);

        assertThat(nef.getCompiler(), is(TESTCONTRACT_COMPILER));
        assertThat(nef.getScript(), is(hexStringToByteArray(TESTCONTRACT_SCRIPT)));
        assertThat(nef.getMethodTokens(), is(empty()));
        assertThat(toHexStringNoPrefix(nef.getCheckSum()), is(TESTCONTRACT_CHECKSUM));

        // serialize
        assertThat(nef.toArray(), is(nefBytes));
    }

    @Test
    public void deserializeWithWrongMagicNumber() {
        String nef = ""
                + "00000000"
                + TESTCONTRACT_COMPILER_HEX
                + RESERVED_BYTES
                + "00" // no tokens
                + RESERVED_BYTES
                + TESTCONTRACT_SCRIPT_SIZE + TESTCONTRACT_SCRIPT
                + TESTCONTRACT_CHECKSUM;
        byte[] nefBytes = hexStringToByteArray(nef);

        DeserializationException thrown = assertThrows(DeserializationException.class,
                () -> NeoSerializableInterface.from(nefBytes, NefFile.class));
        assertThat(thrown.getMessage(), is("Wrong magic number in NEF file."));
    }

    @Test
    public void deserializeWithWrongCheckSum() {
        String nef = MAGIC
                + TESTCONTRACT_COMPILER_HEX
                + RESERVED_BYTES
                + "00" // no tokens
                + RESERVED_BYTES
                + TESTCONTRACT_SCRIPT_SIZE + TESTCONTRACT_SCRIPT
                + "00000000";
        byte[] nefBytes = hexStringToByteArray(nef);

        DeserializationException thrown = assertThrows(DeserializationException.class,
                () -> NeoSerializableInterface.from(nefBytes, NefFile.class));
        assertThat(thrown.getMessage(), is("The checksums did not match."));
    }

    @Test
    public void deserializeWithEmptyScript() {
        String nef = MAGIC
                + TESTCONTRACT_COMPILER_HEX
                + RESERVED_BYTES
                + "00" //no tokens
                + RESERVED_BYTES
                + "00" // empty script
                + TESTCONTRACT_CHECKSUM;
        byte[] nefBytes = hexStringToByteArray(nef);

        DeserializationException thrown = assertThrows(DeserializationException.class,
                () -> NeoSerializableInterface.from(nefBytes, NefFile.class));
        assertThat(thrown.getMessage(), is("Script cannot be empty in NEF file."));
    }

    @Test
    public void getSize() throws DeserializationException, URISyntaxException, IOException {
        byte[] nefBytes = Files.readAllBytes(Paths.get(NefFileTest.class.getClassLoader()
                .getResource(TESTCONTRACT_FILE).toURI()));
        NefFile nef = NeoSerializableInterface.from(nefBytes, NefFile.class);

        assertThat(nef.getSize(), is(nefBytes.length));
    }

    @Test
    public void deserializeNeoTokenNefFile() throws DeserializationException {
        byte[] nefBytes = hexStringToByteArray(
                "4e4546336e656f2d636f72652d76332e3000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000700fd411af77b6771cbbae9");
        NefFile nef = NeoSerializableInterface.from(nefBytes, NefFile.class);

        assertThat(nef.getCompiler(), is("neo-core-v3.0"));
        assertThat(nef.getScript(), is(hexStringToByteArray("00fd411af77b67")));
        assertThat(nef.getMethodTokens(), is(empty()));
        assertThat(nef.getCheckSumAsInteger(), is(3921333105L));
    }

    @Test
    public void readNeoNefFileFromStackItem() throws DeserializationException, IOException {
        byte[] nefBytes = hexStringToByteArray(
                "4e4546336e656f2d636f72652d76332e3000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000700fd411af77b6771cbbae9");
        ByteStringStackItem stackItem = new ByteStringStackItem(nefBytes);
        NefFile nef = NefFile.readFromStackItem(stackItem);

        assertThat(nef.getCompiler(), is("neo-core-v3.0"));
        assertThat(nef.getScript(), is(hexStringToByteArray("00fd411af77b67")));
        assertThat(nef.getMethodTokens(), is(empty()));
        assertThat(nef.getCheckSumAsInteger(), is(3921333105L));
    }

    @Test
    public void serializeDeserializeNefFileWithSourceUrl() throws DeserializationException {
        String url = "github.com/neow3j/neow3j";
        NefFile nef = new NefFile("neo-core-v3.0", url, null, hexStringToByteArray("00fd411af77b67"));

        byte[] bytes = nef.toArray();
        String hexString = toHexString(bytes);
        // first number "18" is the length of the url.
        assertThat(hexString, containsString("186769746875622e636f6d2f6e656f77336a2f6e656f77336a"));

        NefFile nefDes = NeoSerializableInterface.from(bytes, NefFile.class);
        assertThat(nefDes.getSourceUrl(), is(url));
    }

    @Test
    public void failDeserializationWithTooLongSourceUrl() {
        String nefHex =
                // beginning of nef
                "4e4546336e656f2d636f72652d76332e30000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000" +
                        // size of the source url (256 bytes)
                        "fd0001" +
                        // the source url
                        "1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
                        "186769746875622e636f6d2f6e656f77336a2f6e656f77336a000000000700fd411af77b679cc8d824";

        DeserializationException thrown = assertThrows(DeserializationException.class,
                () -> NeoSerializableInterface.from(hexStringToByteArray(nefHex), NefFile.class));
        assertThat(thrown.getMessage(), containsString("Source URL must not be longer than"));
    }

    @Test
    public void failConstructingWithTooLongSourceUrl() {
        // 256 bytes string
        String url =
                "github.com/neow3j/neow3j/neow3j/neow3j/neow3j/neow3j/neow3j/neow3j/neow3j/neow3j" +
                        "/neow3j/neow3j/neow3j/neow3j/neow3j/neow3j/neow3j/neow3j/neow3j/neow3j" +
                        "/neow3j/neow3j/neow3j/neow3j/neow3j/neow3j/neow3j/neow3j/neow3j/neow3j" +
                        "/neow3j/neow3j/neow3j/neow3j/neow3j/";

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new NefFile("neo-core-v3.0", url, null, hexStringToByteArray("00fd411af77b67")));
        assertThat(thrown.getMessage(), containsString("The source URL must not be longer than"));
    }

}
