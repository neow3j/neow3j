package io.neow3j.contract;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import io.neow3j.contract.NefFile.MethodToken;
import io.neow3j.io.NeoSerializableInterface;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.model.types.CallFlags;
import io.neow3j.protocol.core.methods.response.ByteStringStackItem;
import io.neow3j.utils.Numeric;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import org.hamcrest.core.StringContains;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class NefFileTest {

    // Same for both test contracts:
    //  0x3346454e;
    private static final String MAGIC = Numeric.reverseHexString("3346454e");
    private final static String TESTCONTRACT_COMPILER = "neon-3.0.0.0";
    private final static String TESTCONTRACT_COMPILER_HEX =
            "6e656f77336a2d332e302e3000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
    private final static String RESERVED_BYTES = "0000";

    // Test contract without method tokens:
    private final static String TESTCONTRACT_FILE = "contracts/TestContract.nef"; // no tokens
    private static final String TESTCONTRACT_SCRIPT_SIZE = "05";
    private static final String TESTCONTRACT_SCRIPT = "5700017840";
    private static final String TESTCONTRACT_CHECKSUM = "760f39a0";
    private final static String TESTCONTRACT_NEF = MAGIC
                                                   + TESTCONTRACT_COMPILER
                                                   + RESERVED_BYTES
                                                   + "00" // no method tokens
                                                   + RESERVED_BYTES
                                                   + TESTCONTRACT_SCRIPT_SIZE + TESTCONTRACT_SCRIPT
                                                   + TESTCONTRACT_CHECKSUM;

    // Test contract with method tokens:
    private final static String TESTCONTRACT_WITH_TOKENS_FILE =
            "contracts/TestContractWithMethodTokens.nef";
    private static final String TESTCONTRACT_WITH_TOKENS_SCRIPT = "213701004021370000405700017840";
    private final static List<MethodToken> TESTCONTRACT_METHOD_TOKENS = asList(
            new MethodToken(NeoToken.SCRIPT_HASH, "getGasPerBlock", 0, true, CallFlags.ALL),
            new MethodToken(GasToken.SCRIPT_HASH, "totalSupply", 0, true, CallFlags.ALL));
    private static final String TESTCONTRACT_WITH_TOKENS_CHECKSUM = "b559a069";


    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void newNefFile() {
        byte[] script = Numeric.hexStringToByteArray(TESTCONTRACT_SCRIPT);
        NefFile nef = new NefFile(TESTCONTRACT_COMPILER, script, null);
        assertThat(nef.getCompiler(), is(TESTCONTRACT_COMPILER));
        assertThat(nef.getScript(), is(script));
        assertThat(nef.getMethodTokens(), is(empty()));
        assertThat(Numeric.toHexStringNoPrefix(nef.getCheckSum()), is(TESTCONTRACT_CHECKSUM));
    }

    @Test
    public void newNefFileWithMethodTokens() {
        byte[] script = Numeric.hexStringToByteArray(TESTCONTRACT_WITH_TOKENS_SCRIPT);
        NefFile nef = new NefFile(TESTCONTRACT_COMPILER, script, TESTCONTRACT_METHOD_TOKENS);
        assertThat(nef.getCompiler(), is(TESTCONTRACT_COMPILER));
        assertThat(nef.getScript(), is(script));
        assertThat(nef.getMethodTokens(),
                containsInAnyOrder(TESTCONTRACT_METHOD_TOKENS.toArray(new MethodToken[]{})));
        assertThat(Numeric.toHexStringNoPrefix(nef.getCheckSum()),
                is(TESTCONTRACT_WITH_TOKENS_CHECKSUM));
    }

    @Test
    public void failConstructorWithToLongCompilerName() {
        expectedException.expect(IllegalArgumentException.class);
        NefFile nef = new NefFile(
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", // 65 bytes
                Numeric.hexStringToByteArray(TESTCONTRACT_SCRIPT),
                null);
    }

    @Test
    public void readFromFileShouldProduceCorrectNefFileWhenReadingValidFile()
            throws URISyntaxException,
            DeserializationException, IOException {

        File file = new File(Objects.requireNonNull(NefFileTest.class.getClassLoader()
                .getResource(TESTCONTRACT_FILE)).toURI());
        NefFile nef = NefFile.readFromFile(file);
        assertThat(Numeric.toHexStringNoPrefix(nef.getCheckSum()), is(TESTCONTRACT_CHECKSUM));
        assertThat(nef.getScript(), is(Numeric.hexStringToByteArray(TESTCONTRACT_SCRIPT)));
    }

    @Test
    public void readFromFileThatIsTooLarge() throws URISyntaxException, DeserializationException,
            IOException {
        File file = new File(Objects.requireNonNull(NefFileTest.class.getClassLoader()
                .getResource("contracts/too_large.nef")).toURI());
        expectedException.expect(IllegalArgumentException.class);
        NefFile.readFromFile(file);
    }

    @Test
    public void deserializeAndSerialize_ContractWithMethodTokens()
            throws DeserializationException, IOException, URISyntaxException {

        byte[] nefBytes = Files.readAllBytes(Paths.get(NefFileTest.class.getClassLoader()
                .getResource(TESTCONTRACT_WITH_TOKENS_FILE).toURI()));

        // deserialize
        NefFile nef = NeoSerializableInterface.from(nefBytes, NefFile.class);
        assertThat(nef.getCompiler(), is(TESTCONTRACT_COMPILER));
        assertThat(nef.getScript(),
                is(Numeric.hexStringToByteArray(TESTCONTRACT_WITH_TOKENS_SCRIPT)));
        assertThat(nef.getMethodTokens(),
                containsInAnyOrder(TESTCONTRACT_METHOD_TOKENS.toArray(new MethodToken[]{})));
        assertThat(Numeric.toHexStringNoPrefix(nef.getCheckSum()),
                is(TESTCONTRACT_WITH_TOKENS_CHECKSUM));

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
        assertThat(nef.getScript(),
                is(Numeric.hexStringToByteArray(TESTCONTRACT_SCRIPT)));
        assertThat(nef.getMethodTokens(), is(empty()));
        assertThat(Numeric.toHexStringNoPrefix(nef.getCheckSum()),
                is(TESTCONTRACT_CHECKSUM));

        // serialize
        assertThat(nef.toArray(), is(nefBytes));
    }

    @Test
    public void deserializeWithWrongMagicNumber() throws DeserializationException {
        String nef = ""
                     + "00000000"
                     + TESTCONTRACT_COMPILER_HEX
                     + RESERVED_BYTES
                     + "00" // no tokens
                     + RESERVED_BYTES
                     + TESTCONTRACT_SCRIPT_SIZE + TESTCONTRACT_SCRIPT
                     + TESTCONTRACT_CHECKSUM;
        byte[] nefBytes = Numeric.hexStringToByteArray(nef);
        expectedException.expect(DeserializationException.class);
        expectedException.expectMessage(new StringContains("magic"));
        NeoSerializableInterface.from(nefBytes, NefFile.class);
    }

    @Test
    public void deserializeWithWrongCheckSum() throws DeserializationException {
        String nef = MAGIC
                     + TESTCONTRACT_COMPILER_HEX
                     + RESERVED_BYTES
                     + "00" // no tokens
                     + RESERVED_BYTES
                     + TESTCONTRACT_SCRIPT_SIZE + TESTCONTRACT_SCRIPT
                     + "00000000";
        byte[] nefBytes = Numeric.hexStringToByteArray(nef);
        expectedException.expect(DeserializationException.class);
        expectedException.expectMessage(new StringContains("checksum"));
        NeoSerializableInterface.from(nefBytes, NefFile.class);
    }

    @Test
    public void deserializeWithEmptyScript() throws DeserializationException {
        String nef = MAGIC
                     + TESTCONTRACT_COMPILER_HEX
                     + RESERVED_BYTES
                     + "00" //no tokens
                     + RESERVED_BYTES
                     + "00" // empty script
                     + TESTCONTRACT_CHECKSUM;
        byte[] nefBytes = Numeric.hexStringToByteArray(nef);
        expectedException.expect(DeserializationException.class);
        expectedException.expectMessage(new StringContains("Script can't be empty"));
        NeoSerializableInterface.from(nefBytes, NefFile.class);
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
        byte[] nefBytes = Numeric.hexStringToByteArray(
                "4e4546336e656f2d636f72652d76332e3000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000700fd411af77b6771cbbae9");
        NefFile nef = NeoSerializableInterface.from(nefBytes, NefFile.class);
        assertThat(nef.getCompiler(), is("neo-core-v3.0"));
        assertThat(nef.getScript(), is(Numeric.hexStringToByteArray("00fd411af77b67")));
        assertThat(nef.getMethodTokens(), is(empty()));
        assertThat(nef.getCheckSumAsInteger(), is(3921333105L));
    }

    @Test
    public void readNeoNefFileFromStackItem() throws DeserializationException, IOException {
        byte[] nefBytes = Numeric.hexStringToByteArray(
                "4e4546336e656f2d636f72652d76332e3000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000700fd411af77b6771cbbae9");
        ByteStringStackItem stackItem = new ByteStringStackItem(nefBytes);
        NefFile nef = NefFile.readFromStackitem(stackItem);
        assertThat(nef.getCompiler(), is("neo-core-v3.0"));
        assertThat(nef.getScript(), is(Numeric.hexStringToByteArray("00fd411af77b67")));
        assertThat(nef.getMethodTokens(), is(empty()));
        assertThat(nef.getCheckSumAsInteger(), is(3921333105L));
    }

}
