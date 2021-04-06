package io.neow3j.protocol;

import io.neow3j.contract.Hash160;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.model.types.NeoVMStateType;
import io.neow3j.utils.Numeric;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import static io.neow3j.TestProperties.contractManagementHash;
import static io.neow3j.TestProperties.gasTokenHash;
import static io.neow3j.TestProperties.nameServiceHash;
import static io.neow3j.TestProperties.neo3PrivateNetContainerImg;
import static io.neow3j.TestProperties.neoTokenHash;

public class IntegrationTestHelper {

    // Wallet password for the node's wallnet at node-config/wallet.json.
    static final String NODE_WALLET_PASSWORD = "neo";
    // The path to the wallet from the directory of the node process.
    static final String NODE_WALLET_PATH = "wallet.json";

    // Native token hashes.
    static final Hash160 NEO_HASH = new Hash160(neoTokenHash());
    static final Hash160 GAS_HASH = new Hash160(gasTokenHash());

}
