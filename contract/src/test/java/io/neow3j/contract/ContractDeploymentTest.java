package io.neow3j.contract;

import static junit.framework.TestCase.fail;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.wallet.Account;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

public class ContractDeploymentTest {

    private static final String ICO_CONTRACT_AVM_FILENAME = "/contracts/ico-test1.avm";

    private Neow3j neow3j;
    private Account acct;

    @Before
    public void setUp() throws IOException {
        this.neow3j = Neow3j.build(new HttpService(""));
        this.acct = Account.fromWIF("KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr").build();
    }

    @Test
    public void deployment() throws IOException {
        fail();
    }


    @Test
    public void failWithoutAVMFile() {
        try {
            new ContractDeployment.Builder(neow3j)
                    .account(acct)
                    .build();
        } catch (IllegalStateException e) {
            if (e.getMessage().equals("AVM script binary not set.")) {
                return;
            }
        }
        fail();
    }

    @Test
    public void failWithoutAccount() throws IOException {
        try {
            new ContractDeployment.Builder(neow3j)
                    .loadAVMFile(getTestAbsoluteFileName(ICO_CONTRACT_AVM_FILENAME))
                    .build();
        } catch (IllegalStateException e) {
            if (e.getMessage().equals("Account not set.")) {
                return;
            }
        }
        fail();
    }

    @Test
    public void failWithoutNeow3j() throws IOException {
        try {
            new ContractDeployment.Builder(null)
                    .loadAVMFile(getTestAbsoluteFileName(ICO_CONTRACT_AVM_FILENAME))
                    .account(acct)
                    .build();
        } catch (IllegalStateException e) {
            if (e.getMessage().equals("Neow3j not set.")) {
                return;
            }
        }
        fail();
    }

    private String getTestAbsoluteFileName(String fileName) {
        return this.getClass().getResource(fileName).getFile();
    }

}
