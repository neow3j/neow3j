package io.neow3j.test;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import static io.neow3j.test.TestProperties.neo3ExpressPrivateNetContainerImg;
import static io.neow3j.test.TestProperties.neo3PrivateNetContainerImg;

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

    static final String DEFAULT_NEO_EXPRESS_SOURCE = "/default.neo-express";
    static final String DEFAULT_NEO_EXPRESS_DESTINATION = "/app/default.neo-express";

    // This is the port of one of the .NET nodes which is exposed internally by the container.
    static final int EXPOSED_JSONRPC_PORT = 40332;

    public NeoTestContainer() {
        this(CONFIG_FILE_SOURCE);
    }

    public NeoTestContainer(String configFileSource) {
        this(neo3PrivateNetContainerImg(), null, configFileSource);
    }

    public static NeoTestContainer neoExpressTestContainer(Integer secondsPerBlock) {
        return new NeoTestContainer(neo3ExpressPrivateNetContainerImg(), secondsPerBlock, null);
    }

    private NeoTestContainer(String fullImageName, Integer secondsPerBlock,
            String configFileSource) {

        super(DockerImageName.parse(fullImageName));
        if (fullImageName.equals(neo3ExpressPrivateNetContainerImg())) {
            withCopyFileToContainer(MountableFile.forClasspathResource(DEFAULT_NEO_EXPRESS_SOURCE,
                    777),
                    DEFAULT_NEO_EXPRESS_DESTINATION);
            withExposedPorts(EXPOSED_JSONRPC_PORT);
            if (secondsPerBlock != null) {
                withCommand("--seconds-per-block " + secondsPerBlock);
            }
            waitingFor(Wait.forListeningPort());
        } else {
            if (configFileSource != null) {
                withClasspathResourceMapping(configFileSource, CONFIG_FILE_DESTINATION,
                        BindMode.READ_ONLY);
            } else {
                withClasspathResourceMapping(CONFIG_FILE_SOURCE, CONFIG_FILE_DESTINATION,
                        BindMode.READ_ONLY);
            }
            withCopyFileToContainer(MountableFile.forClasspathResource(WALLET_FILE_SOURCE, 777),
                    WALLET_FILE_DESTINATION);
            withClasspathResourceMapping(RPCCONFIG_FILE_SOURCE, RPCCONFIG_FILE_DESTINATION,
                    BindMode.READ_ONLY);
            withClasspathResourceMapping(DBFTCONFIG_FILE_SOURCE, DBFTCONFIG_FILE_DESTINATION,
                    BindMode.READ_ONLY);
            withExposedPorts(EXPOSED_JSONRPC_PORT);
            waitingFor(Wait.forListeningPort());
            try {
                withClasspathResourceMapping(ORACLECONFIG_FILE_SOURCE,
                        ORACLECONFIG_FILE_DESTINATION,
                        BindMode.READ_ONLY);
            } catch (IllegalArgumentException e) {
                System.out.println("OracleService config file not found at "
                        + ORACLECONFIG_FILE_SOURCE);
            }
        }
    }

    public static String getResultFilePath(String testClassName, String methodName) {
        return "responses/" + testClassName + "/" + methodName + ".json";
    }

    public static String getNodeUrl(NeoTestContainer container) {
        return "http://" + container.getContainerIpAddress() + ":" +
                container.getMappedPort(EXPOSED_JSONRPC_PORT);
    }

}
