package io.neow3j.contract;

import static io.neow3j.TestProperties.client1AccountWIF;
import static io.neow3j.TestProperties.client2AccountWIF;
import static io.neow3j.TestProperties.defaultAccountWIF;
import static io.neow3j.TestProperties.gasTokenHash;
import static io.neow3j.TestProperties.neoTokenHash;
import static io.neow3j.utils.Await.waitUntilTransactionIsExecuted;
import static io.neow3j.wallet.Account.createMultiSigAccount;
import static io.neow3j.wallet.Account.fromWIF;
import static java.util.Collections.singletonList;

import io.neow3j.protocol.Neow3j;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;

import java.math.BigDecimal;
import java.math.BigInteger;

public class IntegrationTestHelper {

    // Native token hashes.
    static final Hash160 NEO_HASH = new Hash160(neoTokenHash());
    static final Hash160 GAS_HASH = new Hash160(gasTokenHash());

    static final Account DEFAULT_ACCOUNT = fromWIF(defaultAccountWIF());
    static final Account COMMITTEE_ACCOUNT =
            createMultiSigAccount(singletonList(DEFAULT_ACCOUNT.getECKeyPair().getPublicKey()), 1);
    static final Wallet COMMITTEE_WALLET = Wallet.withAccounts(DEFAULT_ACCOUNT, COMMITTEE_ACCOUNT);
    static final Account CLIENT_1 = fromWIF(client1AccountWIF());
    static final Account CLIENT_2 = fromWIF(client2AccountWIF());
    static final Wallet CLIENTS_WALLET = Wallet.withAccounts(CLIENT_1, CLIENT_2);

    static void fundAccountsWithGas(Neow3j neow3j, Account... accounts) throws Throwable {
        GasToken gasToken = new GasToken(neow3j);
        BigInteger fractions = gasToken.toFractions(new BigDecimal("100000"));
        for (Account account : accounts) {
            transferFromGenesisToAccount(neow3j, new GasToken(neow3j), fractions, account);
        }
    }

    static void fundAccountsWithNeo(Neow3j neow3j, BigInteger amount, Account... accounts)
            throws Throwable {
        NeoToken neoToken = new NeoToken(neow3j);
        for (Account account : accounts) {
            transferFromGenesisToAccount(neow3j, neoToken, amount, account);
        }
    }

    static void transferFromGenesisToAccount(Neow3j neow3j, FungibleToken token,
            BigInteger amount, Account a) throws Throwable {
        Hash256 txHash =
                token.transfer(COMMITTEE_WALLET, a.getScriptHash(), amount)
                        .sign()
                        .send()
                        .getSendRawTransaction()
                        .getHash();
        waitUntilTransactionIsExecuted(txHash, neow3j);
    }

}
