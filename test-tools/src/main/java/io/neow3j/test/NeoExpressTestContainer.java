package io.neow3j.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Arrays.asList;

public class NeoExpressTestContainer extends GenericContainer<NeoExpressTestContainer> implements TestBlockchain {

    public static final String NEOXP_COMMAND = "neoxp";
    private static final String NEOXP_INPUT_FLAG = "--input";

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
            nefFileName = nefFile.substring(nefFileNameIdx + 1);
        }
        String manifestFileName = manifestFile;
        int manifestFileNameIdx = manifestFile.lastIndexOf('/');
        if (manifestFileNameIdx > -1) {
            manifestFileName = manifestFile.substring(manifestFileNameIdx + 1);
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
        List<String> cmds = new ArrayList<>();
        if (secondsPerBlock != 0) {
            cmds.add(NEOXP_RUN_SCRIPT);
            cmds.add(String.valueOf(secondsPerBlock));
        } else {
            cmds.add(NEOXP_RUN_SCRIPT);
        }
        return execCommand(cmds.toArray(new String[0]));
    }

    /**
     * Stops the neo-express instance.
     *
     * @return the message emitted by neo-express on stopping.
     * @throws Exception if an error occurs when trying to stop neo-express.
     */
    @Override
    public String halt() throws Exception {
        return execNeoxpCommandWithDefaultConfig("stop");
    }

    /**
     * Creates a new account on the neo-express instance.
     *
     * @return the new account's address.
     * @throws Exception if an error occurs when trying to create the account.
     */
    @Override
    public String createAccount() throws Exception {
        String resultString = execNeoxpCommandWithDefaultConfig("wallet", "create", "acc" + accountCtr++);
        Pattern pattern = Pattern.compile("Address:\\s[0-9A-Za-z&&[^0OIl]]+");
        Matcher matcher = pattern.matcher(resultString);
        if (!matcher.find()) {
            throw new IllegalStateException("Couldn't extract address from result string: " + resultString);
        }
        String match = matcher.group(0);
        // Ignores the first 9 chars that match the regex expression (i.e., "Address: ") to produce the address string.
        return match.substring(9);
    }

    /**
     * Enables the oracle service on the neo-express instance
     *
     * @return the hash of the oracle designate transaction.
     * @throws Exception if the execution failed.
     */
    @Override
    public String enableOracle() throws Exception {
        String execResult = execNeoxpCommandWithDefaultConfig("oracle", "enable", "genesis");
        return execResult.split(" ")[3];
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
        return execNeoxpCommandWithDefaultConfig("fastfwd", Integer.toString(n));
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
        return execNeoxpCommandWithDefaultConfig("fastfwd", "-t", Integer.toString(seconds), Integer.toString(n));
    }

    /**
     * Executes the given command in the test container.
     * <p>
     * The command has to be provided in separate parts, e.g., {@code "neoxp", "contract", "run", "NeoToken",
     * "balanceOf", "NM7Aky765FG8NhhwtxjXRx7jEL1cnw7PBP", "--account", "genesis"}
     * <p>
     * If the command requires the default neo-express config file path as input, use
     * {@link #execNeoxpCommandWithDefaultConfig(String...)}.
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

    /**
     * Executes a {@code neoxp} command with the given command parts using the test container's default neo-express
     * file path as input to the command.
     * <p>
     * The command has to be provided in separate parts, e.g., {@code "contract", "run", "NeoToken", "balanceOf",
     * "NM7Aky765FG8NhhwtxjXRx7jEL1cnw7PBP", "--account", "genesis"}
     * <p>
     * If a <i>--input</i> flag is provided in the command parts, its subsequent command is replaced with the default
     * neo-express file path used in the test container, i.e., {@code /neoxp/default.neo-express}.
     * <p>
     * If the command does not support the <i>--input</i> flag, or a different neo-express config file should be used,
     * use {@link #execCommand(String...)} either without the <i>--input</i> flag or with it and with the path pointing
     * to the neo-express file to be used, respectively.
     *
     * @param commandParts the command separated into its parts.
     * @return the message emitted by neo-express on minting the blocks.
     * @throws Exception if the execution failed.
     */
    @Override
    public String execNeoxpCommandWithDefaultConfig(String... commandParts) throws Exception {
        ArrayList<String> commandList = new ArrayList<>(asList(commandParts));
        addNeoxpCommand(commandList);
        addDefaultConfigInput(commandList);
        return execCommand(commandList.toArray(new String[0]));
    }

    /**
     * Makes sure that the command list contains the <i>--input</i> flag followed by the test container's default
     * neo-express file path.
     * <p>
     * If the <i>--input</i> flag does not exist in the command list, it is added followed by the test container's
     * default neo-express file path. Otherwise, its subsequent command is replaced with the default neo-express file
     * path.
     *
     * @param commandList the command list.
     */
    static void addDefaultConfigInput(ArrayList<String> commandList) {
        if (!commandList.contains(NEOXP_INPUT_FLAG)) {
            commandList.add(NEOXP_INPUT_FLAG);
            commandList.add(NEOXP_CONFIG_DEST);
            return;
        }
        int configPathIndex = commandList.indexOf(NEOXP_INPUT_FLAG) + 1;
        commandList.remove(configPathIndex);
        commandList.add(configPathIndex, NEOXP_CONFIG_DEST);
    }

    /**
     * Makes sure the command list starts with the {@code neoxp} command.
     *
     * @param commandList the command list.
     */
    static void addNeoxpCommand(ArrayList<String> commandList) {
        if (!commandList.get(0).equals(NEOXP_COMMAND)) {
            commandList.add(0, NEOXP_COMMAND);
        }
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
