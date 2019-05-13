package io.neow3j.model.types;

import org.junit.Test;

import java.math.BigInteger;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class NEOAssetTest {

    @Test
    public void testToBigInt() {
        assertThat(NEOAsset.toBigInt(null), is(BigInteger.ZERO));
        assertThat(NEOAsset.toBigInt("10000000000"), is(BigInteger.valueOf(1000000000000000000L)));
    }

    @Test
    public void testField_HashId() {
        assertThat(NEOAsset.HASH_ID, is("c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b"));
    }

    @Test
    public void testField_Name() {
        assertThat(NEOAsset.NAME, is("NEO"));
    }

    @Test
    public void testField_Type() {
        assertThat(NEOAsset.TYPE, is(AssetType.GOVERNING_TOKEN));
        assertThat(NEOAsset.TYPE.jsonValue(), is("GoverningToken"));
    }

}
