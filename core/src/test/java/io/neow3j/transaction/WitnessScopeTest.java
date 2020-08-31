package io.neow3j.transaction;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class WitnessScopeTest {

    @Test
    public void combineScopes() {
        byte scope = WitnessScope.combineScopes(Arrays.asList(
                WitnessScope.CALLED_BY_ENTRY,
                WitnessScope.CUSTOM_CONTRACTS));
        assertThat(scope, is((byte) 0x11));

        scope = WitnessScope.combineScopes(Arrays.asList(
                WitnessScope.CALLED_BY_ENTRY,
                WitnessScope.CUSTOM_CONTRACTS,
                WitnessScope.CUSTOM_GROUPS));
        assertThat(scope, is((byte) 0x31));

        scope = WitnessScope.combineScopes(Arrays.asList(WitnessScope.GLOBAL));
        assertThat(scope, is((byte) 0x80));

        scope = WitnessScope.combineScopes(Arrays.asList(WitnessScope.FEE_ONLY));
        assertThat(scope, is((byte) 0x00));
    }

    @Test
    public void extractCombinedScopes() {
        List<WitnessScope> scopes = WitnessScope.extractCombinedScopes((byte) 0x00);
        assertThat(scopes, contains(WitnessScope.FEE_ONLY));

        scopes = WitnessScope.extractCombinedScopes((byte) 0x80);
        assertThat(scopes, contains(WitnessScope.GLOBAL));

        scopes = WitnessScope.extractCombinedScopes((byte) 0x11);
        assertThat(scopes, containsInAnyOrder(
                WitnessScope.CALLED_BY_ENTRY,
                WitnessScope.CUSTOM_CONTRACTS));

        scopes = WitnessScope.extractCombinedScopes((byte) 0x21);
        assertThat(scopes, containsInAnyOrder(
                WitnessScope.CALLED_BY_ENTRY,
                WitnessScope.CUSTOM_GROUPS));

        scopes = WitnessScope.extractCombinedScopes((byte) 0x31);
        assertThat(scopes, containsInAnyOrder(
                WitnessScope.CALLED_BY_ENTRY,
                WitnessScope.CUSTOM_GROUPS,
                WitnessScope.CUSTOM_CONTRACTS));

    }
}