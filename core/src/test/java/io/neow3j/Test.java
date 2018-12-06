package io.neow3j;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.BlockParameterIndex;
import io.neow3j.protocol.core.methods.response.NeoBlockCount;
import io.neow3j.protocol.core.methods.response.NeoGetValidators;
import io.neow3j.protocol.core.methods.response.NeoGetVersion;
import io.neow3j.protocol.core.methods.response.NeoGetBlock;
import io.neow3j.protocol.core.methods.response.NeoValidateAddress;
import io.neow3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;

public class Test {

    public static void main(String[] args) {

        //Neow3j w = Neow3j.build(new HttpService("http://localhost:30333/"));
        Neow3j w = Neow3j.build(new HttpService("http://seed10.ngd.network:10332"));

        try {
            NeoGetVersion clientVersion = w.getVersion().send();
            System.out.println(clientVersion.getVersion().getUserAgent());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            NeoBlockCount blockCount = w.getBlockCount().send();
            System.out.println(blockCount.getBlockIndex());

            NeoGetBlock getBlock = w.getBlock(new BlockParameterIndex(blockCount.getBlockIndex().subtract(BigInteger.valueOf(1))), true).send();
            System.out.println(getBlock.getBlock());

            NeoValidateAddress validateResult = w.validateAddress("ARvMqz3hEFE4qBkHAaPNxALquNQtBbH12f").send();
            System.out.println("isValid=" + validateResult.getValidation().isValid());

            NeoGetValidators getValidatorsResult = w.getValidators().send();
            System.out.println(getValidatorsResult.getValidators());

        } catch (IOException e) {
            e.printStackTrace();
        }


//        w.catchUpToLatestAndSubscribeToNewBlocksObservable(new BlockParameterIndex(2889367), true)
//                .subscribe((blockReqResult) -> {
//                    System.out.println("######################");
//                    System.out.println("blockIndex: " + blockReqResult.getBlock().getIndex());
//                    System.out.println("hashId: " + blockReqResult.getBlock().getHash());
//                    System.out.println("confirmations: " + blockReqResult.getBlock().getConfirmations());
//                    System.out.println("transactions: " + blockReqResult.getBlock().getTransactions());
//                });
    }

}
