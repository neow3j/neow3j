package io.neow3j.transaction;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class WitnessScopeTest {

    @Test
    public void getCombinedScopes() {
        byte scope = WitnessScope.getCombinedScope(Arrays.asList(
                WitnessScope.CALLED_BY_ENTRY,
                WitnessScope.CUSTOM_CONSTRACTS));
        assertThat(scope, is((byte) 0x11));

        scope = WitnessScope.getCombinedScope(Arrays.asList(
                WitnessScope.CALLED_BY_ENTRY,
                WitnessScope.CUSTOM_CONSTRACTS,
                WitnessScope.CUSTOM_GROUPS));
        assertThat(scope, is((byte) 0x31));

        scope = WitnessScope.getCombinedScope(Arrays.asList(WitnessScope.GLOBAL));
        assertThat(scope, is((byte) 0x00));
    }

}