package io.neow3j.test;

import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

public class NeoExpressTestContainer extends GenericContainer<NeoExpressTestContainer> {

    /**
     * Default location for a neoxp checkpoint file in the container.
     */
    public static final String CHECKPOINT_DEST = "/app/setup.neoxp-checkpoint";

    /**
     * Default location for a neoxp batch file in the container.
     */
    public static final String BATCH_DEST = "/app/setup.batch";

    /**
     * Default location for the neoxp config file in the container.
     */
    public static final String CONFIG_DEST = "/app/default.neo-express";
    static final String DEFAULT_CONFIG_SOURCE = "/default.neo-express";

    // This is the port of neo-express node which is exposed by the container.
    static final int EXPOSED_JSONRPC_PORT = 40332;

    /**
     * Creates a new instance of a docker container running a neo-express private network.
     *
     * @param secondsPerBlock The block time to use by neo-express.
     * @param resources       Names of files that should be copied into the container. For each
     *                        file you need to set a source and a destination (in the container)
     *                        path
     *                        consecutively.
     */
    public NeoExpressTestContainer(int secondsPerBlock, String... resources) {
        super(DockerImageName.parse(TestProperties.neo3ExpressPrivateNetContainerImg()));
        withCopyFileToContainer(MountableFile.forClasspathResource(DEFAULT_CONFIG_SOURCE, 777),
                CONFIG_DEST);
        withExposedPorts(EXPOSED_JSONRPC_PORT);

        int i = 0;
        while (resources != null && i + 1 < resources.length) {
            String src = resources[i++];
            String dest = resources[i++];
            withCopyFileToContainer(MountableFile.forClasspathResource(src, 777), dest);
        }
        if (secondsPerBlock != 0) {
            withCommand("-s " + secondsPerBlock);
        }
        waitingFor(Wait.forListeningPort());
    }

    public String getNodeUrl() {
        return "http://" + this.getContainerIpAddress() + ":" +
                this.getMappedPort(EXPOSED_JSONRPC_PORT);
    }

    /**
     * Deploys the contract with the given NEF. The NEF has to be available on the container
     *
     * @param nefFilePath The file path of the NEF.
     * @return The hash of the deployment transaction.
     * @throws Exception if the execution failed.
     */
    public String deployContract(String nefFilePath) throws Exception {
        Container.ExecResult execResult = execInContainer(
                "neoxp", "contract", "deploy", nefFilePath, "genesis");
        if (execResult.getExitCode() != 0) {
            throw new Exception("Failed executing command in container. Error was: \n " +
                    execResult.getStderr());
        }
        return execResult.getStdout().split(" ")[2];
    }

    /**
     * Executes a contract invocation according to the given invoke file. The invoke file has to be
     * available on the container.
     *
     * @param invokeFile The invoke file to use for the invocation.
     * @return The hash of the invocation transaction.
     * @throws Exception if the execution failed.
     */
    public String invokeContract(String invokeFile) throws Exception {
        Container.ExecResult execResult = execInContainer(
                "neoxp", "contract", "invoke", invokeFile, "genesis");
        if (execResult.getExitCode() != 0) {
            throw new Exception("Failed executing command in container. Error was: \n " +
                    execResult.getStderr());
        }
        return execResult.getStdout().split(" ")[2];
    }

    /**
     * Enables the oracle service on the neo-express instance
     *
     * @return The hash of the oracle designate transaction.
     * @throws Exception if the execution failed.
     */
    public String enableOracle() throws Exception {
        Container.ExecResult execResult = execInContainer("neoxp", "oracle", "enable", "genesis");
        if (execResult.getExitCode() != 0) {
            throw new Exception("Failed executing command in container. Error was: \n " +
                    execResult.getStderr());
        }
        return execResult.getStdout().split(" ")[3];
    }
}