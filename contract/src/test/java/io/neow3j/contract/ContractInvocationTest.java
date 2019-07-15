package io.neow3j.contract;

import io.neow3j.contract.exception.ContractInvocationException;
import io.neow3j.model.ContractParameter;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.InvocationResult;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.exceptions.ErrorResponseException;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.fail;

public class ContractInvocationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ContractInvocationTest.class);

    @Test
    public void serialize() {
        // TODO: 2019-07-03 Guil: to be implemented
    }

    @Test
    public void deserialize() {
        // TODO: 2019-07-03 Guil: to be implemented
    }

//    @Test
//    public void testFetchSystemFee() throws IOException, ErrorResponseException, ContractInvocationException {
//        // TODO 10.07.19 claude: Implement
//        String address = "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y";
//        Neow3j neow3j = Neow3j.build(new HttpService("http://nucbox.axlabs.com:30333/"));
//        Account a = Account.fromWIF("KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr").build();
//        a.updateAssetBalances(neow3j);
//        System.out.println("Has GAS: " + a.getBalances().hasAsset(GASAsset.HASH_ID));
//        System.out.println("Has " + a.getBalances().getAssetBalance(GASAsset.HASH_ID).getAmount() + " GAS.");
//
//        ContractParameter[] p = new ContractParameter[]{ new ContractParameter(
//                ContractParameterType.BYTE_ARRAY,
//                Numeric.toHexStringNoPrefix(Keys.toScriptHash(a.getAddress()))
//        )};
//        ContractInvocation invoc = new ContractInvocation.Builder(neow3j)
//                .contractScriptHash("c4497e3f01d2efb02489be0b63072d5df8b1f8bc")
//                .account(a)
//                .parameter(ContractParameterType.STRING, "register")
//                .parameter(ContractParameterType.STRING, "neow3j.neo")
//                .parameter(ContractParameterType.ARRAY, p)
//                .build();
//
//        try {
//            invoc.fetchSystemFee();
//        } catch (ErrorResponseException e) {
//            System.out.println(e.getError().getMessage());
//            fail();
//        }
//    }
//
//   @Test
//   public void testInvokeContract() throws IOException, ErrorResponseException, ContractInvocationException {
//       String address = "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y";
//       Neow3j neow3j = Neow3j.build(new HttpService("http://nucbox.axlabs.com:30333/"));
//       Account a = Account.fromWIF("KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr").build();
//       a.updateAssetBalances(neow3j);
//       LOG.info("Has GAS: " + a.getBalances().hasAsset(GASAsset.HASH_ID));
//       LOG.info("Has " + a.getBalances().getAssetBalance(GASAsset.HASH_ID).getAmount() + " GAS.");
//
//
//       ContractParameter[] p = new ContractParameter[]{
//               new ContractParameter(ContractParameterType.STRING, "neon.neo"),
//               new ContractParameter(ContractParameterType.BYTE_ARRAY, Numeric.toHexStringNoPrefix(Keys.toScriptHash(a.getAddress())))
//       };
//       ContractInvocation i = new ContractInvocation.Builder(neow3j)
//               .contractScriptHash("c4497e3f01d2efb02489be0b63072d5df8b1f8bc")
//               .account(a)
//               .parameter(ContractParameterType.STRING, "register")
//               .parameter(ContractParameterType.ARRAY, p)
//               .build();
//
//       try {
//           i.fetchSystemFee()
//                   .createTransaction()
//                   .signTransaction();
//       } catch (ContractInvocationException e) {
//           LOG.info("Contract invocation stopped with " + e.getVmState().toString());
//            e.getStack().forEach(param -> LOG.info(param.toString()));
//       }
//
//       try {
//           NeoSendRawTransaction response = i.invoke();
//           LOG.info("The response is: " + response);
//       } catch (ErrorResponseException e) {
//           LOG.info("Invocation resulted in an error with message: " + e.getError().getMessage());
//       }
//
//   }

//    @Test
//    public void testInvokeContract2() throws IOException, ErrorResponseException, ContractInvocationException {
//        String address = "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y";
//        Neow3j neow3j = Neow3j.build(new HttpService("http://nucbox.axlabs.com:30333/"));
//        Account a = Account.fromWIF("KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr").build();
//        a.updateAssetBalances(neow3j);
//
//        ContractParameter[] p = new ContractParameter[]{
//                new ContractParameter(ContractParameterType.STRING, "neow3j.neo"),
//                new ContractParameter(ContractParameterType.STRING, Numeric.toHexStringNoPrefix(Keys.toScriptHash(a.getAddress())))
//        };
//        ContractInvocation i = new ContractInvocation.Builder(neow3j)
//                .contractScriptHash("a4e6af094a380a2d0b78e8454e00e8fb8d17f0ed")
//                .account(a)
//                .parameter(ContractParameterType.STRING, "register")
//                .parameter(ContractParameterType.ARRAY, p)
////                .parameter(ContractParameterType.STRING, "test.neo")
////                .parameter(ContractParameterType.ARRAY, new ContractParameter[]{})
//                .build();
//
//        try {
//            i.fetchSystemFee()
//                    .createTransaction()
//                    .signTransaction();
//        } catch (ContractInvocationException e) {
//            LOG.info("Contract invocation stopped with " + e.getVmState().toString());
//            e.getStack().forEach(param -> LOG.info(param.toString()));
//            fail();
//        }
//
//        try {
//            NeoSendRawTransaction response = i.invoke();
//            LOG.info(response.toString());
//        } catch (ErrorResponseException e) {
//            LOG.info(e.getError().getMessage());
//        }
//
//    }

    @Test
    public void testInvokeContract3() throws IOException, ErrorResponseException, ContractInvocationException {
        Neow3j neow3j = Neow3j.build(new HttpService("http://nucbox.axlabs.com:30333"));
        Account a = Account.fromWIF("KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr").build();
        a.updateAssetBalances(neow3j);
        String scriptHash = "45f884c16dea105d6a83492584cc8ef353f75ba9";


//        String remark = Long.toString(Instant.now().toEpochMilli())
//                + Numeric.toHexStringNoPrefix(SecureRandomUtils.generateRandomBytes(4));
//        RawTransactionAttribute remarkAttr =  new RawTransactionAttribute(
//                TransactionAttributeUsageType.REMARK, remark);
        String domainName = "claude.com";
//        String domainName = "neo.com";
        String address = "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y";

        ContractInvocation i = new ContractInvocation.Builder(neow3j)
                .contractScriptHash(scriptHash)
                .account(a)
                .parameter(ContractParameter.string("register"))
                .parameter(ContractParameter.array(
                        ContractParameter.string(domainName),
                        ContractParameter.byteArrayFromAddress(address)
                ))
                .build();

        try {
            i.fetchSystemFee();
        } catch(ContractInvocationException e) {
            LOG.info("Invocation simulation failed", e);
        }
        i.createTransaction().signTransaction();

        InvocationResult result = i.simulateInvoke();
        LOG.info("----------------- SIMULATION RESULT: -----------------");
        LOG.info(result.toString());

//        try {
//            NeoSendRawTransaction resp = i.invoke();
//            LOG.info("----------------- INVOCATION RESULT: -----------------");
//            LOG.info("Success: " + resp.getSendRawTransaction().toString());
//        } catch (ErrorResponseException e) {
//            LOG.info(e.getError().getMessage());
//        }

    }

    @Test
    public void testTest() {
        long l = -1;
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(l);
        LOG.info(Numeric.toHexStringNoPrefix(buffer.array()));
    }

}
