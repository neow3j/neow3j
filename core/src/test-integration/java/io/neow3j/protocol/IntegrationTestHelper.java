package io.neow3j.protocol;

import io.neow3j.contract.Hash160;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.utils.Numeric;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import static io.neow3j.TestProperties.contractManagementHash;
import static io.neow3j.TestProperties.gasTokenHash;
import static io.neow3j.TestProperties.nameServiceHash;
import static io.neow3j.TestProperties.neo3PrivateNetContainerImg;
import static io.neow3j.TestProperties.neoTokenHash;

public class IntegrationTestHelper {

    static final String CONFIG_FILE_SOURCE = "/node-config/config.json";
    static final String CONFIG_FILE_DESTINATION = "/neo-cli/config.json";
    static final String WALLET_FILE_SOURCE = "/node-config/wallet.json";
    static final String WALLET_FILE_DESTINATION = "/neo-cli/wallet.json";
    static final String RPCCONFIG_FILE_SOURCE = "/node-config/rpcserver.config.json";
    static final String RPCCONFIG_FILE_DESTINATION = "/neo-cli/Plugins/RpcServer/config.json";
    static final String DBFTCONFIG_FILE_SOURCE = "/node-config/dbft.config.json";
    static final String DBFTCONFIG_FILE_DESTINATION = "/neo-cli/Plugins/DBFTPlugin/config.json";
    // This is the port of one of the .NET nodes which is exposed internally by the container.
    static final int EXPOSED_JSONRPC_PORT = 40332;
    // Wallet password for the node's wallnet at node-config/wallet.json.
    static final String NODE_WALLET_PASSWORD = "neo";
    // The path to the wallet from the directory of the node process.
    static final String NODE_WALLET_PATH = "wallet.json";

    // Native token hashes.
    static final Hash160 NEO_HASH = new Hash160(neoTokenHash());
    static final Hash160 GAS_HASH = new Hash160(gasTokenHash());
    static final Hash160 NAME_SERVICE_HASH = new Hash160(nameServiceHash());
    static final Hash160 CONTRACT_MANAGEMENT_HASH = new Hash160(contractManagementHash());

    // Total supply of NEO tokens.
    static final int NEO_TOTAL_SUPPLY = 100000000;
    // First account (multi-sig) in the node's wallet
    static final String ACCOUNT_1_ADDRESS = "NKvR5WeczCQMcVWQD9aaMqegfEoCBXGWpW";
    static final String ACCOUNT_1_WIF =
            "L24Qst64zASL2aLEKdJtRLnbnTbqpcRNWkWJ3yhDh2CLUtLdwYK2";

    // Second account (single-sig) in the node's wallet
    static final String ACCOUNT_2_ADDRESS = "NUrPrFLETzoe7N2FLi2dqTvLwc9L2Em84K";
    static final ECPublicKey ACCOUNT_2_PUBKEY = new ECPublicKey(
            Numeric.hexStringToByteArray(
                    "036cfcc5d0550d0481b66f58e25067280f042b4933fc013dc4930ce2a4194c9d94"));

    static final String VM_STATE_HALT = "HALT";


    static String getNodeUrl(GenericContainer<?> container) {
        return "http://" + container.getContainerIpAddress() +
                ":" + container.getMappedPort(EXPOSED_JSONRPC_PORT);
    }

    static GenericContainer<?> setupPrivateNetContainer() {
        return new GenericContainer<>(
                DockerImageName.parse(neo3PrivateNetContainerImg()))
                .withClasspathResourceMapping(CONFIG_FILE_SOURCE, CONFIG_FILE_DESTINATION,
                        BindMode.READ_ONLY)
                .withCopyFileToContainer(
                        MountableFile.forClasspathResource(WALLET_FILE_SOURCE, 777),
                        WALLET_FILE_DESTINATION)
                .withClasspathResourceMapping(RPCCONFIG_FILE_SOURCE, RPCCONFIG_FILE_DESTINATION,
                        BindMode.READ_ONLY)
                .withClasspathResourceMapping(DBFTCONFIG_FILE_SOURCE, DBFTCONFIG_FILE_DESTINATION,
                        BindMode.READ_ONLY)
                .withExposedPorts(EXPOSED_JSONRPC_PORT)
                .waitingFor(Wait.forListeningPort());
    }

}
