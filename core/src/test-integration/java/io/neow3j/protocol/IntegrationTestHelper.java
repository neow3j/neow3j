package io.neow3j.protocol;

import io.neow3j.contract.Hash160;

import static io.neow3j.TestProperties.gasTokenHash;
import static io.neow3j.TestProperties.neoTokenHash;

public class IntegrationTestHelper {

    // Wallet password for the node's wallet at node-config/wallet.json.
    static final String NODE_WALLET_PASSWORD = "neo";
    // The path to the wallet from the directory of the node process.
    static final String NODE_WALLET_PATH = "wallet.json";

    // Native token hashes.
    static final Hash160 NEO_HASH = new Hash160(neoTokenHash());
    static final Hash160 GAS_HASH = new Hash160(gasTokenHash());

}
