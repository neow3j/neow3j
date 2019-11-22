package io.neow3j.transaction;

import io.neow3j.contract.ScriptHash;
import io.neow3j.transaction.exceptions.CosignerConfigurationException;
import io.neow3j.utils.Numeric;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.security.spec.ECPoint;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CosignerTest {

    private ScriptHash account;
    private ScriptHash contract1;
    private ScriptHash contract2;
    private ECPoint groupPubKey1;
    private ECPoint groupPubKey2;

    @Before
    public void setUp() throws Exception {
        account = ScriptHash.fromAddress("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y");
        contract1 = ScriptHash.fromScript(Numeric.hexStringToByteArray("d802a401"));
        contract2 = ScriptHash.fromScript(Numeric.hexStringToByteArray("c503b112"));
        groupPubKey1 = new ECPoint(BigInteger.ONE, BigInteger.ONE);
        groupPubKey2 = new ECPoint(BigInteger.TEN, BigInteger.TEN);
    }

    @Test
    public void createCosignerWithCallByEntryWitnessScope() {
        Cosigner cos = Cosigner.calledByEntry(account);
        assertThat(cos.getAccount(), is(account));
        assertThat(cos.getScopes(), hasSize(1));
        assertThat(cos.getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
        assertTrue(cos.getAllowedContracts().isEmpty());
        assertTrue(cos.getAllowedGroups().isEmpty());
    }

    @Test
    public void createCosignerWithGlobalWitnessScope() {
        Cosigner cos = Cosigner.global(account);
        assertThat(cos.getAccount(), is(account));
        assertThat(cos.getScopes(), hasSize(1));
        assertThat(cos.getScopes(), contains(WitnessScope.GLOBAL));
        assertTrue(cos.getAllowedContracts().isEmpty());
        assertTrue(cos.getAllowedGroups().isEmpty());
    }

    @Test
    public void buildValidCosigner1() {
        Cosigner cos = new Cosigner.Builder()
                .account(this.account)
                .scopes(WitnessScope.CUSTOM_CONSTRACTS, WitnessScope.CALLED_BY_ENTRY)
                .allowedContracts(this.contract1, this.contract2)
                .build();

        assertThat(cos.getAccount(), is(this.account));
        assertThat(cos.getScopes(), hasSize(2));
        assertThat(cos.getScopes(),
                containsInAnyOrder(WitnessScope.CUSTOM_CONSTRACTS, WitnessScope.CALLED_BY_ENTRY));
        assertThat(cos.getAllowedContracts(), hasSize(2));
        assertThat(cos.getAllowedContracts(), containsInAnyOrder(this.contract1, this.contract2));
        assertTrue(cos.getAllowedGroups().isEmpty());
    }

    @Test
    public void buildValidCosigner2() {
        Cosigner cos = new Cosigner.Builder()
                .account(this.account)
                // the allowed contracts scope is added automatically.
                .allowedContracts(this.contract1, this.contract2)
                .build();

        assertThat(cos.getAccount(), is(this.account));
        assertThat(cos.getScopes(), hasSize(1));
        assertThat(cos.getScopes(), containsInAnyOrder(WitnessScope.CUSTOM_CONSTRACTS));
        assertThat(cos.getAllowedContracts(), hasSize(2));
        assertThat(cos.getAllowedContracts(),
                containsInAnyOrder(this.contract1, this.contract2));
        assertTrue(cos.getAllowedGroups().isEmpty());
    }

    @Test
    public void buildValidCosigner3() {
        Cosigner cos = new Cosigner.Builder()
                .account(this.account)
                .allowedGroups(this.groupPubKey1, this.groupPubKey2)
                .build();

        assertThat(cos.getAccount(), is(this.account));
        assertThat(cos.getScopes(), hasSize(1));
        assertThat(cos.getScopes(), containsInAnyOrder(WitnessScope.CUSTOM_GROUPS));
        assertThat(cos.getAllowedGroups(), hasSize(2));
        assertThat(cos.getAllowedGroups(),
                containsInAnyOrder(this.groupPubKey1, this.groupPubKey2));
        assertTrue(cos.getAllowedContracts().isEmpty());
    }


    @Test(expected = CosignerConfigurationException.class)
    public void tryBuildEmptyCosigner() {
        new Cosigner.Builder().build();
    }

    @Test(expected = CosignerConfigurationException.class)
    public void tryBuildCosignerWithoutScopes() {
        new Cosigner.Builder()
                .account(this.account)
                .build();
    }

    @Test(expected = CosignerConfigurationException.class)
    public void tryBuildCosignerWithoutAccount() {
        new Cosigner.Builder()
                .scopes(WitnessScope.CALLED_BY_ENTRY)
                .build();
    }

    @Test(expected = CosignerConfigurationException.class)
    public void tryBuildCosignerWithGlobalAndAnyOtherScope() {
        try {
            new Cosigner.Builder()
                    .account(this.account)
                    .scopes(WitnessScope.GLOBAL)
                    .scopes(WitnessScope.CUSTOM_CONSTRACTS)
                    .build();
        } catch (CosignerConfigurationException e) {
            // continue
        }

        try {
            new Cosigner.Builder()
                    .account(this.account)
                    .scopes(WitnessScope.GLOBAL)
                    .scopes(WitnessScope.CUSTOM_GROUPS)
                    .build();
        } catch (CosignerConfigurationException e) {
            // continue
        }

        new Cosigner.Builder()
                .account(this.account)
                .scopes(WitnessScope.GLOBAL)
                .scopes(WitnessScope.CALLED_BY_ENTRY)
                .build();
    }

    @Test(expected = CosignerConfigurationException.class)
    public void tryBuildCustomContractsCosignerWithoutSpecifyingAllowedContracts() {
        new Cosigner.Builder()
                .account(this.account)
                .scopes(WitnessScope.CUSTOM_CONSTRACTS)
                .build();
    }

    @Test(expected = CosignerConfigurationException.class)
    public void tryBuildCustomGroupsCosignerWithoutSpecifyingAllowedContracts() {
        new Cosigner.Builder()
                .account(this.account)
                .scopes(WitnessScope.CUSTOM_GROUPS)
                .build();
    }

}