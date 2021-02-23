package io.neow3j.protocol.core;

import static io.neow3j.utils.Numeric.cleanHexPrefix;
import static io.neow3j.utils.Strings.isEmpty;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import io.neow3j.contract.ContractParameter;
import io.neow3j.contract.ScriptHash;
import io.neow3j.crypto.Base64;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.Neow3jService;
import io.neow3j.protocol.core.methods.response.NeoBlockCount;
import io.neow3j.protocol.core.methods.response.NeoBlockHash;
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
import io.neow3j.protocol.core.methods.response.NeoGetRawBlock;
import io.neow3j.protocol.core.methods.response.NeoGetRawMemPool;
import io.neow3j.protocol.core.methods.response.NeoGetRawTransaction;
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
import io.neow3j.protocol.core.methods.response.TransactionSendAsset;
import io.neow3j.protocol.core.methods.response.TransactionSigner;
import io.neow3j.protocol.rx.JsonRpc2_0Rx;
import io.neow3j.transaction.Signer;
import io.neow3j.utils.Async;
import io.neow3j.utils.Numeric;
import io.reactivex.Observable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
     * Gets the hash of the latest block in the main chain.
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
    public Request<?, NeoBlockHash> getBlockHash(BlockParameterIndex blockIndex) {
        return new Request<>(
                "getblockhash",
                singletonList(blockIndex.getBlockIndex()),
                neow3jService,
                NeoBlockHash.class);
    }

    /**
     * Gets the corresponding block information according to the specified hash
     * or index.
     *
     * @param address                      the block hash.
     * @param returnFullTransactionObjects whether to get block information
     *                                     with all transaction objects or
     *                                     just the block header.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetBlock> getBlock(String address,
            boolean returnFullTransactionObjects) {
        if (returnFullTransactionObjects) {
            return new Request<>(
                    "getblock",
                    asList(address, 1),
                    neow3jService,
                    NeoGetBlock.class);
        } else {
            return getBlockHeader(address);
        }
    }

    /**
     * Gets the corresponding block information according to the specified hash
     * in hexadecimal.
     *
     * @param address the block hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetRawBlock> getRawBlock(String address) {
        return new Request<>(
                "getblock",
                asList(address, 0),
                neow3jService,
                NeoGetRawBlock.class);
    }

    /**
     * Gets the corresponding block information according to the specified
     * index.
     *
     * @param blockIndex                   the block index.
     * @param returnFullTransactionObjects whether to get block information
     *                                     with all transaction objects or
     *                                     just the block header.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetBlock> getBlock(BlockParameterIndex blockIndex,
            boolean returnFullTransactionObjects) {
        if (returnFullTransactionObjects) {
            return new Request<>(
                    "getblock",
                    asList(blockIndex.getBlockIndex(), 1),
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
    public Request<?, NeoGetRawBlock> getRawBlock(BlockParameterIndex blockIndex) {
        return new Request<>(
                "getblock",
                asList(blockIndex.getBlockIndex(), 0),
                neow3jService,
                NeoGetRawBlock.class);
    }

    /**
     * Gets the block count of the main chain.
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
     * specified script hash.
     *
     * @param hash the block script hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetBlock> getBlockHeader(String hash) {
        return new Request<>(
                "getblockheader",
                asList(hash, 1),
                neow3jService,
                NeoGetBlock.class);
    }

    /**
     * Gets the corresponding block header information according to the
     * specified index.
     *
     * @param blockIndex the block index.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetBlock> getBlockHeader(BlockParameterIndex blockIndex) {
        return new Request<>(
                "getblockheader",
                asList(blockIndex.getBlockIndex(), 1),
                neow3jService,
                NeoGetBlock.class);
    }

    /**
     * Gets the corresponding block header information according to the
     * specified script hash in hexadecimal.
     *
     * @param hash the block script hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetRawBlock> getRawBlockHeader(String hash) {
        return new Request<>(
                "getblockheader",
                asList(hash, 0),
                neow3jService,
                NeoGetRawBlock.class);
    }

    /**
     * Gets the corresponding block header information according to the
     * specified script hash in hexadecimal.
     *
     * @param blockIndex the block index.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetRawBlock> getRawBlockHeader(BlockParameterIndex blockIndex) {
        return new Request<>(
                "getblockheader",
                asList(blockIndex.getBlockIndex(), 0),
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
     * @param scriptHash the contract script hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetContractState> getContractState(ScriptHash scriptHash) {
        return new Request<>(
                "getcontractstate",
                singletonList(scriptHash.toString()),
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
        return new Request<>(
                "getstorage",
                asList(contractAddress, Base64.encode(keyHexString)),
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
     * Invokes a smart contract with its script hash based on the specified
     * operation.
     *
     * @param contractScriptHash the contract script hash to invoke.
     * @param functionName       the function to invoke.
     * @param signers            the signers.
     * @return the request object.
     */
    @Override
    public Request<?, NeoInvokeFunction> invokeFunction(String contractScriptHash,
            String functionName, Signer... signers) {
        return invokeFunction(contractScriptHash, functionName, null, signers);
    }

    /**
     * Invokes a smart contract based on the specified function and parameters.
     *
     * @param contractScriptHash the contract script hash to invoke.
     * @param functionName       the function to invoke.
     * @param contractParams     the parameters of the function.
     * @param signers            the signers.
     * @return the request object.
     */
    @Override
    public Request<?, NeoInvokeFunction> invokeFunction(String contractScriptHash,
            String functionName, List<ContractParameter> contractParams,
            Signer... signers) {

        if (contractParams == null) {
            contractParams = new ArrayList<>();
        }
        List<TransactionSigner> txSigners = Arrays.stream(signers)
                .map(TransactionSigner::new).collect(Collectors.toList());
        List<?> params;
        if (txSigners.size() > 0) {
            params = asList(contractScriptHash, functionName, contractParams, txSigners);
        } else {
            params = asList(contractScriptHash, functionName, contractParams);
        }
        return new Request<>(
                "invokefunction",
                params.stream().filter(Objects::nonNull)
                        .collect(Collectors.toList()),
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
    public Request<?, NeoInvokeScript> invokeScript(String script,
            Signer... signers) {
        List<?> params;
        if (signers.length > 0) {
            params = asList(script, Arrays.stream(signers)
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

        if (methodParams == null) {
            methodParams = new ArrayList<>();
        }
        List<TransactionSigner> txSigners = Arrays.stream(signers)
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
        return new Request<>(
                "getwalletbalance",
                singletonList(cleanHexPrefix(assetId)),
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
                asList(txSendAsset.stream().filter(Objects::nonNull).collect(Collectors.toList())),
                neow3jService,
                NeoSendMany.class);
    }

    /**
     * Initiates multiple transfers to multiple addresses from one specific
     * address in a transaction.
     *
     * @param fromAddress the transferring address.
     * @param txSendAsset a list of {@code TransactoinSendAsset} objects, that
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
        return new Request<>(
                "sendtoaddress",
                Stream.of(assetId, toAddress, value)
                        .filter((param) -> (param != null && !isEmpty(param)))
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
     * Gets the contract event information based on the specified transaction
     * id. The contract event information is stored under the ApplicationLogs
     * directory.
     *
     * @param txId the transaction id.
     * @return the request object.
     */
    @Override
    public Request<?, NeoGetApplicationLog> getApplicationLog(String txId) {
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

    // Neow3j Rx Convenience methods:

    @Override
    public Observable<NeoGetBlock> blockObservable(boolean fullTransactionObjects) {
        return neow3jRx.blockObservable(fullTransactionObjects, blockTime);
    }

    @Override
    public Observable<NeoGetBlock> replayBlocksObservable(
            BlockParameter startBlock, BlockParameter endBlock,
            boolean fullTransactionObjects) {
        return neow3jRx.replayBlocksObservable(startBlock, endBlock, fullTransactionObjects);
    }

    @Override
    public Observable<NeoGetBlock> replayBlocksObservable(
            BlockParameter startBlock, BlockParameter endBlock,
            boolean fullTransactionObjects, boolean ascending) {
        return neow3jRx.replayBlocksObservable(startBlock, endBlock,
                fullTransactionObjects, ascending);
    }

    @Override
    public Observable<NeoGetBlock> catchUpToLatestBlockObservable(
            BlockParameter startBlock, boolean fullTransactionObjects,
            Observable<NeoGetBlock> onCompleteObservable) {
        return neow3jRx.catchUpToLatestBlockObservable(
                startBlock, fullTransactionObjects, onCompleteObservable);
    }

    @Override
    public Observable<NeoGetBlock> catchUpToLatestBlockObservable(
            BlockParameter startBlock, boolean fullTransactionObjects) {
        return neow3jRx.catchUpToLatestBlockObservable(startBlock, fullTransactionObjects);
    }

    @Override
    public Observable<NeoGetBlock> catchUpToLatestAndSubscribeToNewBlocksObservable(
            BlockParameter startBlock, boolean fullTransactionObjects) {
        return neow3jRx.catchUpToLatestAndSubscribeToNewBlocksObservable(
                startBlock, fullTransactionObjects, blockTime);
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
