package io.neow3j.protocol;

import io.neow3j.types.Hash160;

import static io.neow3j.test.TestProperties.committeeAccountAddress;
import static io.neow3j.test.TestProperties.defaultAccountAddress;
import static io.neow3j.test.TestProperties.gasTokenHash;
import static io.neow3j.test.TestProperties.neoTokenHash;

public class IntegrationTestHelper {

    // Wallet password for the node's wallet at node-config/wallet.json.
    static final String NODE_WALLET_PASSWORD = "neo";
    // The path to the wallet from the directory of the node process.
    static final String NODE_WALLET_PATH = "wallet.json";

    static final Hash160 COMMITTEE_HASH = Hash160.fromAddress(committeeAccountAddress());
    static final Hash160 DEFAULT_ACCOUNT_HASH = Hash160.fromAddress(defaultAccountAddress());

    // Native token hashes.
    static final Hash160 NEO_HASH = new Hash160(neoTokenHash());
    static final Hash160 GAS_HASH = new Hash160(gasTokenHash());

}
