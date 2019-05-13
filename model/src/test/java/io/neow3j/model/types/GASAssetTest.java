package io.neow3j.model.types;

import org.junit.Test;

import java.math.BigInteger;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class GASAssetTest {

    @Test
    public void testToBigInt() {
        assertThat(GASAsset.toBigInt(null), is(BigInteger.ZERO));
        assertThat(GASAsset.toBigInt("10000000000"), is(BigInteger.valueOf(1000000000000000000L)));
    }

    @Test
    public void testField_HashId() {
        assertThat(GASAsset.HASH_ID, is("602c79718b16e442de58778e148d0b1084e3b2dffd5de6b7b16cee7969282de7"));
    }

    @Test
    public void testField_Name() {
        assertThat(GASAsset.NAME, is("NEOGas"));
    }

    @Test
    public void testField_Type() {
        assertThat(GASAsset.TYPE, is(AssetType.UTILITY_TOKEN));
        assertThat(GASAsset.TYPE.jsonValue(), is("UtilityToken"));
    }

}
