package io.neow3j.contract;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import io.neow3j.contract.NefFile.Version;
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

    private static final String referenceScript =
            "5700017810b6250c000000215f0078ce40215f0078ce6701215f01402156032140";
    private static final String referenceChecksum = "9dd69a48";
    private static final String referenceMagic = "3346454e";
    private final static String referenceCompiler =
            "6e656f6e00000000000000000000000000000000000000000000000000000000"; // "neon"
    private final static String referenceVersion = "03000000000000000000000000000000"; // 3.0.0.0
    private final static String referenceScriptHash = "6819bfcaf19bf96a837b6005673ce67d924b4226";
    private final static String referenceNef = Numeric.reverseHexString(referenceMagic)
            + referenceCompiler
            + referenceVersion
            + Numeric.reverseHexString(referenceScriptHash)
            + referenceChecksum
            + "21" + referenceScript;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void newNefFile() {
        byte[] script = Numeric.hexStringToByteArray(referenceScript);
        NefFile nef = new NefFile("neon", new Version(3, 0, 0, 0), script);
        assertThat(nef.getCompiler(), is("neon"));
        assertThat(nef.getScriptHash().toString(), is(referenceScriptHash));
        assertThat(nef.getScript(), is(script));
        assertThat(nef.getVersion().getMajor(), is(3));
        assertThat(nef.getVersion().getMinor(), is(0));
        assertThat(nef.getVersion().getBuild(), is(0));
        assertThat(nef.getVersion().getRevision(), is(0));
        assertThat(Numeric.toHexStringNoPrefix(nef.getCheckSum()), is(referenceChecksum));
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
                .getResource("contracts/test.nef")).toURI());
        NefFile nef = NefFile.readFromFile(file);
        assertThat(Numeric.toHexStringNoPrefix(nef.getCheckSum()), is(referenceChecksum));
    }

    @Test
    public void deserializeAndSerialize() throws DeserializationException {
        byte[] nefBytes = Numeric.hexStringToByteArray(referenceNef);
        NefFile nef = NeoSerializableInterface.from(nefBytes, NefFile.class);
        assertThat(nef.getVersion(), is(new NefFile.Version(3, 0, 0, 0)));
        assertThat(nef.getCompiler(), is("neon"));
        assertThat(nef.getScriptHash().toString(), is(referenceScriptHash));
        assertThat(nef.getScript(), is(Numeric.hexStringToByteArray(referenceScript)));
        assertThat(Numeric.toHexStringNoPrefix(nef.getCheckSum()), is(referenceChecksum));
        // serialize
        assertThat(nef.toArray(), is(nefBytes));
    }

    @Test
    public void deserializeWithWrongMagicNumber() throws DeserializationException {
        String nef =  ""
                + "00000000"
                + referenceCompiler
                + referenceVersion
                + Numeric.reverseHexString(referenceScriptHash)
                + referenceChecksum
                + "21" + referenceScript;
        byte[] nefBytes = Numeric.hexStringToByteArray(nef);
        expectedException.expect(DeserializationException.class);
        expectedException.expectMessage(new StringContains("magic"));
        NeoSerializableInterface.from(nefBytes, NefFile.class);
    }

    @Test
    public void deserializeWithWrongCheckSum() throws DeserializationException {
        String nef =  ""
                + Numeric.reverseHexString(referenceMagic)
                + referenceCompiler
                + referenceVersion
                + Numeric.reverseHexString(referenceScriptHash)
                + "00000000"
                + "21" + referenceScript;
        byte[] nefBytes = Numeric.hexStringToByteArray(nef);
        expectedException.expect(DeserializationException.class);
        expectedException.expectMessage(new StringContains("checksum"));
        NeoSerializableInterface.from(nefBytes, NefFile.class);
    }

    @Test
    public void getSize() throws DeserializationException {
        byte[] nefBytes = Numeric.hexStringToByteArray(referenceNef);
        NefFile nef = NeoSerializableInterface.from(nefBytes, NefFile.class);
        assertThat(nef.getSize(), is(110));
    }

    @Test
    public void getVersionSize() {
        assertThat(new NefFile.Version().getSize(), is(4 * 32));
    }

}