package io.neow3j.compiler.utils;

import static io.neow3j.TestProperties.neo3PrivateNetContainerImg;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

public class NeoTestContainer extends GenericContainer<NeoTestContainer> {

    static final String CONFIG_FILE_SOURCE = "/node-config/config.json";
    static final String CONFIG_FILE_DESTINATION = "/neo-cli/config.json";
    static final String WALLET_FILE_SOURCE = "/node-config/wallet.json";
    static final String WALLET_FILE_DESTINATION = "/neo-cli/wallet.json";
    static final String RPCCONFIG_FILE_SOURCE = "/node-config/rpcserver.config.json";
    static final String RPCCONFIG_FILE_DESTINATION = "/neo-cli/Plugins/RpcServer/config.json";
    static final String DBFTCONFIG_FILE_SOURCE = "/node-config/dbft.config.json";
    static final String DBFTCONFIG_FILE_DESTINATION = "/neo-cli/Plugins/DBFTPlugin/config.json";
    static final String ORACLECONFIG_FILE_SOURCE = "/node-config/oracle.config.json";
    static final String ORACLECONFIG_FILE_DESTINATION = "/neo-cli/Plugins/OracleService/config"
            + ".json";

    // This is the port of one of the .NET nodes which is exposed internally by the container.
    static final int EXPOSED_JSONRPC_PORT = 40332;

    public NeoTestContainer() {
        super(DockerImageName.parse(neo3PrivateNetContainerImg()));
        this.withClasspathResourceMapping(CONFIG_FILE_SOURCE, CONFIG_FILE_DESTINATION,
                BindMode.READ_ONLY)
                .withCopyFileToContainer(
                        MountableFile.forClasspathResource(WALLET_FILE_SOURCE, 777),
                        WALLET_FILE_DESTINATION)
                .withClasspathResourceMapping(RPCCONFIG_FILE_SOURCE, RPCCONFIG_FILE_DESTINATION,
                        BindMode.READ_ONLY)
                .withClasspathResourceMapping(DBFTCONFIG_FILE_SOURCE, DBFTCONFIG_FILE_DESTINATION,
                        BindMode.READ_ONLY)
                .withClasspathResourceMapping(ORACLECONFIG_FILE_SOURCE,
                        ORACLECONFIG_FILE_DESTINATION,
                        BindMode.READ_ONLY)
                .withExposedPorts(EXPOSED_JSONRPC_PORT)
                .waitingFor(Wait.forListeningPort());
    }

    public static String getResultFilePath(String testClassName, String methodName) {
        return "responses/" + testClassName + "/" + methodName + ".json";
    }

    public static String getNodeUrl(GenericContainer<?> container) {
        return "http://" + container.getContainerIpAddress() +
                ":" + container.getMappedPort(EXPOSED_JSONRPC_PORT);
    }
}
