package io.neow3j.protocol.core;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import io.neow3j.contract.ContractParameter;
import io.neow3j.contract.Hash160;
import io.neow3j.contract.Hash256;
import io.neow3j.crypto.Base64;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.Neow3jService;
import io.neow3j.protocol.core.methods.response.NeoBlockCount;
import io.neow3j.protocol.core.methods.response.NeoBlockHash;
import io.neow3j.protocol.core.methods.response.NeoBlockHeaderCount;
import io.neow3j.protocol.core.methods.response.NeoCalculateNetworkFee;
import io.neow3j.protocol.core.methods.response.NeoCloseWallet;
import io.neow3j.protocol.core.methods.response.NeoConnectionCount;
import io.neow3j.protocol.core.methods.response.NeoDumpPrivKey;
import io.neow3j.protocol.core.methods.response.NeoGetApplicationLog;
import io.neow3j.protocol.core.methods.response.NeoGetBlock;
import io.neow3j.protocol.core.methods.response.NeoGetCommittee;
import io.neow3j.protocol.core.methods.response.NeoGetContractState;
import io.neow3j.protocol.core.methods.response.NeoGetMemPool;
import io.neow3j.protocol.core.methods.response.NeoGetNativeContracts;
import io.neow3j.protocol.core.methods.response.NeoGetNep17Balances;
import io.neow3j.protocol.core.methods.response.NeoGetNep17Transfers;
import io.neow3j.protocol.core.methods.response.NeoGetNewAddress;
import io.neow3j.protocol.core.methods.response.NeoGetPeers;
import io.neow3j.protocol.core.methods.response.NeoGetProof;
import io.neow3j.protocol.core.methods.response.NeoGetRawBlock;
import io.neow3j.protocol.core.methods.response.NeoGetRawMemPool;
import io.neow3j.protocol.core.methods.response.NeoGetRawTransaction;
import io.neow3j.protocol.core.methods.response.NeoGetStateHeight;
import io.neow3j.protocol.core.methods.response.NeoGetStateRoot;
import io.neow3j.protocol.core.methods.response.NeoGetStorage;
import io.neow3j.protocol.core.methods.response.NeoGetTransaction;
import io.neow3j.protocol.core.methods.response.NeoGetTransactionHeight;
import io.neow3j.protocol.core.methods.response.NeoGetUnclaimedGas;
import io.neow3j.protocol.core.methods.response.NeoGetNextBlockValidators;
import io.neow3j.protocol.core.methods.response.NeoGetVersion;
import io.neow3j.protocol.core.methods.response.NeoGetWalletBalance;
import io.neow3j.protocol.core.methods.response.NeoGetWalletUnclaimedGas;
import io.neow3j.protocol.core.methods.response.NeoImportPrivKey;
import io.neow3j.protocol.core.methods.response.NeoInvokeContractVerify;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.NeoInvokeScript;
import io.neow3j.protocol.core.methods.response.NeoListAddress;
import io.neow3j.protocol.core.methods.response.NeoListPlugins;
import io.neow3j.protocol.core.methods.response.NeoOpenWallet;
import io.neow3j.protocol.core.methods.response.NeoSendFrom;
import io.neow3j.protocol.core.methods.response.NeoSendMany;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.methods.response.NeoSendToAddress;
import io.neow3j.protocol.core.methods.response.NeoSubmitBlock;
import io.neow3j.protocol.core.methods.response.NeoValidateAddress;
import io.neow3j.protocol.core.methods.response.NeoVerifyProof;
import io.neow3j.protocol.core.methods.response.TransactionSendAsset;
import io.neow3j.protocol.core.methods.response.TransactionSigner;
import io.neow3j.protocol.rx.JsonRpc2_0Rx;
import io.neow3j.transaction.Signer;
import io.neow3j.utils.Async;
import io.neow3j.utils.Numeric;
import io.reactivex.Observable;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * JSON-RPC 2.0 factory implementation.
 */
public class JsonRpc2_0Neow3j extends Neow3j {

    public static final int DEFAULT_BLOCK_TIME = 15 * 1000;

    protected final Neow3jService neow3jService;
    private final JsonRpc2_0Rx neow3jRx;
    private final long blockTime;
    private final ScheduledExecutorService scheduledExecutorService;

    public JsonRpc2_0Neow3j(Neow3jService neow3jService) {
        this(neow3jService, DEFAULT_BLOCK_TIME, Async.defaultExecutorService());
    }

    public JsonRpc2_0Neow3j(
            Neow3jService neow3jService, long pollingInterval,
            ScheduledExecutorService scheduledExecutorService) {
        this.neow3jService = neow3jService;
        this.neow3jRx = new JsonRpc2_0Rx(this, scheduledExecutorService);
        this.blockTime = pollingInterval;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    // Blockchain Methods

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
     * Gets the block hash of the corresponding block based on the specified
     * index.
     *
     * @param blockIndex the block index.
     * @return the request object.
     */
    @Override
    public Request<?, NeoBlockHash> getBlockHash(BigInteger blockIndex) {
        return new Request<>(
                "getblockhash",
                singletonList(blockIndex),
                neow3jService,
                NeoBlockHash.class);
    }

    /**
     * Gets the corresponding block information according to the specified hash
     * or index.
     *
     * @param blockHash                    the block hash.
     * @param returnFullTransactionObjects whether to get block information with all transaction
     *                                     objects or just the block header.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetBlock> getBlock(String blockHash,
            boolean returnFullTransactionObjects) {
        return getBlock(new Hash256(blockHash), returnFullTransactionObjects);
    }

    /**
     * Gets the corresponding block information according to the specified hash
     * or index.
     *
     * @param blockHash                    the block hash.
     * @param returnFullTransactionObjects whether to get block information with all transaction
     *                                     objects or just the block header.
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
     * Gets the corresponding block information according to the specified hash
     * in hexadecimal.
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
     * Gets the corresponding block information according to the specified hash
     * in hexadecimal.
     *
     * @param blockHash the block hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetRawBlock> getRawBlock(String blockHash) {
        return getRawBlock(new Hash256(blockHash));
    }

    /**
     * Gets the corresponding block information according to the specified
     * index.
     *
     * @param blockIndex                   the block index.
     * @param returnFullTransactionObjects whether to get block information with all transaction
     *                                     objects or just the block header.
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
     * Gets the corresponding block information according to the specified hash
     * in hexadecimal.
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
     * Gets the corresponding block header information according to the
     * specified hash.
     *
     * @param hash the block hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetBlock> getBlockHeader(Hash256 hash) {
        return new Request<>(
                "getblockheader",
                asList(hash, 1),
                neow3jService,
                NeoGetBlock.class);
    }

    /**
     * Gets the corresponding block header information according to the
     * specified hash.
     *
     * @param hash the block hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetBlock> getBlockHeader(String hash) {
        return getBlockHeader(new Hash256(hash));
    }

    /**
     * Gets the corresponding block header information according to the
     * specified index.
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
     * Gets the corresponding block header information according to the
     * specified hash in hexadecimal.
     *
     * @param hash the block hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetRawBlock> getRawBlockHeader(Hash256 hash) {
        return new Request<>(
                "getblockheader",
                asList(hash, 0),
                neow3jService,
                NeoGetRawBlock.class);
    }

    /**
     * Gets the corresponding block header information according to the
     * specified hash in hexadecimal.
     *
     * @param hash the block hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetRawBlock> getRawBlockHeader(String hash) {
        return getRawBlockHeader(new Hash256(hash));
    }

    /**
     * Gets the corresponding block header information according to the
     * specified script hash in hexadecimal.
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
     * Gets the native contracts list, which includes the basic information of native contracts
     * and the contract descriptive file manifest.json.
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
     * @param hash160 the contract script hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetContractState> getContractState(Hash160 hash160) {
        return new Request<>(
                "getcontractstate",
                singletonList(hash160),
                neow3jService,
                NeoGetContractState.class);
    }

    /**
     * Gets the contract information.
     *
     * @param contractName the name of the contract or its script hash as a String.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetContractState> getContractState(String contractName) {
        return new Request<>(
                "getcontractstate",
                singletonList(contractName),
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
                singletonList(1),
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
     * Gets the corresponding transaction information based on the specified
     * hash value.
     *
     * @param txId the transaction id.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetTransaction> getTransaction(String txId) {
        return getTransaction(new Hash256(txId));
    }

    /**
     * Gets the corresponding transaction information based on the specified
     * hash value.
     *
     * @param txId the transaction id.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetTransaction> getTransaction(Hash256 txId) {
        return new Request<>(
                "getrawtransaction",
                asList(txId, 1),
                neow3jService,
                NeoGetTransaction.class);
    }

    /**
     * Gets the corresponding transaction information based on the specified
     * hash value in hexadecimal.
     *
     * @param txId the transaction id.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetRawTransaction> getRawTransaction(String txId) {
        return getRawTransaction(new Hash256(txId));
    }

    /**
     * Gets the corresponding transaction information based on the specified
     * hash value in hexadecimal.
     *
     * @param txId the transaction id.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetRawTransaction> getRawTransaction(Hash256 txId) {
        return new Request<>(
                "getrawtransaction",
                asList(txId, 0),
                neow3jService,
                NeoGetRawTransaction.class);
    }

    /**
     * Gets the stored value according to the contract script hash and the key.
     *
     * @param contractAddress the contract hash.
     * @param keyHexString    the key to look up in storage as a hexadecimal string.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetStorage> getStorage(String contractAddress, String keyHexString) {
        return getStorage(new Hash160(contractAddress), keyHexString);
    }

    /**
     * Gets the stored value according to the contract script hash and the key.
     *
     * @param contractScriptHash the contract hash.
     * @param keyHexString       the key to look up in storage as a hexadecimal string.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetStorage> getStorage(Hash160 contractScriptHash, String keyHexString) {
        return new Request<>(
                "getstorage",
                asList(contractScriptHash, Base64.encode(keyHexString)),
                neow3jService,
                NeoGetStorage.class);
    }

    /**
     * Gets the transaction height with the specified transaction hash.
     *
     * @param txId the transaction id.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetTransactionHeight> getTransactionHeight(String txId) {
        return getTransactionHeight(new Hash256(txId));
    }

    /**
     * Gets the transaction height with the specified transaction hash.
     *
     * @param txId the transaction id.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetTransactionHeight> getTransactionHeight(Hash256 txId) {
        return new Request<>(
                "gettransactionheight",
                singletonList(txId),
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

    // Node Methods

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
     * Gets a list of nodes that the node is currently connected or
     * disconnected from.
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
                singletonList(Base64.encode(Numeric.hexStringToByteArray(rawTransactionHex))),
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
                singletonList(serializedBlockAsHex),
                neow3jService,
                NeoSubmitBlock.class);
    }

    // SmartContract Methods

    /**
     * Invokes the function with {@code functionName} of the smart contract with the given script
     * hash.
     *
     * @param contractScriptHash the contract script hash to invoke.
     * @param functionName       the function to invoke.
     * @param signers            the signers.
     * @return the request object.
     */
    @Override
    public Request<?, NeoInvokeFunction> invokeFunction(String contractScriptHash,
            String functionName, Signer... signers) {
        return invokeFunction(new Hash160(contractScriptHash), functionName, null, signers);
    }

    /**
     * Invokes the function with {@code functionName} of the smart contract with the given script
     * hash.
     *
     * @param contractScriptHash the contract script hash to invoke.
     * @param functionName       the function to invoke.
     * @param signers            the signers.
     * @return the request object.
     */
    @Override
    public Request<?, NeoInvokeFunction> invokeFunction(Hash160 contractScriptHash,
            String functionName, Signer... signers) {
        return invokeFunction(contractScriptHash, functionName, null, signers);
    }

    /**
     * Invokes the function with {@code functionName} of the smart contract with the given script
     * hash.
     *
     * @param contractScriptHash the contract script hash to invoke.
     * @param functionName       the function to invoke.
     * @param contractParams     the parameters of the function.
     * @param signers            the signers.
     * @return the request object.
     */
    @Override
    public Request<?, NeoInvokeFunction> invokeFunction(String contractScriptHash,
            String functionName, List<ContractParameter> contractParams, Signer... signers) {
        return invokeFunction(new Hash160(contractScriptHash), functionName, contractParams,
                signers);
    }

    /**
     * Invokes the function with {@code functionName} of the smart contract with the given script
     * hash.
     *
     * @param contractScriptHash the contract script hash to invoke.
     * @param functionName       the function to invoke.
     * @param contractParams     the parameters of the function.
     * @param signers            the signers.
     * @return the request object.
     */
    @Override
    public Request<?, NeoInvokeFunction> invokeFunction(Hash160 contractScriptHash,
            String functionName, List<ContractParameter> contractParams, Signer... signers) {

        if (contractParams == null) {
            contractParams = new ArrayList<>();
        }
        List<TransactionSigner> txSigners = stream(signers).map(TransactionSigner::new)
                .collect(Collectors.toList());
        List<?> params;
        if (txSigners.size() > 0) {
            params = asList(contractScriptHash, functionName, contractParams, txSigners);
        } else {
            params = asList(contractScriptHash, functionName, contractParams);
        }
        return new Request<>(
                "invokefunction",
                params.stream().filter(Objects::nonNull).collect(Collectors.toList()),
                neow3jService,
                NeoInvokeFunction.class);
    }

    /**
     * Invokes a script.
     *
     * @param script  the script to invoke.
     * @param signers the signers.
     * @return the request object.
     */
    @Override
    public Request<?, NeoInvokeScript> invokeScript(String script, Signer... signers) {
        List<?> params;
        if (signers.length > 0) {
            params = asList(script, stream(signers)
                    .map(TransactionSigner::new)
                    .collect(Collectors.toList()));
        } else {
            params = singletonList(script);
        }
        return new Request<>(
                "invokescript",
                params,
                neow3jService,
                NeoInvokeScript.class);
    }

    /**
     * Invokes a contract in verification mode.
     * <p>
     * Requires an open wallet on the neo-node that contains the accounts for
     * the signers.
     *
     * @param scriptHash   the contract script hash.
     * @param methodParams a list of parameters of the verify function.
     * @param signers      the signers.
     * @return the request object.
     */
    @Override
    public Request<?, NeoInvokeContractVerify> invokeContractVerify(String scriptHash,
            List<ContractParameter> methodParams, Signer... signers) {
        return invokeContractVerify(new Hash160(scriptHash), methodParams, signers);
    }

    /**
     * Invokes a contract in verification mode.
     * <p>
     * Requires an open wallet on the neo-node that contains the accounts for
     * the signers.
     *
     * @param scriptHash   the contract script hash.
     * @param methodParams a list of parameters of the verify function.
     * @param signers      the signers.
     * @return the request object.
     */
    @Override
    public Request<?, NeoInvokeContractVerify> invokeContractVerify(Hash160 scriptHash,
            List<ContractParameter> methodParams, Signer... signers) {

        if (methodParams == null) {
            methodParams = new ArrayList<>();
        }
        List<TransactionSigner> txSigners = stream(signers)
                .map(TransactionSigner::new).collect(Collectors.toList());
        List<?> params = asList(scriptHash, methodParams, txSigners);
        return new Request<>(
                "invokecontractverify",
                params.stream().filter(Objects::nonNull).collect(Collectors.toList()),
                neow3jService,
                NeoInvokeContractVerify.class);
    }

    /**
     * Gets the unclaimed GAS of the specified address.
     *
     * @param address the address.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetUnclaimedGas> getUnclaimedGas(String address) {
        return new Request<>(
                "getunclaimedgas",
                singletonList(address),
                neow3jService,
                NeoGetUnclaimedGas.class);
    }

    // Utilities Methods

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
                singletonList(address),
                neow3jService,
                NeoValidateAddress.class);
    }

    // Wallet Methods

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
     * Exports the private key of the specified address.
     *
     * @param address the address.
     * @return the request object.
     */
    @Override
    public Request<?, NeoDumpPrivKey> dumpPrivKey(String address) {
        return new Request<>(
                "dumpprivkey",
                singletonList(address),
                neow3jService,
                NeoDumpPrivKey.class);
    }

    /**
     * Gets the balance of the corresponding asset in the wallet.
     *
     * @param assetId the asset id.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetWalletBalance> getWalletBalance(String assetId) {
        return getWalletBalance(new Hash160(assetId));
    }

    /**
     * Gets the balance of the corresponding asset in the wallet.
     *
     * @param assetId the asset id.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetWalletBalance> getWalletBalance(Hash160 assetId) {
        return new Request<>(
                "getwalletbalance",
                singletonList(assetId),
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
                singletonList(privateKeyInWIF),
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
                singletonList(Base64.encode(Numeric.hexStringToByteArray(txHex))),
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
     * Transfers an amount of an asset from an address to another address.
     *
     * @param fromAddress the transferring address.
     * @param assetId     the script hash of the NEP17 contract.
     * @param toAddress   the destination address.
     * @param value       the transfer amount.
     * @return the request object.
     */
    @Override
    public Request<?, NeoSendFrom> sendFrom(String fromAddress, String assetId,
            String toAddress, String value) {
        return sendFrom(fromAddress, new Hash160(assetId), toAddress, value);
    }

    /**
     * Transfers an amount of an asset from an address to another address.
     *
     * @param fromAddress the transferring address.
     * @param assetId     the script hash of the NEP17 contract.
     * @param toAddress   the destination address.
     * @param value       the transfer amount.
     * @return the request object.
     */
    @Override
    public Request<?, NeoSendFrom> sendFrom(String fromAddress, Hash160 assetId,
            String toAddress, String value) {
        return new Request<>(
                "sendfrom",
                asList(assetId, fromAddress, toAddress, value),
                neow3jService,
                NeoSendFrom.class);
    }

    /**
     * Transfers an amount of an asset from an address to another address.
     *
     * @param fromAddress the transferring address.
     * @param txSendAsset a {@code TransactionSendAsset} object containing the
     *                    asset, the destination address and the transfer
     *                    amount.
     * @return the request object.
     */
    @Override
    public Request<?, NeoSendFrom> sendFrom(String fromAddress, TransactionSendAsset txSendAsset) {
        return sendFrom(fromAddress, txSendAsset.getAsset(), txSendAsset.getAddress(),
                txSendAsset.getValue());
    }

    /**
     * Initiates multiple transfers to multiple addresses from the open wallet
     * in a transaction.
     *
     * @param txSendAsset a list of {@code TransactoinSendAsset} objects, that
     *                    each contains the asset, destination address and
     *                    transfer amount.
     * @return the request object.
     */
    @Override
    public Request<?, NeoSendMany> sendMany(List<TransactionSendAsset> txSendAsset) {
        return new Request<>(
                "sendmany",
                singletonList(txSendAsset.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())),
                neow3jService,
                NeoSendMany.class);
    }

    /**
     * Initiates multiple transfers to multiple addresses from one specific
     * address in a transaction.
     *
     * @param fromAddress the transferring address.
     * @param txSendAsset a list of {@code TransactionSendAsset} objects, that
     *                    each contains the asset, destination address and
     *                    transfer amount.
     * @return the request object.
     */
    @Override
    public Request<?, NeoSendMany> sendMany(String fromAddress,
            List<TransactionSendAsset> txSendAsset) {

        return new Request<>(
                "sendmany",
                asList(fromAddress, txSendAsset.stream().filter(Objects::nonNull)
                        .collect(Collectors.toList())),
                neow3jService,
                NeoSendMany.class);
    }

    /**
     * Transfers an amount of an asset to another address.
     *
     * @param assetId   the script hash of the NEP17 contract.
     * @param toAddress the destination address.
     * @param value     the transfer amount.
     * @return the request object.
     */
    @Override
    public Request<?, NeoSendToAddress> sendToAddress(String assetId, String toAddress,
            String value) {
        return sendToAddress(new Hash160(assetId), toAddress, value);
    }

    /**
     * Transfers an amount of an asset to another address.
     *
     * @param assetId   the script hash of the NEP17 contract.
     * @param toAddress the destination address.
     * @param value     the transfer amount.
     * @return the request object.
     */
    @Override
    public Request<?, NeoSendToAddress> sendToAddress(Hash160 assetId, String toAddress,
            String value) {
        return new Request<>(
                "sendtoaddress",
                Stream.of(assetId, toAddress, value)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()),
                neow3jService,
                NeoSendToAddress.class);
    }

    /**
     * Transfers an amount of an asset to another address.
     *
     * @param txSendAsset a {@code TransactionSendAsset} object containing the
     *                    asset, the destination address and the transfer
     *                    amount.
     * @return the request object.
     */
    @Override
    public Request<?, NeoSendToAddress> sendToAddress(TransactionSendAsset txSendAsset) {
        return sendToAddress(txSendAsset.getAsset(), txSendAsset.getAddress(),
                txSendAsset.getValue());
    }

    // ApplicationLogs

    /**
     * Gets the application logs of the specified transaction.
     *
     * @param txId the transaction id.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetApplicationLog> getApplicationLog(String txId) {
        return getApplicationLog(new Hash256(txId));
    }

    /**
     * Gets the application logs of the specified transaction.
     *
     * @param txId the transaction id.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetApplicationLog> getApplicationLog(Hash256 txId) {
        return new Request<>(
                "getapplicationlog",
                singletonList(txId),
                neow3jService,
                NeoGetApplicationLog.class);
    }

    // RpcNep17Tracker

    /**
     * Gets the balance of all NEP17 assets in the specified address.
     *
     * @param address the address.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetNep17Balances> getNep17Balances(String address) {
        return new Request<>(
                "getnep17balances",
                singletonList(address),
                neow3jService,
                NeoGetNep17Balances.class);
    }

    /**
     * Gets all the NEP17 transaction information occurred in the specified
     * address.
     *
     * @param address the address.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetNep17Transfers> getNep17Transfers(String address) {
        return new Request<>(
                "getnep17transfers",
                singletonList(address),
                neow3jService,
                NeoGetNep17Transfers.class);
    }

    /**
     * Gets all the NEP17 transaction information occurred in the specified
     * address since the specified time.
     *
     * @param address the address.
     * @param from    the timestamp transactions occurred since.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetNep17Transfers> getNep17Transfers(String address, Date from) {
        return new Request<>(
                "getnep17transfers",
                asList(address, from.getTime()),
                neow3jService,
                NeoGetNep17Transfers.class);
    }

    /**
     * Gets all the NEP17 transaction information occurred in the specified
     * address in the specified time range.
     *
     * @param address the address.
     * @param from    the start timestamp.
     * @param until   the end timestamp.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetNep17Transfers> getNep17Transfers(String address, Date from,
            Date until) {
        return new Request<>(
                "getnep17transfers",
                asList(address, from.getTime(), until.getTime()),
                neow3jService,
                NeoGetNep17Transfers.class);
    }

    // StateService

    /**
     * Gets the state root by the block height.
     *
     * @param blockIndex the block index.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetStateRoot> getStateRoot(BigInteger blockIndex) {
        return new Request<>(
                "getstateroot",
                singletonList(blockIndex),
                neow3jService,
                NeoGetStateRoot.class);
    }

    /**
     * Gets the proof based on the root hash, the contract script hash and the storage key.
     *
     * @param rootHash           the root hash.
     * @param contractScriptHash the contract script hash.
     * @param storageKeyHex      the storage key.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetProof> getProof(Hash256 rootHash, Hash160 contractScriptHash,
            String storageKeyHex) {
        return new Request<>(
                "getproof",
                asList(rootHash, contractScriptHash,
                        Base64.encode(storageKeyHex)),
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

    // Neow3j Rx Convenience methods:

    @Override
    public Observable<NeoGetBlock> blockObservable(boolean fullTransactionObjects) {
        return neow3jRx.blockObservable(fullTransactionObjects, blockTime);
    }

    @Override
    public Observable<NeoGetBlock> replayBlocksObservable(
            BigInteger startBlock, BigInteger endBlock,
            boolean fullTransactionObjects) {
        return neow3jRx.replayBlocksObservable(startBlock, endBlock, fullTransactionObjects);
    }

    @Override
    public Observable<NeoGetBlock> replayBlocksObservable(
            BigInteger startBlock, BigInteger endBlock,
            boolean fullTransactionObjects, boolean ascending) {
        return neow3jRx.replayBlocksObservable(startBlock, endBlock,
                fullTransactionObjects, ascending);
    }

    @Override
    public Observable<NeoGetBlock> catchUpToLatestBlockObservable(
            BigInteger startBlock, boolean fullTransactionObjects,
            Observable<NeoGetBlock> onCompleteObservable) {
        return neow3jRx.catchUpToLatestBlockObservable(
                startBlock, fullTransactionObjects, onCompleteObservable);
    }

    @Override
    public Observable<NeoGetBlock> catchUpToLatestBlockObservable(
            BigInteger startBlock, boolean fullTransactionObjects) {
        return neow3jRx.catchUpToLatestBlockObservable(startBlock, fullTransactionObjects);
    }

    @Override
    public Observable<NeoGetBlock> catchUpToLatestAndSubscribeToNewBlocksObservable(
            BigInteger startBlock, boolean fullTransactionObjects) {
        return neow3jRx.catchUpToLatestAndSubscribeToNewBlocksObservable(
                startBlock, fullTransactionObjects, blockTime);
    }

    @Override
    public Observable<NeoGetBlock> subscribeToNewBlocksObservable(boolean fullTransactionObjects)
            throws IOException {

        return neow3jRx.subscribeToNewBlocksObservable(fullTransactionObjects, blockTime);
    }

    @Override
    public void shutdown() {
        scheduledExecutorService.shutdown();
        try {
            neow3jService.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to close neow3j service", e);
        }
    }

}
