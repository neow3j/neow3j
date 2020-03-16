package io.neow3j.model.types;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class NEOAssetTest {

    @Test
    public void testField_HashId() {
        assertThat(NEOAsset.HASH_ID, is("43cf98eddbe047e198a3e5d57006311442a0ca15"));
    }

    @Test
    public void testField_Name() {
        assertThat(NEOAsset.NAME, is("NEO"));
    }

}
