package io.neow3j.test;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

public class NeoTestContainer extends GenericContainer<NeoTestContainer> {

    public static final String CONFIG_BASE_DIR = "neo-cli-config/";
    public static final String CONFIG_FILE_SOURCE = CONFIG_BASE_DIR + "config.json";

    static final String CONFIG_FILE_DESTINATION = "/neo-cli/config.json";
    static final String WALLET_FILE_SOURCE = CONFIG_BASE_DIR + "wallet.json";
    static final String WALLET_FILE_DESTINATION = "/neo-cli/wallet.json";
    static final String RPCCONFIG_FILE_SOURCE = CONFIG_BASE_DIR + "rpcserver.config.json";
    static final String RPCCONFIG_FILE_DESTINATION = "/neo-cli/Plugins/RpcServer/config.json";
    static final String DBFTCONFIG_FILE_SOURCE = CONFIG_BASE_DIR + "dbft.config.json";
    static final String DBFTCONFIG_FILE_DESTINATION = "/neo-cli/Plugins/DBFTPlugin/config.json";
    static final String ORACLECONFIG_FILE_SOURCE = CONFIG_BASE_DIR + "oracle.config.json";
    static final String ORACLECONFIG_FILE_DESTINATION = "/neo-cli/Plugins/OracleService/config.json";
    static final String APPLOGSCONFIG_FILE_SOURCE = CONFIG_BASE_DIR + "applicationlogs.config.json";
    static final String APPLOGSCONFIG_FILE_DESTINATION = "/neo-cli/Plugins/ApplicationLogs/config.json";
    static final String TOKEN_TRACKER_FILE_SOURCE = CONFIG_BASE_DIR + "tokentracker.config.json";
    static final String RPCNEP17TRACKER_FILE_DESTINATION = "/neo-cli/Plugins/TokensTracker/config.json";
    static final String STATE_SERVICE_FILE_SOURCE = CONFIG_BASE_DIR + "stateservice.config.json";
    static final String STATE_SERVICE_FILE_DESTINATION = "/neo-cli/Plugins/StateService/config.json";

    // This is the port of one of the .NET nodes which is exposed internally by the container.
    static final int EXPOSED_JSONRPC_PORT = 40332;

    public NeoTestContainer() {
        this(CONFIG_FILE_SOURCE);
    }

    public NeoTestContainer(String configFileSource) {
        super(DockerImageName.parse(TestProperties.neo3PrivateNetContainerImg()));
        if (configFileSource != null) {
            withClasspathResourceMapping(configFileSource, CONFIG_FILE_DESTINATION, BindMode.READ_ONLY);
        } else {
            withClasspathResourceMapping(CONFIG_FILE_SOURCE, CONFIG_FILE_DESTINATION, BindMode.READ_ONLY);
        }
        withCopyFileToContainer(MountableFile.forClasspathResource(WALLET_FILE_SOURCE, 777), WALLET_FILE_DESTINATION);
        withClasspathResourceMapping(RPCCONFIG_FILE_SOURCE, RPCCONFIG_FILE_DESTINATION, BindMode.READ_ONLY);
        withClasspathResourceMapping(DBFTCONFIG_FILE_SOURCE, DBFTCONFIG_FILE_DESTINATION, BindMode.READ_ONLY);
        withClasspathResourceMapping(APPLOGSCONFIG_FILE_SOURCE, APPLOGSCONFIG_FILE_DESTINATION, BindMode.READ_ONLY);
        withClasspathResourceMapping(TOKEN_TRACKER_FILE_SOURCE, RPCNEP17TRACKER_FILE_DESTINATION, BindMode.READ_ONLY);
        withClasspathResourceMapping(STATE_SERVICE_FILE_SOURCE, STATE_SERVICE_FILE_DESTINATION, BindMode.READ_ONLY);
        withExposedPorts(EXPOSED_JSONRPC_PORT);
        waitingFor(Wait.forListeningPort());
        try {
            withClasspathResourceMapping(ORACLECONFIG_FILE_SOURCE, ORACLECONFIG_FILE_DESTINATION, BindMode.READ_ONLY);
        } catch (IllegalArgumentException e) {
            System.out.println("OracleService config file not found at " + ORACLECONFIG_FILE_SOURCE);
        }
    }

    public static String getResultFilePath(String testClassName, String methodName) {
        return "responses/" + testClassName + "/" + methodName + ".json";
    }

    public String getNodeUrl() {
        return "http://" + getHost() + ":" + getMappedPort(EXPOSED_JSONRPC_PORT);
    }

}
