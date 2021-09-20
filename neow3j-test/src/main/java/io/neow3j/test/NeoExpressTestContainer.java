package io.neow3j.test;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.math.BigInteger;

public class NeoExpressTestContainer extends GenericContainer<NeoExpressTestContainer> {

    private static final String IMAGE = "ghcr.io/neow3j/neow3j-test-docker:latest";
    public static final String CONTAINER_WORKDIR = "/neoxp/";
    private static final String NEOXP_RUN_SCRIPT = CONTAINER_WORKDIR + "neoxp-run.sh";

    public static final String NEOXP_CONFIG_FILE = "default.neo-express";
    public static final String CONFIG_DEST = CONTAINER_WORKDIR + NEOXP_CONFIG_FILE;

    // This is the port of neo-express node which is exposed by the container.
    static final int EXPOSED_JSONRPC_PORT = 40332;

    // Can be set if the container is initialized with a specific block time.
    private int secondsPerBlock = 0;

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
        super(DockerImageName.parse(IMAGE));
        withCopyFileToContainer(MountableFile.forClasspathResource(NEOXP_CONFIG_FILE, 777),
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
            this.secondsPerBlock = secondsPerBlock;
        }
        waitingFor(Wait.forListeningPort());
    }

    public String getNodeUrl() {
        return "http://" + this.getContainerIpAddress() + ":" +
                this.getMappedPort(EXPOSED_JSONRPC_PORT);
    }

    public String runExpress() throws Exception {
        String cmd;
        if (secondsPerBlock != 0) {
            cmd = NEOXP_RUN_SCRIPT + " " + secondsPerBlock;
        } else {
            cmd = NEOXP_RUN_SCRIPT;
        }
        ExecResult execResult = execInContainer(cmd);
        if (execResult.getExitCode() != 0) {
            throw new Exception("Failed executing command in container. Error was: \n " +
                    execResult.getStderr());
        }
        return execResult.getStdout();
    }

    public String stopExpress() throws Exception {
        ExecResult execResult = execInContainer("neoxp", "stop");
        if (execResult.getExitCode() != 0) {
            throw new Exception("Failed executing command in container. Error was: \n " +
                    execResult.getStderr());
        }
        return execResult.getStdout();
    }

    public String createAccount(String name) throws Exception {
        ExecResult execResult = execInContainer("neoxp", "wallet", "create", name);
        if (execResult.getExitCode() != 0) {
            throw new Exception("Failed executing command in container. Error was: \n " +
                    execResult.getStderr());
        }
        return execResult.getStdout().replaceAll(" ", "").split("\n")[1];
    }

    /**
     * Deploys the contract with the given NEF. The NEF has to be available on the container
     *
     * @param nefFilePath The file path of the NEF.
     * @return The hash of the deployment transaction.
     * @throws Exception if the execution failed.
     */
    public String deployContract(String nefFilePath) throws Exception {
        ExecResult execResult = execInContainer(
                "neoxp", "contract", "deploy", nefFilePath, "genesis");
        if (execResult.getExitCode() != 0) {
            throw new Exception("Failed executing command in container. Error was: \n " +
                    execResult.getStderr());
        }
        return execResult.getStdout().split(" ")[2];
    }

    /**
     * @param amount   The amount of assets to transfer.
     * @param asset    The asset to transfer. Can be a symbol, e.g., "NEO", or the hash of a
     *                 contract.
     * @param sender   The sender. Can be a name of a wallet, e.g., "genesis", or an address.
     * @param receiver The receiver. Can be a name of a wallet, e.g., "genesis", or an address.
     * @return The transaction hash of the transfer.
     * @throws Exception if an error occurs when executing the transfer on the neo-express instance.
     */
    public String transfer(BigInteger amount, String asset, String sender, String receiver)
            throws Exception {
        ExecResult execResult = execInContainer("neoxp", "transfer", amount.toString(), asset,
                sender, receiver);
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
        ExecResult execResult = execInContainer(
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
        ExecResult execResult = execInContainer("neoxp", "oracle", "enable", "genesis");
        if (execResult.getExitCode() != 0) {
            throw new Exception("Failed executing command in container. Error was: \n " +
                    execResult.getStderr());
        }
        return execResult.getStdout().split(" ")[3];
    }
}