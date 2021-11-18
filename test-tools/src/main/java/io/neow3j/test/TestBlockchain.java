package io.neow3j.test;

import java.util.List;

import static java.util.Arrays.asList;

public interface TestBlockchain {

    /**
     * Sets the given block time.
     *
     * @param secondsPerBlock The block time.
     * @return this.
     */
    TestBlockchain withSecondsPerBlock(int secondsPerBlock);

    /**
     * Adds the given batch file to the test container and executes it before the neo-express
     * instance is started.
     * The batch file must be located in the resources directory.
     *
     * @param batchFile The batch file name.
     * @return this.
     */
    TestBlockchain withBatchFile(String batchFile);

    /**
     * Adds the given checkpoint file to the test container and executes it before the neo-express
     * instance is started.
     * The file must be located in the resources directory.
     * <p>
     * If both a batch and a checkpoint file is added, the checkpoint is first applied.
     *
     * @param checkpointFile The checkpoint file name.
     * @return this.
     */
    TestBlockchain withCheckpoint(String checkpointFile);

    /**
     * Adds the given neo-express config file to the test container. It will be used to configure
     * the neo-express instance in the container.
     * The file must be located in the resources directory.
     *
     * @param configFile The config file name.
     * @return this.
     */
    TestBlockchain withNeoxpConfig(String configFile);

    /**
     * Adds the given NEF and manifest files to the test container. They can then be used with the
     * neo-express command {@code neoxp contract deploy}.
     * The files must be located in the resources directory.
     * <p>
     * The copied files in the container will have the same name as the given sources.
     *
     * @param nefFile      The NEF file name.
     * @param manifestFile The manifest file name.
     * @return this.
     */
    TestBlockchain withNefAndManifestFiles(String nefFile, String manifestFile);

    /**
     * Gets the URL of the neo-express node running in the container.
     *
     * @return the neo-express node URL.
     */
    String getNodeUrl();

    /**
     * Resumes the blockchain if it was stopped before.
     *
     * @return The message emitted by the chain on startup.
     */
    String resume() throws Exception;

    /**
     * Halts the blockchain, i.e., stops block production.
     *
     * @return The message emitted by the chain on stopping.
     */
    String halt() throws Exception;

    /**
     * Creates a new account with the given name.
     *
     * @param name The name of the account to create.
     * @return The new account's address.
     */
    String createAccount(String name) throws Exception;

    /**
     * Enables the oracle service.
     *
     * @return The hash of the oracle designate transaction.
     */
    String enableOracle() throws Exception;

    /**
     * Fast-forwards the blockchain state by {@code n} blocks. I.e., mints {@code n} empty blocks.
     *
     * @param n The number of blocks to mint.
     * @return The message emitted on minting the blocks.
     */
    String fastForward(int n) throws Exception;

    /**
     * Executes the given command.
     *
     * @param commandParts The command separated into its parts.
     * @return The message emitted on executing the command.
     */
    String execCommand(String... commandParts) throws Exception;

    /**
     * Starts the blockchain, i.e., the process that includes the blockchain.
     */
    void start();

    /**
     * Stops the blockchain, i.e., the process that includes the blockchain.
     */
    void stop();

    /**
     * Gets the genesis account this blockchain is based on.
     *
     * @return the genesis account.
     */
    GenesisAccount getGenesisAccount() throws Exception;

    /**
     * Gets the private key corresponding to the given address.
     * <p>
     * This is not meant to be used for multi-sig accounts.
     *
     * @param address The account's address.
     * @return the private key as a hexadecimal string.
     */
    String getAccount(String address) throws Exception;

    class GenesisAccount {

        private String verificationScript;

        private List<String> privateKeys;

        public GenesisAccount(String script, String... privateKeys) {
            this.verificationScript = script;
            this.privateKeys = asList(privateKeys);
        }

        /**
         * Gets the verification script of the genesis account.
         *
         * @return the script.
         */
        public String getVerificationScript() {
            return verificationScript;
        }

        /**
         * Gets the hexadecimal private keys involved in the genesis account.
         *
         * @return the private keys.
         */
        public List<String> getPrivateKeys() {
            return privateKeys;
        }
    }
}
