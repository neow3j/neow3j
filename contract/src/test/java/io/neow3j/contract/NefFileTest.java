package io.neow3j.contract;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import io.neow3j.io.NeoSerializableInterface;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.utils.Numeric;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

import org.hamcrest.core.StringContains;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class NefFileTest {

    private static final String REFERENCE_SCRIPT_SIZE = "fd0e01";

    private static final String REFERENCE_SCRIPT =
            "570001782706000000400c0548656c6c6f0c05576f726c642150419bf667ce41e63f18844056010c14f66443498d3878d32b994e4e1283c6934421dafe604021354800000025260000000c114e6f20617574686f72697a6174696f6e2e213a2110c00c0764657374726f790c14abd6e8ad9f410941376591cddf9ad6820db797cd41627d5b524540210c14f66443498d3878d32b994e4e1283c6934421dafe2141f827ec8c400c0548656c6c6f21419bf667ce41925de831405700022135cbffffff251a0000000c114e6f20617574686f72697a6174696f6e2e213a7879215012c00c067570646174650c14abd6e8ad9f410941376591cddf9ad6820db797cd41627d5b524540213580ffffff40";
    private static final String REFERENCE_CHECKSUM = "98fee73e";
    private static final String REFERENCE_MAGIC = "3346454e";
    private final static String REFERENCE_COMPILER =
            "6e656f6e00000000000000000000000000000000000000000000000000000000"; // "neon"
    private final static String REFERENCE_VERSION =
            "332e302e302e3000000000000000000000000000000000000000000000000000"; // 3.0.0.0
    private final static String REFERENCE_NEF = Numeric.reverseHexString(REFERENCE_MAGIC)
            + REFERENCE_COMPILER
            + REFERENCE_VERSION
            + REFERENCE_SCRIPT_SIZE + REFERENCE_SCRIPT
            + REFERENCE_CHECKSUM;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void newNefFile() {
        byte[] script = Numeric.hexStringToByteArray(REFERENCE_SCRIPT);
        NefFile nef = new NefFile("neon", "3.0.0.0", script);
        assertThat(nef.getCompiler(), is("neon"));
        assertThat(nef.getScript(), is(script));
        assertThat(Numeric.toHexStringNoPrefix(nef.getCheckSum()), is(REFERENCE_CHECKSUM));
    }

    @Test
    public void failConstructorWithToLongCompilerName() {
        expectedException.expect(IllegalArgumentException.class);
        NefFile nef = new NefFile("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", // 33 bytes
                "3.0.0.0", Numeric.hexStringToByteArray(REFERENCE_SCRIPT));
    }

    @Test
    public void failConstructorWithToLongVersionString() {
        expectedException.expect(IllegalArgumentException.class);
        NefFile nef = new NefFile("neow3j", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", // 33 bytes
                Numeric.hexStringToByteArray(REFERENCE_SCRIPT));
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
    public void readFromFileProducesCorrectChecksum() throws URISyntaxException,
            DeserializationException, IOException {

        File file = new File(Objects.requireNonNull(NefFileTest.class.getClassLoader()
                .getResource("contracts/DotnetContract.nef")).toURI());
        NefFile nef = NefFile.readFromFile(file);
        assertThat(Numeric.toHexStringNoPrefix(nef.getCheckSum()), is(REFERENCE_CHECKSUM));
    }

    @Test
    public void serializeNewNefFile() {
        NefFile nef = new NefFile("neon", "3.0.0.0", Numeric.hexStringToByteArray(REFERENCE_SCRIPT));
        assertThat(nef.toArray(), is(Numeric.hexStringToByteArray(REFERENCE_NEF)));
    }

    @Test
    public void deserializeAndSerialize() throws DeserializationException {
        byte[] nefBytes = Numeric.hexStringToByteArray(REFERENCE_NEF);
        NefFile nef = NeoSerializableInterface.from(nefBytes, NefFile.class);
        assertThat(nef.getVersion(), is("3.0.0.0"));
        assertThat(nef.getCompiler(), is("neon"));
        assertThat(nef.getScript(), is(Numeric.hexStringToByteArray(REFERENCE_SCRIPT)));
        assertThat(Numeric.toHexStringNoPrefix(nef.getCheckSum()), is(REFERENCE_CHECKSUM));
        // serialize
        assertThat(nef.toArray(), is(nefBytes));
    }

    @Test
    public void deserializeWithWrongMagicNumber() throws DeserializationException {
        String nef = ""
                + "00000000"
                + REFERENCE_COMPILER
                + REFERENCE_VERSION
                + REFERENCE_SCRIPT_SIZE + REFERENCE_SCRIPT
                + REFERENCE_CHECKSUM;
        byte[] nefBytes = Numeric.hexStringToByteArray(nef);
        expectedException.expect(DeserializationException.class);
        expectedException.expectMessage(new StringContains("magic"));
        NeoSerializableInterface.from(nefBytes, NefFile.class);
    }

    @Test
    public void deserializeWithWrongCheckSum() throws DeserializationException {
        String nef = ""
                + Numeric.reverseHexString(REFERENCE_MAGIC)
                + REFERENCE_COMPILER
                + REFERENCE_VERSION
                + REFERENCE_SCRIPT_SIZE + REFERENCE_SCRIPT
                + "00000000";
        byte[] nefBytes = Numeric.hexStringToByteArray(nef);
        expectedException.expect(DeserializationException.class);
        expectedException.expectMessage(new StringContains("checksum"));
        NeoSerializableInterface.from(nefBytes, NefFile.class);
    }

    @Test
    public void deserializeWithEmptyScript() throws DeserializationException {
        String nef = ""
                + Numeric.reverseHexString(REFERENCE_MAGIC)
                + REFERENCE_COMPILER
                + REFERENCE_VERSION
                + "00"
                + REFERENCE_CHECKSUM;
        byte[] nefBytes = Numeric.hexStringToByteArray(nef);
        expectedException.expect(DeserializationException.class);
        expectedException.expectMessage(new StringContains("Script can't be empty"));
        NeoSerializableInterface.from(nefBytes, NefFile.class);
    }

    @Test
    public void getSize() throws DeserializationException {
        byte[] nefBytes = Numeric.hexStringToByteArray(REFERENCE_NEF);
        NefFile nef = NeoSerializableInterface.from(nefBytes, NefFile.class);
        assertThat(nef.getSize(), is(345));
    }
}
