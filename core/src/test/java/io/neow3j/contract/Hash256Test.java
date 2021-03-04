package io.neow3j.contract;

import static io.neow3j.utils.ArrayUtils.reverseArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import io.neow3j.io.BinaryWriter;
import io.neow3j.io.NeoSerializableInterface;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class Hash256Test {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void createFromValidHash() {
        Hash256 hash =
                new Hash256("0xb804a98220c69ab4674e97142beeeb00909113d417b9d6a67c12b71a3974a21a");
        assertThat(hash.toString(),
                is("b804a98220c69ab4674e97142beeeb00909113d417b9d6a67c12b71a3974a21a"));

        hash = new Hash256("b804a98220c69ab4674e97142beeeb00909113d417b9d6a67c12b71a3974a21a");
        assertThat(hash.toString(),
                is("b804a98220c69ab4674e97142beeeb00909113d417b9d6a67c12b71a3974a21a"));
    }

    @Test
    public void createFromInvalidHexhWithOddLength() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("String argument is not hexadecimal.");
        new Hash256("b804a98220c69ab4674e97142beeeb00909113d417b9d6a67c12b71a3974a21ae");
    }

    @Test
    public void createFromMalformedHash() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("String argument is not hexadecimal.");
        new Hash256("g804a98220c69ab4674e97142beeeb00909113d417b9d6a67c12b71a3974a21a");
    }

    @Test
    public void createFromHashMoreThan256Bits() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Hash must be 32 bytes long but was 33 bytes.");
        new Hash256("0xb804a98220c69ab4674e97142beeeb00909113d417b9d6a67c12b71a3974a21a12");
    }

    @Test
    public void hashFromByteArray() {
        byte[] b = Numeric.hexStringToByteArray(Numeric.reverseHexString(
                "b804a98220c69ab4674e97142beeeb00909113d417b9d6a67c12b71a3974a21a"));
        Hash256 hash = new Hash256(b);
        assertThat(hash.toString(),
                is("b804a98220c69ab4674e97142beeeb00909113d417b9d6a67c12b71a3974a21a"));
    }

    @Test
    public void toArray() {
        Hash256 hash =
                new Hash256("b804a98220c69ab4674e97142beeeb00909113d417b9d6a67c12b71a3974a21a");
        byte[] expected = reverseArray(Numeric.hexStringToByteArray(
                "b804a98220c69ab4674e97142beeeb00909113d417b9d6a67c12b71a3974a21a"));
        assertArrayEquals(expected, hash.toArray());
    }

    @Test
    public void serialize() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BinaryWriter writer = new BinaryWriter(outStream);
        new Hash256("b804a98220c69ab4674e97142beeeb00909113d417b9d6a67c12b71a3974a21a")
                .serialize(writer);
        byte[] actual = outStream.toByteArray();
        byte[] expected = reverseArray(Numeric.hexStringToByteArray(
                "b804a98220c69ab4674e97142beeeb00909113d417b9d6a67c12b71a3974a21a"));
        assertArrayEquals(expected, actual);
    }

    @Test
    public void deserialize() throws DeserializationException {
        byte[] data = reverseArray(Numeric.hexStringToByteArray(
                "b804a98220c69ab4674e97142beeeb00909113d417b9d6a67c12b71a3974a21a"));
        Hash256 hash = NeoSerializableInterface.from(data, Hash256.class);
        assertThat(hash.toString(),
                is("b804a98220c69ab4674e97142beeeb00909113d417b9d6a67c12b71a3974a21a"));
    }

    @Test
    public void equals() {
        // first message has script hash 159759880646822985762674987218710759559479736571 (as
        // integer)
        byte[] bytes1 = Numeric
                .hexStringToByteArray(
                        "1aa274391ab7127ca6d6b917d413919000ebee2b14974e67b49ac62082a904b8");
        // first message has script hash 776468865644545852461964229176363821261390671687 (as
        // integer)
        byte[] bytes2 =
                Numeric.hexStringToByteArray(
                        "b43034ab680d646f8b6ca71647aa6ba167b2eb0b3757e545f6c2715787b13272");
        Hash256 hash1 = new Hash256(bytes1);
        Hash256 hash2 = new Hash256(bytes2);
        Hash256 hash3 =
                new Hash256("0xb804a98220c69ab4674e97142beeeb00909113d417b9d6a67c12b71a3974a21a");
        assertNotEquals(hash1, hash2);
        assertNotEquals(hash2, hash1);
        assertEquals(hash1, hash1);
        assertEquals(hash1, hash3);
        assertEquals(hash1.hashCode(), hash3.hashCode());
    }

    @Test
    public void compareTo() {
        byte[] bytes1 = Numeric
                .hexStringToByteArray(
                        "1aa274391ab7127ca6d6b917d413919000ebee2b14974e67b49ac62082a904b8");
        byte[] bytes2 =
                Numeric.hexStringToByteArray(
                        "b43034ab680d646f8b6ca71647aa6ba167b2eb0b3757e545f6c2715787b13272");
        Hash256 hash1 = new Hash256(bytes1);
        Hash256 hash2 = new Hash256(bytes2);
        Hash256 hash3 =
                new Hash256("0xf4609b99e171190c22adcf70c88a7a14b5b530914d2398287bd8bb7ad95a661c");

        assertThat(hash1.compareTo(hash1), is(0));
        assertThat(hash1.compareTo(hash2), is(1));
        assertThat(hash1.compareTo(hash3), is(-1));
        assertThat(hash2.compareTo(hash1), is(-1));
        assertThat(hash2.compareTo(hash3), is(-1));
        assertThat(hash3.compareTo(hash1), is(1));
        assertThat(hash3.compareTo(hash2), is(1));
    }

    @Test
    public void getSize() {
        Hash256 hash =
                new Hash256("b804a98220c69ab4674e97142beeeb00909113d417b9d6a67c12b71a3974a21a");
        assertThat(hash.getSize(), is(32));
    }

}
