package io.neow3j.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;

public class NeoExpressTestContainer extends GenericContainer<NeoExpressTestContainer> implements TestBlockchain {

    public static final String DEFAULT_NEOXP_CONFIG = "default.neo-express";

    public static final String CONTAINER_WORKDIR = "/neoxp/";
    private static final String NEOXP_RUN_SCRIPT = CONTAINER_WORKDIR + "neoxp-run.sh";
    public static final String NEOXP_CONFIG_DEST = CONTAINER_WORKDIR + "default.neo-express";
    private static final String BATCH_FILE_DEST = CONTAINER_WORKDIR + "setup.batch";
    private static final String CHECKPOINT_FILE_DEST = CONTAINER_WORKDIR + "setup.neoxp-checkpoint";

    // This is the port of neo-express node which is exposed by the container.
    static final int EXPOSED_JSONRPC_PORT = 40332;

    static final ObjectMapper objectMapper = new ObjectMapper();

    // Can be set if the container is initialized with a specific block time.
    private int secondsPerBlock = 0;
    private String neoxpConfigFile;

    private int accountCtr = 0;

    /**
     * Creates a new instance of a docker container running a neo-express private network.
     *
     * @param resources the names of the files that should be copied into the container. For each file you need to
     *                  set a source and a destination (in the container) path consecutively.
     */
    public NeoExpressTestContainer(String... resources) {
        super(DockerImageName.parse(TestProperties.neoExpressDockerImage()));
        withExposedPorts(EXPOSED_JSONRPC_PORT);
        waitingFor(Wait.forListeningPort());

        int i = 0;
        while (resources != null && i + 1 < resources.length) {
            String src = resources[i++];
            String dest = resources[i++];
            withCopyFileToContainer(MountableFile.forClasspathResource(src, 777), dest);
        }
    }

    @Override
    public void start() {
        if (neoxpConfigFile == null) {
            this.neoxpConfigFile = DEFAULT_NEOXP_CONFIG;
            withCopyFileToContainer(MountableFile.forClasspathResource(this.neoxpConfigFile, 777), NEOXP_CONFIG_DEST);
        }
        super.start();
    }

    /**
     * Sets the given block time.
     *
     * @param secondsPerBlock the block time.
     * @return this.
     */
    @Override
    public NeoExpressTestContainer withSecondsPerBlock(int secondsPerBlock) {
        if (secondsPerBlock < 1) {
            throw new IllegalArgumentException("Seconds per block must be 1 or higher.");
        }
        withCommand("-s " + secondsPerBlock);
        this.secondsPerBlock = secondsPerBlock;
        return this;
    }

    /**
     * Adds the given batch file to the test container and executes it before the neo-express instance is started.
     * <p>
     * The batch file must be located in the resources directory.
     *
     * @param batchFile the batch file name.
     * @return this.
     */
    @Override
    public NeoExpressTestContainer withBatchFile(String batchFile) {
        withCopyFileToContainer(MountableFile.forClasspathResource(batchFile), BATCH_FILE_DEST);
        return this;
    }

    /**
     * Adds the given checkpoint file to the test container and executes it before the neo-express instance is started.
     * <p>
     * The file must be located in the resources directory.
     * <p>
     * If both a batch and a checkpoint file is added, the checkpoint is first applied.
     *
     * @param checkpointFile the checkpoint file name.
     * @return this.
     */
    @Override
    public NeoExpressTestContainer withCheckpoint(String checkpointFile) {
        withCopyFileToContainer(MountableFile.forClasspathResource(checkpointFile), CHECKPOINT_FILE_DEST);
        return this;
    }

    /**
     * Adds the given neo-express config file to the test container. It will be used to configure the neo-express
     * instance in the container.
     * <p>
     * The file must be located in the resources directory.
     *
     * @param configFile the config file name.
     * @return this.
     */
    @Override
    public NeoExpressTestContainer withConfigFile(String configFile) {
        this.neoxpConfigFile = configFile;
        InputStream s = NeoExpressTestContainer.class.getClassLoader().getResourceAsStream(configFile);
        NeoExpressConfig config;
        try {
            config = new ObjectMapper().readValue(s, NeoExpressConfig.class);
        } catch (IOException e) {
            throw new RuntimeException("Config file is not a valid neo-express configuration.", e);
        }
        if (config.getConsensusNodes().size() > 1) {
            throw new IllegalStateException("Can't handle multi-node neo-express setups.");
        }
        withClasspathResourceMapping(configFile, NEOXP_CONFIG_DEST, BindMode.READ_WRITE);
        return this;
    }

    /**
     * Adds the given NEF and manifest files to the test container. They can then be used with the neo-express
     * command {@code neoxp contract deploy}.
     * <p>
     * The files must be located in the resources directory.
     * <p>
     * The copied files in the container will have the same name as the given sources.
     *
     * @param nefFile      the NEF file name.
     * @param manifestFile the manifest file name.
     * @return this.
     */
    public NeoExpressTestContainer withNefAndManifestFiles(String nefFile, String manifestFile) {
        String nefFileName = nefFile;
        int nefFileNameIdx = nefFile.lastIndexOf('/');
        if (nefFileNameIdx > -1) {
           nefFileName = nefFile.substring(nefFileNameIdx+1);
        }
        String manifestFileName = manifestFile;
        int manifestFileNameIdx = manifestFile.lastIndexOf('/');
        if (manifestFileNameIdx > -1) {
            manifestFileName = manifestFile.substring(manifestFileNameIdx+1);
        }
        withCopyFileToContainer(MountableFile.forClasspathResource(nefFile, 777), CONTAINER_WORKDIR + nefFileName);
        withCopyFileToContainer(MountableFile.forClasspathResource(manifestFile, 777),
                CONTAINER_WORKDIR + manifestFileName);
        return this;
    }

    /**
     * Gets the URL of the neo-express node running in the container.
     *
     * @return the neo-express node URL.
     */
    @Override
    public String getNodeUrl() {
        return "http://" + this.getHost() + ":" + this.getMappedPort(EXPOSED_JSONRPC_PORT);
    }

    /**
     * Runs neo-express if it was stopped before.
     *
     * @return the message emitted by neo-express on startup.
     * @throws Exception if an error occurs while trying to start neo-express.
     */
    @Override
    public String resume() throws Exception {
        String cmd;
        if (secondsPerBlock != 0) {
            cmd = NEOXP_RUN_SCRIPT + " " + secondsPerBlock;
        } else {
            cmd = NEOXP_RUN_SCRIPT;
        }
        ExecResult execResult = execInContainer(cmd);
        if (execResult.getExitCode() != 0) {
            throw new Exception("Failed executing command in container. Error was: \n " + execResult.getStderr());
        }
        return execResult.getStdout();
    }

    /**
     * Stops the neo-express instance.
     *
     * @return the message emitted by neo-express on stopping.
     * @throws Exception if an error occurs when trying to stop neo-express.
     */
    @Override
    public String halt() throws Exception {
        ExecResult execResult = execInContainer("neoxp", "stop");
        if (execResult.getExitCode() != 0) {
            throw new Exception("Failed executing command in container. Error was: \n " + execResult.getStderr());
        }
        return execResult.getStdout();
    }

    /**
     * Creates a new account on the neo-express instance.
     *
     * @return the new account's address.
     * @throws Exception if an error occurs when trying to create the account.
     */
    @Override
    public String createAccount() throws Exception {
        ExecResult execResult = execInContainer("neoxp", "wallet", "create", "acc" + accountCtr++);
        if (execResult.getExitCode() != 0) {
            throw new Exception("Failed executing command in container. Error was: \n " + execResult.getStderr());
        }
        return execResult.getStdout().replaceAll(" ", "").split("\n")[1];
    }

    /**
     * Enables the oracle service on the neo-express instance
     *
     * @return the hash of the oracle designate transaction.
     * @throws Exception if the execution failed.
     */
    @Override
    public String enableOracle() throws Exception {
        ExecResult execResult = execInContainer("neoxp", "oracle", "enable", "genesis");
        if (execResult.getExitCode() != 0) {
            throw new Exception("Failed executing command in container. Error was: \n " + execResult.getStderr());
        }
        return execResult.getStdout().split(" ")[3];
    }

    /**
     * Fast-forwards the blockchain state by {@code n} blocks. I.e., mints {@code n} empty blocks. The block
     * timestamps are according to the current system time.
     *
     * @param n the number of blocks to mint.
     * @return the message emitted by neo-express on minting the blocks.
     * @throws Exception if the execution failed.
     */
    @Override
    public String fastForward(int n) throws Exception {
        ExecResult execResult = execInContainer("neoxp", "fastfwd", Integer.toString(n));
        if (execResult.getExitCode() != 0) {
            throw new Exception("Failed executing command in container. Error was: \n " + execResult.getStderr());
        }
        return execResult.getStdout();
    }

    /**
     * Mints {@code n} blocks with the last block being {@code seconds} in the future.
     *
     * @param seconds the time delta in seconds from now.
     * @param n       the number of blocks to mint.
     * @return the message emitted by neo-express on minting the blocks.
     * @throws Exception if the execution failed.
     */
    @Override
    public String fastForward(int seconds, int n) throws Exception {
        ExecResult execResult =
                execInContainer("neoxp", "fastfwd", "-t", Integer.toString(seconds), Integer.toString(n));
        if (execResult.getExitCode() != 0) {
            throw new Exception("Failed executing command in container. Error was: \n " + execResult.getStderr());
        }
        return execResult.getStdout();
    }

    /**
     * Executes the given command on the neo-express instance.
     * <p>
     * The command has to be provided in its separate parts, e.g., {@code "neoxp", "contract", "run", "NeoToken",
     * "balanceOf", "NM7Aky765FG8NhhwtxjXRx7jEL1cnw7PBP", "--account genesis"}
     *
     * @param commandParts the command separated into its parts.
     * @return the message emitted by neo-express on minting the blocks.
     * @throws Exception if the execution failed.
     */
    @Override
    public String execCommand(String... commandParts) throws Exception {
        ExecResult execResult = execInContainer(commandParts);
        if (execResult.getExitCode() != 0) {
            throw new Exception("Failed executing command in container. Error was: \n " + execResult.getStderr());
        }
        return execResult.getStdout();
    }

    @Override
    public String getAccount(String address) throws IOException {
        InputStream s = this.getClass().getClassLoader().getResourceAsStream(neoxpConfigFile);
        NeoExpressConfig config = objectMapper.readValue(s, NeoExpressConfig.class);

        Optional<NeoExpressConfig.Wallet.Account> acc = Stream.concat(
                        config.getConsensusNodes().stream().flatMap(n -> n.getWallet().getAccounts().stream()),
                        config.getWallets().stream().flatMap(w -> w.getAccounts().stream()))
                .filter(a -> a.getScriptHash().equals(address)).findFirst();

        if (!acc.isPresent()) {
            throw new IllegalArgumentException("Account with address '" + address + "' not found.");
        }
        if (acc.get().getPrivateKey() == null) {
            throw new IllegalStateException(
                    format("Private key for account with address '%s' not available.", address));
        }
        return acc.get().getPrivateKey();
    }

    @Override
    public GenesisAccount getGenesisAccount() throws IOException {
        InputStream s = this.getClass().getClassLoader().getResourceAsStream(neoxpConfigFile);
        NeoExpressConfig config = objectMapper.readValue(s, NeoExpressConfig.class);
        // We only deal with single node neo-express setups in the container, i.e., exactly one occurrence of a
        // consensus node entry is expected in the neoxp config.
        NeoExpressConfig.Wallet w = config.getConsensusNodes().get(0).getWallet();
        Optional<NeoExpressConfig.Wallet.Account> genesisAcc = w.getAccounts().stream()
                .filter(a -> a.getContract().getScript().length() >= 2 * 42).findFirst();
        if (!genesisAcc.isPresent()) {
            throw new IllegalStateException("Couldn't find genesis account in Neo Express config.");
        }
        return new GenesisAccount(genesisAcc.get().getContract().getScript(), genesisAcc.get().getPrivateKey());
    }

}
