package io.neow3j.contract;

import io.neow3j.contract.ContractInvocation.Builder;
import io.neow3j.model.ContractParameter;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.InvocationResult;
import io.neow3j.protocol.exceptions.ErrorResponseException;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.wallet.Account;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ContractInvocationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ContractInvocationTest.class);

    @Test
    public void testInvokeContract() throws IOException, ErrorResponseException {
        Neow3j neow3j = Neow3j.build(new HttpService("http://nucbox.axlabs.com:30333"));
        Account a = Account.fromWIF("KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr").build();
        a.updateAssetBalances(neow3j);
        String scriptHash = "fb3b7244a0d54259afabcc0ba3f1d2bd2d611884";

        String domainName = "neo.com";
        String address = "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y";

        ContractInvocation i = new Builder(neow3j)
                .contractScriptHash(scriptHash)
                .account(a)
                .parameter(ContractParameter.string("register"))
                .parameter(ContractParameter.array(
                        ContractParameter.string(domainName),
                        ContractParameter.byteArrayFromAddress(address)
                ))
                .build()
                .sign();

        InvocationResult result = i.testInvoke();
        LOG.info("----------------- SIMULATION RESULT: -----------------");
        LOG.info(result.toString());

        try {
            i.invoke();
            LOG.info("Success");
        } catch (ErrorResponseException e) {
            LOG.info(e.getError().getMessage());
        }

    }

}
