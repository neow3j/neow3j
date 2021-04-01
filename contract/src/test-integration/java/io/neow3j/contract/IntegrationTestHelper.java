package io.neow3j.contract;

import io.neow3j.protocol.Neow3j;
import io.neow3j.utils.Await;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;

import java.math.BigDecimal;

import static io.neow3j.TestProperties.gasTokenHash;
import static io.neow3j.TestProperties.neoTokenHash;
import static io.neow3j.wallet.Account.fromWIF;
import static java.util.Collections.singletonList;

public class IntegrationTestHelper {

    // Native token hashes.
    static final Hash160 NEO_HASH = new Hash160(neoTokenHash());
    static final Hash160 GAS_HASH = new Hash160(gasTokenHash());

    static final Account singleSigCommitteeMember =
            fromWIF("L24Qst64zASL2aLEKdJtRLnbnTbqpcRNWkWJ3yhDh2CLUtLdwYK2");
    static final Account committee = Account.createMultiSigAccount(
            singletonList(singleSigCommitteeMember.getECKeyPair().getPublicKey()), 1);
    static final Wallet committeeWallet =
            Wallet.withAccounts(singleSigCommitteeMember, committee);
    static final Account client1 =
            fromWIF("L3gSLs2CSRYss1zoTmSB9hYAxqimn7Br5yDomH8FDb6NDsupeRVK");
    static final Account client2 =
            fromWIF("L4oDbG4m9f7cnHyawQ4HWJJSrcVDZ8k3E4YxL7Ran89FL2t31hya");
    static final Wallet walletClients12 = Wallet.withAccounts(client1, client2);

    static void fundAccountsWithGas(Neow3j neow3j, Account... accounts) throws Throwable {
        for (Account account : accounts) {
            transferGasFromGenesisToAccount(neow3j, account);
        }
    }

    static void transferGasFromGenesisToAccount(Neow3j neow3j, Account a) throws Throwable {
        Hash256 txHash = new GasToken(neow3j)
                .transfer(committeeWallet, a.getScriptHash(), new BigDecimal("100000"))
                .sign()
                .send()
                .getSendRawTransaction()
                .getHash();
        Await.waitUntilTransactionIsExecuted(txHash, neow3j);
    }

}