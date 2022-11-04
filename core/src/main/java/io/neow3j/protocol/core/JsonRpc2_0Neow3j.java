package io.neow3j.protocol.core;

import io.neow3j.crypto.Base64;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.Neow3jConfig;
import io.neow3j.protocol.Neow3jService;
import io.neow3j.protocol.core.response.NeoBlockCount;
import io.neow3j.protocol.core.response.NeoBlockHash;
import io.neow3j.protocol.core.response.NeoBlockHeaderCount;
import io.neow3j.protocol.core.response.NeoCalculateNetworkFee;
import io.neow3j.protocol.core.response.NeoCloseWallet;
import io.neow3j.protocol.core.response.NeoConnectionCount;
import io.neow3j.protocol.core.response.NeoDumpPrivKey;
import io.neow3j.protocol.core.response.NeoFindStates;
import io.neow3j.protocol.core.response.NeoGetApplicationLog;
import io.neow3j.protocol.core.response.NeoGetBlock;
import io.neow3j.protocol.core.response.NeoGetCommittee;
import io.neow3j.protocol.core.response.NeoGetContractState;
import io.neow3j.protocol.core.response.NeoGetMemPool;
import io.neow3j.protocol.core.response.NeoGetNativeContracts;
import io.neow3j.protocol.core.response.NeoGetNep11Balances;
import io.neow3j.protocol.core.response.NeoGetNep11Properties;
import io.neow3j.protocol.core.response.NeoGetNep11Transfers;
import io.neow3j.protocol.core.response.NeoGetNep17Balances;
import io.neow3j.protocol.core.response.NeoGetNep17Transfers;
import io.neow3j.protocol.core.response.NeoGetNewAddress;
import io.neow3j.protocol.core.response.NeoGetNextBlockValidators;
import io.neow3j.protocol.core.response.NeoGetPeers;
import io.neow3j.protocol.core.response.NeoGetProof;
import io.neow3j.protocol.core.response.NeoGetRawBlock;
import io.neow3j.protocol.core.response.NeoGetRawMemPool;
import io.neow3j.protocol.core.response.NeoGetRawTransaction;
import io.neow3j.protocol.core.response.NeoGetState;
import io.neow3j.protocol.core.response.NeoGetStateHeight;
import io.neow3j.protocol.core.response.NeoGetStateRoot;
import io.neow3j.protocol.core.response.NeoGetStorage;
import io.neow3j.protocol.core.response.NeoGetTransaction;
import io.neow3j.protocol.core.response.NeoGetTransactionHeight;
import io.neow3j.protocol.core.response.NeoGetUnclaimedGas;
import io.neow3j.protocol.core.response.NeoGetVersion;
import io.neow3j.protocol.core.response.NeoGetWalletBalance;
import io.neow3j.protocol.core.response.NeoGetWalletUnclaimedGas;
import io.neow3j.protocol.core.response.NeoImportPrivKey;
import io.neow3j.protocol.core.response.NeoInvokeContractVerify;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.response.NeoInvokeScript;
import io.neow3j.protocol.core.response.NeoListAddress;
import io.neow3j.protocol.core.response.NeoListPlugins;
import io.neow3j.protocol.core.response.NeoOpenWallet;
import io.neow3j.protocol.core.response.NeoSendFrom;
import io.neow3j.protocol.core.response.NeoSendMany;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.response.NeoSendToAddress;
import io.neow3j.protocol.core.response.NeoSubmitBlock;
import io.neow3j.protocol.core.response.NeoTerminateSession;
import io.neow3j.protocol.core.response.NeoTraverseIterator;
import io.neow3j.protocol.core.response.NeoValidateAddress;
import io.neow3j.protocol.core.response.NeoVerifyProof;
import io.neow3j.protocol.core.response.TransactionSendToken;
import io.neow3j.protocol.core.response.TransactionSigner;
import io.neow3j.protocol.rx.JsonRpc2_0Rx;
import io.neow3j.transaction.Signer;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.reactivex.Observable;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;

/**
 * JSON-RPC 2.0 factory implementation.
 */
public class JsonRpc2_0Neow3j extends Neow3j {

    protected final Neow3jService neow3jService;
    private final JsonRpc2_0Rx neow3jRx;

    public JsonRpc2_0Neow3j(Neow3jService neow3jService, Neow3jConfig config) {
        super(config);
        this.neow3jService = neow3jService;
        this.neow3jRx = new JsonRpc2_0Rx(this, getScheduledExecutorService());
    }

    // region Blockchain Methods

    /**
     * Gets the hash of the latest block in the blockchain.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoBlockHash> getBestBlockHash() {
        return new Request<>(
                "getbestblockhash",
                emptyList(),
                neow3jService,
                NeoBlockHash.class);
    }

    /**
     * Gets the block hash of the corresponding block based on the specified block index.
     *
     * @param blockIndex the block index.
     * @return the request object.
     */
    @Override
    public Request<?, NeoBlockHash> getBlockHash(BigInteger blockIndex) {
        return new Request<>(
                "getblockhash",
                asList(blockIndex),
                neow3jService,
                NeoBlockHash.class);
    }

    /**
     * Gets the corresponding block information according to the specified block hash.
     *
     * @param blockHash                    the block hash.
     * @param returnFullTransactionObjects whether to get block information with all transaction objects or just the
     *                                     block header.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetBlock> getBlock(Hash256 blockHash,
            boolean returnFullTransactionObjects) {
        if (returnFullTransactionObjects) {
            return new Request<>(
                    "getblock",
                    asList(blockHash, 1),
                    neow3jService,
                    NeoGetBlock.class);
        } else {
            return getBlockHeader(blockHash);
        }
    }

    /**
     * Gets the corresponding block information for the specified block hash.
     *
     * @param blockHash the block hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetRawBlock> getRawBlock(Hash256 blockHash) {
        return new Request<>(
                "getblock",
                asList(blockHash, 0),
                neow3jService,
                NeoGetRawBlock.class);
    }

    /**
     * Gets the corresponding block information according to the specified block index.
     *
     * @param blockIndex                   the block index.
     * @param returnFullTransactionObjects whether to get block information with all transaction objects or just the
     *                                     block header.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetBlock> getBlock(BigInteger blockIndex,
            boolean returnFullTransactionObjects) {
        if (returnFullTransactionObjects) {
            return new Request<>(
                    "getblock",
                    asList(blockIndex, 1),
                    neow3jService,
                    NeoGetBlock.class);
        } else {
            return getBlockHeader(blockIndex);
        }
    }

    /**
     * Gets the corresponding block information according to the specified block index.
     *
     * @param blockIndex the block index.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetRawBlock> getRawBlock(BigInteger blockIndex) {
        return new Request<>(
                "getblock",
                asList(blockIndex, 0),
                neow3jService,
                NeoGetRawBlock.class);
    }

    /**
     * Gets the block header count of the blockchain.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoBlockHeaderCount> getBlockHeaderCount() {
        return new Request<>(
                "getblockheadercount",
                emptyList(),
                neow3jService,
                NeoBlockHeaderCount.class);
    }

    /**
     * Gets the block count of the blockchain.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoBlockCount> getBlockCount() {
        return new Request<>(
                "getblockcount",
                emptyList(),
                neow3jService,
                NeoBlockCount.class);
    }

    /**
     * Gets the corresponding block header information according to the specified block hash.
     *
     * @param blockHash the block hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetBlock> getBlockHeader(Hash256 blockHash) {
        return new Request<>(
                "getblockheader",
                asList(blockHash, 1),
                neow3jService,
                NeoGetBlock.class);
    }

    /**
     * Gets the corresponding block header information according to the specified index.
     *
     * @param blockIndex the block index.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetBlock> getBlockHeader(BigInteger blockIndex) {
        return new Request<>(
                "getblockheader",
                asList(blockIndex, 1),
                neow3jService,
                NeoGetBlock.class);
    }

    /**
     * Gets the corresponding block header information according to the specified block hash.
     *
     * @param blockHash the block hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetRawBlock> getRawBlockHeader(Hash256 blockHash) {
        return new Request<>(
                "getblockheader",
                asList(blockHash, 0),
                neow3jService,
                NeoGetRawBlock.class);
    }

    /**
     * Gets the corresponding block header information according to the specified block index.
     *
     * @param blockIndex the block index.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetRawBlock> getRawBlockHeader(BigInteger blockIndex) {
        return new Request<>(
                "getblockheader",
                asList(blockIndex, 0),
                neow3jService,
                NeoGetRawBlock.class);
    }

    /**
     * Gets the native contracts list, which includes the basic information of native contracts and the contract
     * descriptive file {@code manifest.json}.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetNativeContracts> getNativeContracts() {
        return new Request<>(
                "getnativecontracts",
                emptyList(),
                neow3jService,
                NeoGetNativeContracts.class);
    }

    /**
     * Gets the contract information.
     *
     * @param contractHash the contract script hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetContractState> getContractState(Hash160 contractHash) {
        return new Request<>(
                "getcontractstate",
                asList(contractHash),
                neow3jService,
                NeoGetContractState.class);
    }

    /**
     * Gets the native contract information by its name.
     * <p>
     * This RPC only works for native contracts.
     *
     * @param nativeContractName the name of the native contract.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetContractState> getNativeContractState(String nativeContractName) {
        return new Request<>(
                "getcontractstate",
                asList(nativeContractName),
                neow3jService,
                NeoGetContractState.class);
    }

    /**
     * Gets a list of unconfirmed or confirmed transactions in memory.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetMemPool> getMemPool() {
        return new Request<>(
                "getrawmempool",
                asList(1),
                neow3jService,
                NeoGetMemPool.class);
    }

    /**
     * Gets a list of confirmed transactions in memory.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetRawMemPool> getRawMemPool() {
        return new Request<>(
                "getrawmempool",
                emptyList(),
                neow3jService,
                NeoGetRawMemPool.class);
    }

    /**
     * Gets the corresponding transaction information based on the specified transaction hash.
     *
     * @param txHash the transaction hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetTransaction> getTransaction(Hash256 txHash) {
        return new Request<>(
                "getrawtransaction",
                asList(txHash, 1),
                neow3jService,
                NeoGetTransaction.class);
    }

    /**
     * Gets the corresponding transaction information based on the specified transaction hash.
     *
     * @param txHash the transaction hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetRawTransaction> getRawTransaction(Hash256 txHash) {
        return new Request<>(
                "getrawtransaction",
                asList(txHash, 0),
                neow3jService,
                NeoGetRawTransaction.class);
    }

    /**
     * Gets the stored value according to the contract hash and the key.
     *
     * @param contractHash the contract hash.
     * @param keyHexString the key to look up in storage as a hexadecimal string.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetStorage> getStorage(Hash160 contractHash, String keyHexString) {
        return new Request<>(
                "getstorage",
                asList(contractHash, Base64.encode(keyHexString)),
                neow3jService,
                NeoGetStorage.class);
    }

    /**
     * Gets the transaction height with the specified transaction hash.
     *
     * @param txHash the transaction hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetTransactionHeight> getTransactionHeight(Hash256 txHash) {
        return new Request<>(
                "gettransactionheight",
                asList(txHash),
                neow3jService,
                NeoGetTransactionHeight.class);
    }

    /**
     * Gets the validators of the next block.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetNextBlockValidators> getNextBlockValidators() {
        return new Request<>(
                "getnextblockvalidators",
                emptyList(),
                neow3jService,
                NeoGetNextBlockValidators.class);
    }

    /**
     * Gets the public key list of current Neo committee members.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetCommittee> getCommittee() {
        return new Request<>(
                "getcommittee",
                emptyList(),
                neow3jService,
                NeoGetCommittee.class);
    }

    // endregion Blockchain Methods
    // region Node Methods

    /**
     * Gets the current number of connections for the node.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoConnectionCount> getConnectionCount() {
        return new Request<>(
                "getconnectioncount",
                emptyList(),
                neow3jService,
                NeoConnectionCount.class);
    }

    /**
     * Gets a list of nodes that the node is currently connected or disconnected from.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetPeers> getPeers() {
        return new Request<>(
                "getpeers",
                emptyList(),
                neow3jService,
                NeoGetPeers.class);
    }

    /**
     * Gets the version information of the node.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetVersion> getVersion() {
        return new Request<>(
                "getversion",
                emptyList(),
                neow3jService,
                NeoGetVersion.class);
    }

    /**
     * Broadcasts a transaction over the NEO network.
     *
     * @param rawTransactionHex the raw transaction in hexadecimal.
     * @return the request object.
     */
    @Override
    public Request<?, NeoSendRawTransaction> sendRawTransaction(String rawTransactionHex) {
        return new Request<>(
                "sendrawtransaction",
                asList(Base64.encode(rawTransactionHex)),
                neow3jService,
                NeoSendRawTransaction.class);
    }

    /**
     * Broadcasts a new block over the NEO network.
     *
     * @param serializedBlockAsHex the block in hexadecimal.
     * @return the request object.
     */
    @Override
    public Request<?, NeoSubmitBlock> submitBlock(String serializedBlockAsHex) {
        return new Request<>(
                "submitblock",
                asList(serializedBlockAsHex),
                neow3jService,
                NeoSubmitBlock.class);
    }

    // endregion Node Methods
    // region SmartContract Methods

    /**
     * Invokes the function with {@code functionName} of the smart contract with the specified contract hash.
     *
     * @param contractHash the contract hash to invoke.
     * @param functionName the function to invoke.
     * @param signers      the signers.
     * @return the request object.
     */
    @Override
    public Request<?, NeoInvokeFunction> invokeFunction(Hash160 contractHash, String functionName, Signer... signers) {
        return invokeFunction(contractHash, functionName, asList(), signers);
    }

    /**
     * Invokes the function with {@code functionName} of the smart contract with the specified contract hash.
     *
     * @param contractHash   the contract hash to invoke.
     * @param functionName   the function to invoke.
     * @param contractParams the parameters of the function.
     * @param signers        the signers.
     * @return the request object.
     */
    @Override
    public Request<?, NeoInvokeFunction> invokeFunction(Hash160 contractHash, String functionName,
            List<ContractParameter> contractParams, Signer... signers) {

        List<TransactionSigner> txSigners = stream(signers)
                .map(TransactionSigner::new)
                .collect(Collectors.toList());
        return new Request<>(
                "invokefunction",
                asList(contractHash, functionName, contractParams, txSigners),
                neow3jService,
                NeoInvokeFunction.class);
    }

    /**
     * Invokes the function with {@code functionName} of the smart contract with the specified contract hash.
     * <p>
     * Includes diagnostics from the invocation.
     *
     * @param contractHash   the contract hash to invoke.
     * @param functionName   the function to invoke.
     * @param signers        the signers.
     * @return the request object.
     */
    @Override
    public Request<?, NeoInvokeFunction> invokeFunctionDiagnostics(Hash160 contractHash, String functionName,
            Signer... signers) {
        return invokeFunctionDiagnostics(contractHash, functionName, asList(), signers);
    }

    /**
     * Invokes the function with {@code functionName} of the smart contract with the specified contract hash.
     * <p>
     * Includes diagnostics from the invocation.
     *
     * @param contractHash   the contract hash to invoke.
     * @param functionName   the function to invoke.
     * @param contractParams the parameters of the function.
     * @param signers        the signers.
     * @return the request object.
     */
    @Override
    public Request<?, NeoInvokeFunction> invokeFunctionDiagnostics(Hash160 contractHash, String functionName,
            List<ContractParameter> contractParams, Signer... signers) {

        List<TransactionSigner> txSigners = stream(signers)
                .map(TransactionSigner::new)
                .collect(Collectors.toList());
        return new Request<>(
                "invokefunction",
                asList(contractHash, functionName, contractParams, txSigners, true),
                neow3jService,
                NeoInvokeFunction.class);
    }

    /**
     * Invokes a script.
     *
     * @param scriptHex the script to invoke.
     * @param signers   the signers.
     * @return the request object.
     */
    @Override
    public Request<?, NeoInvokeScript> invokeScript(String scriptHex, Signer... signers) {
        String scriptBase64 = Base64.encode(scriptHex);
        List<TransactionSigner> signersList = stream(signers).map(TransactionSigner::new).collect(Collectors.toList());
        return new Request<>(
                "invokescript",
                asList(scriptBase64, signersList),
                neow3jService,
                NeoInvokeScript.class);
    }

    /**
     * Invokes a script.
     * <p>
     * Includes diagnostics from the invocation.
     *
     * @param scriptHex the script to invoke.
     * @param signers   the signers.
     * @return the request object.
     */
    @Override
    public Request<?, NeoInvokeScript> invokeScriptDiagnostics(String scriptHex, Signer... signers) {
        String scriptBase64 = Base64.encode(scriptHex);
        List<TransactionSigner> signersList = stream(signers).map(TransactionSigner::new).collect(Collectors.toList());
        return new Request<>(
                "invokescript",
                asList(scriptBase64, signersList, true),
                neow3jService,
                NeoInvokeScript.class);
    }

    /**
     * Returns the results from an iterator.
     * <p>
     * The results are limited to {@code count} items. If {@code count} is greater than {@code MaxIteratorResultItems}
     * in the Neo Node's configuration file, this request fails.
     * <p>
     * By sending the same request multiple times, the iterator advances and returns the remaining results. For
     * example, in case there are more than {@code count} items, sending the same request multiple times allows to
     * retrive all stack items in the iterator.
     *
     * @param sessionId  the session id.
     * @param iteratorId the iterator id.
     * @param count      the maximal number of stack items returned.
     * @return the request object.
     */
    @Override
    public Request<?, NeoTraverseIterator> traverseIterator(String sessionId, String iteratorId, int count) {
        return new Request<>(
                "traverseiterator",
                asList(sessionId, iteratorId, count),
                neow3jService,
                NeoTraverseIterator.class);
    }

    /**
     * Terminates an open session.
     *
     * @param sessionId the session id.
     * @return the request object.
     */
    @Override
    public Request<?, NeoTerminateSession> terminateSession(String sessionId) {
        return new Request<>(
                "terminatesession",
                asList(sessionId),
                neow3jService,
                NeoTerminateSession.class);
    }

    /**
     * Invokes a contract in verification mode.
     * <p>
     * Requires an open wallet on the Neo node that contains the accounts for the signers.
     *
     * @param contractHash the contract hash.
     * @param methodParams a list of parameters of the verify function.
     * @param signers      the signers.
     * @return the request object.
     */
    @Override
    public Request<?, NeoInvokeContractVerify> invokeContractVerify(Hash160 contractHash,
            List<ContractParameter> methodParams, Signer... signers) {

        if (methodParams == null) {
            methodParams = new ArrayList<>();
        }
        List<TransactionSigner> txSigners = stream(signers)
                .map(TransactionSigner::new)
                .collect(Collectors.toList());
        return new Request<>(
                "invokecontractverify",
                asList(contractHash, methodParams, txSigners),
                neow3jService,
                NeoInvokeContractVerify.class);
    }

    /**
     * Gets the unclaimed GAS of the account with the specified script hash.
     *
     * @param scriptHash the account's script hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetUnclaimedGas> getUnclaimedGas(Hash160 scriptHash) {
        return new Request<>(
                "getunclaimedgas",
                asList(scriptHash.toAddress()),
                neow3jService,
                NeoGetUnclaimedGas.class);
    }

    // endregion SmartContract Methods
    // region Utilities Methods

    /**
     * Gets a list of plugins loaded by the node.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoListPlugins> listPlugins() {
        return new Request<>(
                "listplugins",
                emptyList(),
                neow3jService,
                NeoListPlugins.class);
    }

    /**
     * Verifies whether the address is a valid NEO address.
     *
     * @param address the address to verify.
     * @return the request object.
     */
    @Override
    public Request<?, NeoValidateAddress> validateAddress(String address) {
        return new Request<>(
                "validateaddress",
                asList(address),
                neow3jService,
                NeoValidateAddress.class);
    }

    // endregion Utilities Methods
    // region Wallet Methods

    /**
     * Closes the current wallet.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoCloseWallet> closeWallet() {
        return new Request<>(
                "closewallet",
                emptyList(),
                neow3jService,
                NeoCloseWallet.class);
    }

    /**
     * Exports the private key of the specified script hash.
     *
     * @param scriptHash the account's script hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoDumpPrivKey> dumpPrivKey(Hash160 scriptHash) {
        return new Request<>(
                "dumpprivkey",
                asList(scriptHash.toAddress()),
                neow3jService,
                NeoDumpPrivKey.class);
    }

    /**
     * Gets the wallet balance of the corresponding token.
     *
     * @param tokenHash the token hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetWalletBalance> getWalletBalance(Hash160 tokenHash) {
        return new Request<>(
                "getwalletbalance",
                asList(tokenHash),
                neow3jService,
                NeoGetWalletBalance.class);
    }

    /**
     * Creates a new address.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetNewAddress> getNewAddress() {
        return new Request<>(
                "getnewaddress",
                emptyList(),
                neow3jService,
                NeoGetNewAddress.class);
    }

    /**
     * Gets the amount of unclaimed GAS in the wallet.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetWalletUnclaimedGas> getWalletUnclaimedGas() {
        return new Request<>(
                "getwalletunclaimedgas",
                emptyList(),
                neow3jService,
                NeoGetWalletUnclaimedGas.class);
    }

    /**
     * Imports a private key to the wallet.
     *
     * @param privateKeyInWIF the private key in WIF-format.
     * @return the request object.
     */
    @Override
    public Request<?, NeoImportPrivKey> importPrivKey(String privateKeyInWIF) {
        return new Request<>(
                "importprivkey",
                asList(privateKeyInWIF),
                neow3jService,
                NeoImportPrivKey.class);
    }

    /**
     * Calculates the network fee for the specified transaction.
     *
     * @param txHex the transaction in hexadecimal.
     * @return the request object.
     */
    @Override
    public Request<?, NeoCalculateNetworkFee> calculateNetworkFee(String txHex) {
        return new Request<>(
                "calculatenetworkfee",
                asList(Base64.encode(txHex)),
                neow3jService,
                NeoCalculateNetworkFee.class);
    }

    /**
     * Lists all the addresses in the current wallet.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoListAddress> listAddress() {
        return new Request<>(
                "listaddress",
                emptyList(),
                neow3jService,
                NeoListAddress.class);
    }

    /**
     * Opens the specified wallet.
     *
     * @param walletPath the wallet file path.
     * @param password   the password for the wallet.
     * @return the request object.
     */
    @Override
    public Request<?, NeoOpenWallet> openWallet(String walletPath, String password) {
        return new Request<>(
                "openwallet",
                asList(walletPath, password),
                neow3jService,
                NeoOpenWallet.class);
    }

    /**
     * Transfers an amount of a token from an account to another account.
     *
     * @param tokenHash the token hash of the NEP-17 contract.
     * @param from      the transferring account's script hash.
     * @param to        the recipient.
     * @param amount    the transfer amount in token fractions.
     * @return the request object.
     */
    @Override
    public Request<?, NeoSendFrom> sendFrom(Hash160 tokenHash, Hash160 from, Hash160 to, BigInteger amount) {
        return new Request<>(
                "sendfrom",
                asList(tokenHash, from.toAddress(), to.toAddress(), amount),
                neow3jService,
                NeoSendFrom.class);
    }

    /**
     * Transfers an amount of a token from an account to another account.
     *
     * @param from        the transferring account's script hash.
     * @param txSendToken a {@link TransactionSendToken} object containing the token hash, the transferring account's
     *                    script hash and the transfer amount.
     * @return the request object.
     */
    @Override
    public Request<?, NeoSendFrom> sendFrom(Hash160 from, TransactionSendToken txSendToken) {
        return sendFrom(txSendToken.getToken(), from, Hash160.fromAddress(txSendToken.getAddress()),
                txSendToken.getValue());
    }

    /**
     * Initiates multiple transfers to multiple accounts from the open wallet in a transaction.
     *
     * @param txSendTokens a list of {@link TransactionSendToken} objects, that each contains the token hash, the
     *                     recipient and the transfer amount.
     * @return the request object.
     */
    @Override
    public Request<?, NeoSendMany> sendMany(List<TransactionSendToken> txSendTokens) {
        return new Request<>(
                "sendmany",
                asList(txSendTokens.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())),
                neow3jService,
                NeoSendMany.class);
    }

    /**
     * Initiates multiple transfers to multiple accounts from one specific account in a transaction.
     *
     * @param from         the transferring account's script hash.
     * @param txSendTokens a list of {@link TransactionSendToken} objects, that each contains the token hash, the
     *                     recipient and the transfer amount.
     * @return the request object.
     */
    @Override
    public Request<?, NeoSendMany> sendMany(Hash160 from, List<TransactionSendToken> txSendTokens) {
        return new Request<>(
                "sendmany",
                asList(from.toAddress(),
                        txSendTokens.stream()
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())),
                neow3jService,
                NeoSendMany.class);
    }

    /**
     * Transfers an amount of a token to another account.
     *
     * @param tokenHash the token hash of the NEP-17 contract.
     * @param to        the recipient.
     * @param amount    the transfer amount in token fractions.
     * @return the request object.
     */
    @Override
    public Request<?, NeoSendToAddress> sendToAddress(Hash160 tokenHash, Hash160 to, BigInteger amount) {
        return new Request<>(
                "sendtoaddress",
                Stream.of(tokenHash, to.toAddress(), amount)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()),
                neow3jService,
                NeoSendToAddress.class);
    }

    /**
     * Transfers an amount of a token asset to another address.
     *
     * @param txSendToken a {@link TransactionSendToken} object containing the token hash, the recipient and the
     *                    transfer amount.
     * @return the request object.
     */
    @Override
    public Request<?, NeoSendToAddress> sendToAddress(TransactionSendToken txSendToken) {
        return sendToAddress(txSendToken.getToken(), Hash160.fromAddress(txSendToken.getAddress()),
                txSendToken.getValue());
    }

    // endregion Wallet Methods
    // region ApplicationLogs

    /**
     * Gets the application logs of the specified transaction hash.
     *
     * @param txHash the transaction hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetApplicationLog> getApplicationLog(Hash256 txHash) {
        return new Request<>(
                "getapplicationlog",
                asList(txHash),
                neow3jService,
                NeoGetApplicationLog.class);
    }

    // endregion ApplicationLogs
    // region TokenTracker NEP-17

    /**
     * Gets the balance of all NEP-17 token assets in the specified script hash.
     *
     * @param scriptHash the account's script hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetNep17Balances> getNep17Balances(Hash160 scriptHash) {
        return new Request<>(
                "getnep17balances",
                asList(scriptHash.toAddress()),
                neow3jService,
                NeoGetNep17Balances.class);
    }

    /**
     * Gets all the NEP-17 transaction information occurred in the specified script hash.
     *
     * @param scriptHash the account's script hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetNep17Transfers> getNep17Transfers(Hash160 scriptHash) {
        return new Request<>(
                "getnep17transfers",
                asList(scriptHash.toAddress()),
                neow3jService,
                NeoGetNep17Transfers.class);
    }

    /**
     * Gets all the NEP17 transaction information occurred in the specified script hash since the specified time.
     *
     * @param scriptHash the account's script hash.
     * @param from       the timestamp transactions occurred since.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetNep17Transfers> getNep17Transfers(Hash160 scriptHash, Date from) {
        return new Request<>(
                "getnep17transfers",
                asList(scriptHash.toAddress(), from.getTime()),
                neow3jService,
                NeoGetNep17Transfers.class);
    }

    /**
     * Gets all the NEP17 transaction information occurred in the specified script hash in the specified time range.
     *
     * @param scriptHash the account's script hash.
     * @param from       the start timestamp.
     * @param until      the end timestamp.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetNep17Transfers> getNep17Transfers(Hash160 scriptHash, Date from, Date until) {
        return new Request<>(
                "getnep17transfers",
                asList(scriptHash.toAddress(), from.getTime(), until.getTime()),
                neow3jService,
                NeoGetNep17Transfers.class);
    }

    // endregion TokenTracker NEP-17
    // region TokenTracker NEP-11

    /**
     * Gets all NEP-11 balances of the specified account.
     *
     * @param scriptHash the account's script hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetNep11Balances> getNep11Balances(Hash160 scriptHash) {
        return new Request<>(
                "getnep11balances",
                asList(scriptHash.toAddress()),
                neow3jService,
                NeoGetNep11Balances.class);
    }

    /**
     * Gets all NEP-11 transaction of the given account.
     *
     * @param scriptHash the account's script hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetNep11Transfers> getNep11Transfers(Hash160 scriptHash) {
        return new Request<>(
                "getnep11transfers",
                asList(scriptHash.toAddress()),
                neow3jService,
                NeoGetNep11Transfers.class);
    }

    /**
     * Gets all NEP-11 transaction of the given account since the given time.
     *
     * @param scriptHash the account's script hash.
     * @param from       the date from when to report transactions.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetNep11Transfers> getNep11Transfers(Hash160 scriptHash, Date from) {
        return new Request<>(
                "getnep11transfers",
                asList(scriptHash.toAddress(), from.getTime()),
                neow3jService,
                NeoGetNep11Transfers.class);
    }

    /**
     * Gets all NEP-11 transactions of the given account in the time span between {@code from} and {@code to}.
     *
     * @param scriptHash the account's script hash.
     * @param from       the start timestamp.
     * @param to         the end timestamp.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetNep11Transfers> getNep11Transfers(Hash160 scriptHash, Date from, Date to) {
        return new Request<>(
                "getnep11transfers",
                asList(scriptHash.toAddress(), from.getTime(), to.getTime()),
                neow3jService,
                NeoGetNep11Transfers.class);
    }

    /**
     * Gets the properties of the token with {@code tokenId} from the NEP-11 contract with {@code scriptHash}.
     * <p>
     * The properties are a mapping from the property name string to the value string. The value is plain text if the
     * key is one of the properties defined in the NEP-11 standard. Otherwise, the value is a Base64-encoded byte array.
     * <p>
     * To receive custom property values that consist of nested types (e.g., Maps or Arrays) use {@link #invokeFunction}
     * to directly invoke the method {@code properties} of the NEP-11 smart contract.
     *
     * @param scriptHash the script hash of the token contract.
     * @param tokenId    the ID of the token as a hexadecimal string.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetNep11Properties> getNep11Properties(Hash160 scriptHash, String tokenId) {
        return new Request<>(
                "getnep11properties",
                asList(scriptHash.toAddress(), tokenId),
                neow3jService,
                NeoGetNep11Properties.class);
    }

    // endregion TokenTracker NEP-11
    // region StateService

    /**
     * Gets the state root by the block height.
     *
     * @param blockIndex the block index.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetStateRoot> getStateRoot(Long blockIndex) {
        return new Request<>(
                "getstateroot",
                asList(blockIndex),
                neow3jService,
                NeoGetStateRoot.class);
    }

    /**
     * Gets the proof based on the root hash, the contract hash and the storage key.
     *
     * @param rootHash      the root hash.
     * @param contractHash  the contract hash.
     * @param storageKeyHex the storage key.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetProof> getProof(Hash256 rootHash, Hash160 contractHash, String storageKeyHex) {
        return new Request<>(
                "getproof",
                asList(rootHash, contractHash, Base64.encode(storageKeyHex)),
                neow3jService,
                NeoGetProof.class);
    }

    /**
     * Verifies the proof data and gets the value of the storage corresponding to the key.
     *
     * @param rootHash     the root hash.
     * @param proofDataHex the proof data of the state root.
     * @return the request object.
     */
    @Override
    public Request<?, NeoVerifyProof> verifyProof(Hash256 rootHash, String proofDataHex) {
        return new Request<>(
                "verifyproof",
                asList(rootHash, Base64.encode(proofDataHex)),
                neow3jService,
                NeoVerifyProof.class);
    }

    /**
     * Gets the state root height.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetStateHeight> getStateHeight() {
        return new Request<>(
                "getstateheight",
                emptyList(),
                neow3jService,
                NeoGetStateHeight.class);
    }

    /**
     * Gets the state.
     *
     * @param rootHash     the root hash.
     * @param contractHash the contract hash.
     * @param keyHex       the storage key.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetState> getState(Hash256 rootHash, Hash160 contractHash, String keyHex) {
        return new Request<>(
                "getstate",
                asList(rootHash, contractHash, Base64.encode(keyHex)),
                neow3jService,
                NeoGetState.class);
    }

    /**
     * Gets a list of states that match the provided key prefix.
     * <p>
     * Includes proofs of the first and last entry.
     *
     * @param rootHash             the root hash.
     * @param contractHash         the contract hash.
     * @param keyPrefixHex         the key prefix.
     * @param startKeyHex          the start key.
     * @param countFindResultItems the number of results. An upper limit is defined in the Neo core.
     * @return the request object.
     */
    @Override
    public Request<?, NeoFindStates> findStates(Hash256 rootHash, Hash160 contractHash, String keyPrefixHex,
            String startKeyHex, Integer countFindResultItems) {

        List<Comparable<? extends Comparable<?>>> parameters =
                asList(rootHash, contractHash, Base64.encode(keyPrefixHex));
        if (startKeyHex != null && countFindResultItems != null) {
            parameters = asList(rootHash, contractHash, Base64.encode(keyPrefixHex), Base64.encode(startKeyHex),
                    countFindResultItems);
        } else if (countFindResultItems != null) {
            parameters = asList(rootHash, contractHash, Base64.encode(keyPrefixHex), "", countFindResultItems);
        } else if (startKeyHex != null) {
            parameters = asList(rootHash, contractHash, Base64.encode(keyPrefixHex), Base64.encode(startKeyHex));
        }

        return new Request<>(
                "findstates",
                parameters,
                neow3jService,
                NeoFindStates.class);
    }

    /**
     * Gets a list of states that match the provided key prefix.
     * <p>
     * Includes proofs of the first and last entry.
     *
     * @param rootHash     the root hash.
     * @param contractHash the contract hash.
     * @param keyPrefixHex the key prefix.
     * @param startKeyHex  the start key.
     * @return the request object.
     */
    @Override
    public Request<?, NeoFindStates> findStates(Hash256 rootHash, Hash160 contractHash, String keyPrefixHex,
            String startKeyHex) {
        return findStates(rootHash, contractHash, keyPrefixHex, startKeyHex, null);
    }

    /**
     * Gets a list of states that match the provided key prefix.
     * <p>
     * Includes proofs of the first and last entry.
     *
     * @param rootHash             the root hash.
     * @param contractHash         the contract hash.
     * @param keyPrefixHex         the key prefix.
     * @param countFindResultItems the number of results. An upper limit is defined in the Neo core.
     * @return the request object.
     */
    @Override
    public Request<?, NeoFindStates> findStates(Hash256 rootHash, Hash160 contractHash, String keyPrefixHex,
            Integer countFindResultItems) {
        return findStates(rootHash, contractHash, keyPrefixHex, null, countFindResultItems);
    }

    /**
     * Gets a list of states that match the provided key prefix.
     * <p>
     * Includes proofs of the first and last entry.
     *
     * @param rootHash     the root hash.
     * @param contractHash the contract hash.
     * @param keyPrefixHex the key prefix.
     * @return the request object.
     */
    @Override
    public Request<?, NeoFindStates> findStates(Hash256 rootHash, Hash160 contractHash, String keyPrefixHex) {
        return findStates(rootHash, contractHash, keyPrefixHex, null, null);
    }

    // endregion TokenTracker NEP-11
    // region Neow3j Rx Convenience Methods

    @Override
    public Observable<NeoGetBlock> blockObservable(boolean fullTransactionObjects) {
        return neow3jRx.blockObservable(fullTransactionObjects, getPollingInterval());
    }

    @Override
    public Observable<NeoGetBlock> replayBlocksObservable(BigInteger startBlock, BigInteger endBlock,
            boolean fullTransactionObjects) {

        return neow3jRx.replayBlocksObservable(startBlock, endBlock, fullTransactionObjects, true);
    }

    @Override
    public Observable<NeoGetBlock> replayBlocksObservable(BigInteger startBlock, BigInteger endBlock,
            boolean fullTransactionObjects, boolean ascending) {

        return neow3jRx.replayBlocksObservable(startBlock, endBlock, fullTransactionObjects, ascending);
    }

    @Override
    public Observable<NeoGetBlock> catchUpToLatestBlockObservable(BigInteger startBlock, boolean fullTransactionObjects,
            Observable<NeoGetBlock> onCompleteObservable) {

        return neow3jRx.catchUpToLatestBlockObservable(startBlock, fullTransactionObjects, onCompleteObservable);
    }

    @Override
    public Observable<NeoGetBlock> catchUpToLatestBlockObservable(BigInteger startBlock,
            boolean fullTransactionObjects) {

        return neow3jRx.catchUpToLatestBlockObservable(startBlock, fullTransactionObjects, Observable.empty());
    }

    @Override
    public Observable<NeoGetBlock> catchUpToLatestAndSubscribeToNewBlocksObservable(BigInteger startBlock,
            boolean fullTransactionObjects) {

        return neow3jRx.catchUpToLatestAndSubscribeToNewBlocksObservable(startBlock, fullTransactionObjects,
                getPollingInterval());
    }

    @Override
    public Observable<NeoGetBlock> subscribeToNewBlocksObservable(boolean fullTransactionObjects) {
        return neow3jRx.blockObservable(fullTransactionObjects, getPollingInterval());
    }

    @Override
    public void shutdown() {
        getScheduledExecutorService().shutdown();
        try {
            neow3jService.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to close neow3j service", e);
        }
    }

    // endregion Neow3j Rx Convenience Methods

}
