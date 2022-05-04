package io.neow3j.test;

public interface TestBlockchain {

    /**
     * Sets the given block time to be used by the blockchain.
     *
     * @param secondsPerBlock the block time.
     * @return this.
     */
    TestBlockchain withSecondsPerBlock(int secondsPerBlock);

    /**
     * Adds the given batch file to the blockchain. A batch file specifies commands (e.g., token transfers or other
     * contract invocations) that set up the blockchain state for tests.
     * <p>
     * The batch file must be located in the resources directory.
     *
     * @param batchFile the batch file name.
     * @return this.
     */
    TestBlockchain withBatchFile(String batchFile);

    /**
     * Adds the given checkpoint file to the blockchain. A checkpoint specifies a pre-generated blockchain state that
     * is used to set up the blockchain to that state before tests.
     * <p>
     * The checkpoint file must be located in the resources directory.
     * <p>
     * If both a batch and a checkpoint file are added, the checkpoint is first applied.
     *
     * @param checkpointFile the checkpoint file name.
     * @return this.
     */
    TestBlockchain withCheckpoint(String checkpointFile);

    /**
     * Adds the given config file to the blockchain. It will be used to configure the blockchain instance.
     * <p>
     * The file must be located in the resources directory.
     *
     * @param configFile the config file name.
     * @return this.
     */
    TestBlockchain withConfigFile(String configFile);

    /**
     * @return the URL of the test blockchain node.
     */
    String getNodeUrl();

    /**
     * Resumes the blockchain if it was stopped before.
     *
     * @return the message emitted by the chain on startup or null if no message is emitted.
     * @throws Exception if an error occurred when trying to resume the blockchain.
     */
    String resume() throws Exception;

    /**
     * Halts the blockchain, i.e., stops block production.
     *
     * @return the message emitted by the chain on stopping or null if no message is emitted.
     * @throws Exception if an error occurred when trying to halt the blockchain.
     */
    String halt() throws Exception;

    /**
     * Creates a new account.
     *
     * @return the new account's address.
     * @throws Exception if an error occurred when trying to create an account.
     */
    String createAccount() throws Exception;

    /**
     * Enables the oracle service.
     *
     * @return the hash of the oracle designate transaction.
     * @throws Exception if an error occurred when trying to enable the oracle service.
     */
    String enableOracle() throws Exception;

    /**
     * Fast-forwards the blockchain state by {@code n} blocks. I.e., mints {@code n} empty blocks.
     *
     * @param n the number of blocks to mint.
     * @return the message emitted on minting the blocks or null if no message is emitted.
     * @throws Exception if an error occurred when trying to fast-forward the blockchain.
     */
    String fastForward(int n) throws Exception;

    /**
     * Mints {@code n} blocks with the last block being {@code seconds} in the future.
     *
     * @param seconds the time delta in seconds from now.
     * @param n       the number of blocks to mint.
     * @return the message emitted on minting the blocks.
     * @throws Exception if an error occurred when minting the blocks.
     */
    String fastForward(int seconds, int n) throws Exception;

    /**
     * Executes the given command.
     *
     * @param commandParts the command separated into its parts.
     * @return the message emitted on executing the command or null if no message is emitted.
     * @throws Exception if an error occurred when trying to execute the command on the blockchain.
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
     * This is for test blockchains that provide control over the genesis account. The genesis account owns all NEO
     * and GAS tokens starting from the first block.
     *
     * @return the genesis account.
     * @throws Exception if an error occurred when trying to fetch the genesis account.
     */
    GenesisAccount getGenesisAccount() throws Exception;

    /**
     * Gets the private key corresponding to the given address.
     * <p>
     * This is not meant to be used for multi-sig accounts.
     *
     * @param address the account's address.
     * @return the private key as a hexadecimal string.
     * @throws Exception if an error occurred when trying to fetch the account.
     */
    String getAccount(String address) throws Exception;

    class GenesisAccount {

        private String verificationScript;

        private String[] privateKeys;

        public GenesisAccount(String script, String... privateKeys) {
            this.verificationScript = script;
            this.privateKeys = privateKeys;
        }

        /**
         * @return the verification script of the genesis account.
         */
        public String getVerificationScript() {
            return verificationScript;
        }

        /**
         * @return the hexadecimal private keys involved in the genesis account.
         */
        public String[] getPrivateKeys() {
            return privateKeys;
        }
    }

}
