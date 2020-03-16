package io.neow3j.model.types;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.math.BigInteger;
import org.junit.Test;

public class GASAssetTest {

    @Test
    public void testToBigInt() {
        assertThat(GASAsset.toBigInt(null), is(BigInteger.ZERO));
        assertThat(GASAsset.toBigInt("10000000000"), is(BigInteger.valueOf(1000000000000000000L)));
    }

    @Test
    public void testField_HashId() {
        assertThat(GASAsset.HASH_ID, is("a1760976db5fcdfab2a9930e8f6ce875b2d18225"));
    }

    @Test
    public void testField_Name() {
        assertThat(GASAsset.NAME, is("NEOGas"));
    }

}
