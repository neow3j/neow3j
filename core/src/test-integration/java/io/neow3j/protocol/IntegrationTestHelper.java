package io.neow3j.protocol;

import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.utils.Numeric;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

public class IntegrationTestHelper {

    static final String NEO3_PRIVATENET_CONTAINER_IMG = "ghcr.io" +
            "/axlabs/neo3-privatenet-docker/neo-cli-with-plugins:latest";

    static final String CONFIG_FILE_SOURCE = "/node-config/config.json";
    static final String CONFIG_FILE_DESTINATION = "/neo-cli/config.json";
    static final String PROTOCOL_FILE_SOURCE = "/node-config/protocol.json";
    static final String PROTOCOL_FILE_DESTINATION = "/neo-cli/protocol.json";
    static final String WALLET_FILE_SOURCE = "/node-config/wallet.json";
    static final String WALLET_FILE_DESTINATION = "/neo-cli/wallet.json";
    static final String RPCCONFIG_FILE_SOURCE = "/node-config/rpcserver.config.json";
    static final String RPCCONFIG_FILE_DESTINATION = "/neo-cli/Plugins/RpcServer/config.json";
    // This is the port of one of the .NET nodes which is exposed internally by the container.
    static final int EXPOSED_JSONRPC_PORT = 40332;
    // Wallet password for the node's wallnet at node-config/wallet.json.
    static final String NODE_WALLET_PASSWORD = "neo";
    // The path to the wallet from the directory of the node process.
    static final String NODE_WALLET_PATH = "wallet.json";

    // Hash of the NEO token.
    static final String NEO_HASH = "de5f57d430d3dece511cf975a8d37848cb9e0525";
    static final String GAS_HASH = "668e0c1f9d7b70a99dd9e06eadd4c784d641afbc";
    // Total supply of NEO tokens.
    static final int NEO_TOTAL_SUPPLY = 100000000;
    // First account (multi-sig) in the node's wallet
    static final String ACCOUNT_1_ADDRESS = "NX8GreRFGFK5wpGMWetpX93HmtrezGogzk";
    static final String ACCOUNT_1_WIF =
            "L3kCZj6QbFPwbsVhxnB8nUERDy4mhCSrWJew4u5Qh5QmGMfnCTda";
    // Second account (single-sig) in the node's wallet
    static final String ACCOUNT_2_ADDRESS = "NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj";
    static final ECPublicKey ACCOUNT_2_PUBKEY = new ECPublicKey(
            Numeric.hexStringToByteArray(
                    "02163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b60"));

    static final String VM_STATE_HALT = "HALT";


    static String getNodeUrl(GenericContainer container) {
        return "http://" + container.getContainerIpAddress() +
                ":" + container.getMappedPort(EXPOSED_JSONRPC_PORT);
    }

    static GenericContainer setupPrivateNetContainer() {
        return new GenericContainer(
                DockerImageName.parse(NEO3_PRIVATENET_CONTAINER_IMG))
                .withClasspathResourceMapping(CONFIG_FILE_SOURCE, CONFIG_FILE_DESTINATION,
                        BindMode.READ_ONLY)
                .withClasspathResourceMapping(PROTOCOL_FILE_SOURCE, PROTOCOL_FILE_DESTINATION,
                        BindMode.READ_ONLY)
                .withCopyFileToContainer(
                        MountableFile.forClasspathResource(WALLET_FILE_SOURCE, 777),
                        WALLET_FILE_DESTINATION)
                .withClasspathResourceMapping(RPCCONFIG_FILE_SOURCE, RPCCONFIG_FILE_DESTINATION,
                        BindMode.READ_ONLY)
                .withExposedPorts(EXPOSED_JSONRPC_PORT)
                .waitingFor(Wait.forListeningPort());
    }
}
