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
import org.hamcrest.core.StringContains;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class NefFileTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void newNefFile() {
        byte[] script = Numeric.hexStringToByteArray(
                "5700020c0548656c6c6f0c05576f726c642150419bf667ce41e63f18841140");
        NefFile nef = new NefFile("neon", new Version(3, 0, 0, 0), script);
        assertThat(nef.getCompiler(), is("neon"));
        assertThat(nef.getScriptHash().toString(), is("b1872e12d6151da6312d0ff6617df37a98a48591"));
        assertThat(nef.getScript(), is(Numeric.hexStringToByteArray(
                "5700020c0548656c6c6f0c05576f726c642150419bf667ce41e63f18841140")));
        assertThat(Numeric.toHexStringNoPrefix(nef.getCheckSum()), is("7898d2a8"));
    }

    @Test
    public void readFromFileThatIsTooLarge() throws URISyntaxException, DeserializationException,
            IOException {
        File file = new File(NefFileTest.class.getClassLoader()
                .getResource("contracts/too_large.nef").toURI());
        expectedException.expect(IllegalArgumentException.class);
        NefFile.readFromFile(file);
    }

    @Test
    public void readFromFile() throws URISyntaxException, DeserializationException,
            IOException {
        File file = new File(NefFileTest.class.getClassLoader()
                .getResource("contracts/hello_world.nef").toURI());
        NefFile nef = NefFile.readFromFile(file);
        assertThat(Numeric.toHexStringNoPrefix(nef.getCheckSum()), is("7898d2a8"));
    }

    @Test
    public void deserializeAndSerialize() throws DeserializationException {
        byte[] nefBytes = Numeric.hexStringToByteArray(
                "4e4546336e656f6e00000000000000000000000000000000000000000000000000000000030000000000000000000000000000009185a4987af37d61f60f2d31a61d15d6122e87b17898d2a81f5700020c0548656c6c6f0c05576f726c642150419bf667ce41e63f18841140");
        NefFile nef = NeoSerializableInterface.from(nefBytes, NefFile.class);
        assertThat(nef.getVersion(), is(new NefFile.Version(3,0,0,0)));
        assertThat(nef.getCompiler(), is("neon"));
        assertThat(nef.getScriptHash().toString(), is("b1872e12d6151da6312d0ff6617df37a98a48591"));
        assertThat(nef.getScript(), is(Numeric.hexStringToByteArray(
                "5700020c0548656c6c6f0c05576f726c642150419bf667ce41e63f18841140")));
        assertThat(Numeric.toHexStringNoPrefix(nef.getCheckSum()), is("7898d2a8"));
        // serialize
        assertThat(nef.toArray(), is(nefBytes));
    }

    @Test
    public void deserializeWithWrongMagicNumber() throws DeserializationException {
        byte[] nefBytes = Numeric.hexStringToByteArray(
                "5e4546336e656f6e00000000000000000000000000000000000000000000000000000000030000000000000000000000000000009185a4987af37d61f60f2d31a61d15d6122e87b17898d2a81f5700020c0548656c6c6f0c05576f726c642150419bf667ce41e63f18841140");
        expectedException.expect(DeserializationException.class);
        expectedException.expectMessage(new StringContains("magic"));
        NeoSerializableInterface.from(nefBytes, NefFile.class);
    }

    @Test
    public void deserializeWithWrongCheckSum() throws DeserializationException {
        byte[] nefBytes = Numeric.hexStringToByteArray(
                "4e4546336e656f6e00000000000000000000000000000000000000000000000000000000030000000000000000000000000000009185a4987af37d61f60f2d31a61d15d6122e87b17899d2a81f5700020c0548656c6c6f0c05576f726c642150419bf667ce41e63f18841140");
        expectedException.expect(DeserializationException.class);
        expectedException.expectMessage(new StringContains("checksum"));
        NeoSerializableInterface.from(nefBytes, NefFile.class);
    }

    @Test
    public void getSize() throws DeserializationException {
        byte[] nefBytes = Numeric.hexStringToByteArray(
                "4e4546336e656f6e00000000000000000000000000000000000000000000000000000000030000000000000000000000000000009185a4987af37d61f60f2d31a61d15d6122e87b17898d2a81f5700020c0548656c6c6f0c05576f726c642150419bf667ce41e63f18841140");
        NefFile nef = NeoSerializableInterface.from(nefBytes, NefFile.class);
        assertThat(nef.getSize(), is(108));
    }

    @Test
    public void getVersionSize() {
        assertThat(new NefFile.Version().getSize(), is(4 * 32));
    }

}