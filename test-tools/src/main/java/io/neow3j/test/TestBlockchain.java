package io.neow3j.test;

import java.util.List;

import static java.util.Arrays.asList;

public interface TestBlockchain {

    /**
     * Sets the given block time to be used by the blockchain.
     *
     * @param secondsPerBlock The block time.
     * @return this.
     */
    TestBlockchain withSecondsPerBlock(int secondsPerBlock);

    /**
     * Adds the given batch file to the blockchain. A batch file specifies commands (e.g., token
     * transfers or other contract invocations) that set up the blockchain state for tests.
     * <p>
     * The batch file must be located in the resources directory.
     *
     * @param batchFile The batch file name.
     * @return this.
     */
    TestBlockchain withBatchFile(String batchFile);

    /**
     * Adds the given checkpoint file to the blockchain. A checkpoint specifies a pre-generated
     * blockchain state that is used to set up the blockchain to that state before tests.
     * <p>
     * The checkpoint file must be located in the resources directory.
     * <p>
     * If both a batch and a checkpoint file are added, the checkpoint is first applied.
     *
     * @param checkpointFile The checkpoint file name.
     * @return this.
     */
    TestBlockchain withCheckpoint(String checkpointFile);

    /**
     * Adds the given config file to the blockchain. It will be used to configure the blockchain
     * instance.
     * <p>
     * The file must be located in the resources directory.
     *
     * @param configFile The config file name.
     * @return this.
     */
    TestBlockchain withConfigFile(String configFile);

    /**
     * Gets the URL of the test blockchain node.
     *
     * @return the node URL.
     */
    String getNodeUrl();

    /**
     * Resumes the blockchain if it was stopped before.
     *
     * @return The message emitted by the chain on startup or null if no message is emitted.
     */
    String resume() throws Exception;

    /**
     * Halts the blockchain, i.e., stops block production.
     *
     * @return The message emitted by the chain on stopping or null if no message is emitted.
     */
    String halt() throws Exception;

    /**
     * Creates a new account.
     *
     * @return The new account's address.
     */
    String createAccount() throws Exception;

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
     * @return The message emitted on minting the blocks or null if no message is emitted.
     */
    String fastForward(int n) throws Exception;

    /**
     * Executes the given command.
     *
     * @param commandParts The command separated into its parts.
     * @return The message emitted on executing the command or null if no message is emitted.
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
     * <p>
     * This is for test blockchains that provide control over the genesis account. The genesis
     * account owns all NEO and GAS tokens starting from the first block.
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