package io.neow3j.protocol.core;

import io.neow3j.model.ContractParameter;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.Neow3jService;
import io.neow3j.protocol.core.methods.response.NeoBlockCount;
import io.neow3j.protocol.core.methods.response.NeoBlockHash;
import io.neow3j.protocol.core.methods.response.NeoConnectionCount;
import io.neow3j.protocol.core.methods.response.NeoDumpPrivKey;
import io.neow3j.protocol.core.methods.response.NeoGetAccountState;
import io.neow3j.protocol.core.methods.response.NeoGetApplicationLog;
import io.neow3j.protocol.core.methods.response.NeoGetAssetState;
import io.neow3j.protocol.core.methods.response.NeoGetBalance;
import io.neow3j.protocol.core.methods.response.NeoGetBlock;
import io.neow3j.protocol.core.methods.response.NeoGetBlockSysFee;
import io.neow3j.protocol.core.methods.response.NeoGetClaimable;
import io.neow3j.protocol.core.methods.response.NeoGetContractState;
import io.neow3j.protocol.core.methods.response.NeoGetNep5Balances;
import io.neow3j.protocol.core.methods.response.NeoGetNewAddress;
import io.neow3j.protocol.core.methods.response.NeoGetPeers;
import io.neow3j.protocol.core.methods.response.NeoGetRawBlock;
import io.neow3j.protocol.core.methods.response.NeoGetRawMemPool;
import io.neow3j.protocol.core.methods.response.NeoGetRawTransaction;
import io.neow3j.protocol.core.methods.response.NeoGetStorage;
import io.neow3j.protocol.core.methods.response.NeoGetTransaction;
import io.neow3j.protocol.core.methods.response.NeoGetTxOut;
import io.neow3j.protocol.core.methods.response.NeoGetUnspents;
import io.neow3j.protocol.core.methods.response.NeoGetValidators;
import io.neow3j.protocol.core.methods.response.NeoGetVersion;
import io.neow3j.protocol.core.methods.response.NeoGetWalletHeight;
import io.neow3j.protocol.core.methods.response.NeoInvoke;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.NeoInvokeScript;
import io.neow3j.protocol.core.methods.response.NeoListAddress;
import io.neow3j.protocol.core.methods.response.NeoListPlugins;
import io.neow3j.protocol.core.methods.response.NeoSendMany;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.methods.response.NeoSendToAddress;
import io.neow3j.protocol.core.methods.response.NeoSubmitBlock;
import io.neow3j.protocol.core.methods.response.NeoValidateAddress;
import io.neow3j.protocol.core.methods.response.TransactionOutput;
import io.neow3j.protocol.rx.JsonRpc2_0Rx;
import io.neow3j.utils.Async;
import rx.Observable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import static io.neow3j.utils.Numeric.cleanHexPrefix;
import static io.neow3j.utils.Strings.isEmpty;

/**
 * JSON-RPC 2.0 factory implementation.
 */
public class JsonRpc2_0Neow3j implements Neow3j {

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

    @Override
    public Request<?, NeoGetVersion> getVersion() {
        return new Request<>(
                "getversion",
                Collections.<String>emptyList(),
                neow3jService,
                NeoGetVersion.class);
    }


    @Override
    public Request<?, NeoGetBlock> getBlock(String address, boolean returnFullTransactionObjects) {
        if (returnFullTransactionObjects) {
            return new Request<>(
                    "getblock",
                    Arrays.asList(address, 1),
                    neow3jService,
                    NeoGetBlock.class);
        } else {
            return getBlockHeader(address);
        }
    }

    @Override
    public Request<?, NeoGetRawBlock> getRawBlock(String address) {
        return new Request<>(
                "getblock",
                Arrays.asList(address, 0),
                neow3jService,
                NeoGetRawBlock.class);
    }

    @Override
    public Request<?, NeoGetBlock> getBlock(BlockParameterIndex blockIndex, boolean returnFullTransactionObjects) {
        if (returnFullTransactionObjects) {
            return new Request<>(
                    "getblock",
                    Arrays.asList(blockIndex.getBlockIndex(), 1),
                    neow3jService,
                    NeoGetBlock.class);
        } else {
            return getBlockHeader(blockIndex);
        }
    }

    @Override
    public Request<?, NeoGetRawBlock> getRawBlock(BlockParameterIndex blockIndex) {
        return new Request<>(
                "getblock",
                Arrays.asList(blockIndex.getBlockIndex(), 0),
                neow3jService,
                NeoGetRawBlock.class);
    }

    @Override
    public Request<?, NeoBlockCount> getBlockCount() {
        return new Request<>(
                "getblockcount",
                Collections.<String>emptyList(),
                neow3jService,
                NeoBlockCount.class);
    }

    @Override
    public Request<?, NeoBlockHash> getBestBlockHash() {
        return new Request<>(
                "getbestblockhash",
                Collections.<String>emptyList(),
                neow3jService,
                NeoBlockHash.class);
    }

    @Override
    public Request<?, NeoBlockHash> getBlockHash(BlockParameterIndex blockIndex) {
        return new Request<>(
                "getblockhash",
                Arrays.asList(blockIndex.getBlockIndex()),
                neow3jService,
                NeoBlockHash.class);
    }

    @Override
    public Request<?, NeoGetBlock> getBlockHeader(String hash) {
        return new Request<>(
                "getblockheader",
                Arrays.asList(hash, 1),
                neow3jService,
                NeoGetBlock.class);
    }

    @Override
    public Request<?, NeoGetBlock> getBlockHeader(BlockParameterIndex blockIndex) {
        return new Request<>(
                "getblockheader",
                Arrays.asList(blockIndex.getBlockIndex(), 1),
                neow3jService,
                NeoGetBlock.class);
    }

    @Override
    public Request<?, NeoGetRawBlock> getRawBlockHeader(String hash) {
        return new Request<>(
                "getblockheader",
                Arrays.asList(hash, 0),
                neow3jService,
                NeoGetRawBlock.class);
    }

    @Override
    public Request<?, NeoGetRawBlock> getRawBlockHeader(BlockParameterIndex blockIndex) {
        return new Request<>(
                "getblockheader",
                Arrays.asList(blockIndex.getBlockIndex(), 0),
                neow3jService,
                NeoGetRawBlock.class);
    }

    @Override
    public Request<?, NeoConnectionCount> getConnectionCount() {
        return new Request<>(
                "getconnectioncount",
                Collections.<String>emptyList(),
                neow3jService,
                NeoConnectionCount.class);
    }

    @Override
    public Request<?, NeoListAddress> listAddress() {
        return new Request<>(
                "listaddress",
                Collections.<String>emptyList(),
                neow3jService,
                NeoListAddress.class);
    }

    @Override
    public Request<?, NeoGetPeers> getPeers() {
        return new Request<>(
                "getpeers",
                Collections.<String>emptyList(),
                neow3jService,
                NeoGetPeers.class);
    }

    @Override
    public Request<?, NeoGetRawMemPool> getRawMemPool() {
        return new Request<>(
                "getrawmempool",
                Collections.<String>emptyList(),
                neow3jService,
                NeoGetRawMemPool.class);
    }

    @Override
    public Request<?, NeoGetValidators> getValidators() {
        return new Request<>(
                "getvalidators",
                Collections.<String>emptyList(),
                neow3jService,
                NeoGetValidators.class);
    }

    @Override
    public Request<?, NeoValidateAddress> validateAddress(String address) {
        return new Request<>(
                "validateaddress",
                Arrays.asList(address),
                neow3jService,
                NeoValidateAddress.class);
    }

    @Override
    public Request<?, NeoGetAccountState> getAccountState(String address) {
        return new Request<>(
                "getaccountstate",
                Arrays.asList(address),
                neow3jService,
                NeoGetAccountState.class);
    }

    @Override
    public Request<?, NeoGetNewAddress> getNewAddress() {
        return new Request<>(
                "getnewaddress",
                Collections.<String>emptyList(),
                neow3jService,
                NeoGetNewAddress.class);
    }

    @Override
    public Request<?, NeoGetWalletHeight> getWalletHeight() {
        return new Request<>(
                "getwalletheight",
                Collections.<String>emptyList(),
                neow3jService,
                NeoGetWalletHeight.class);
    }

    @Override
    public Request<?, NeoGetBlockSysFee> getBlockSysFee(BlockParameterIndex blockIndex) {
        return new Request<>(
                "getblocksysfee",
                Arrays.asList(blockIndex.getBlockIndex()),
                neow3jService,
                NeoGetBlockSysFee.class);
    }

    @Override
    public Request<?, NeoGetTxOut> getTxOut(String transactionHash, int txIndex) {
        return new Request<>(
                "gettxout",
                Arrays.asList(transactionHash, txIndex),
                neow3jService,
                NeoGetTxOut.class);
    }

    @Override
    public Request<?, NeoSendRawTransaction> sendRawTransaction(String rawTransactionHex) {
        return new Request<>(
                "sendrawtransaction",
                Arrays.asList(rawTransactionHex),
                neow3jService,
                NeoSendRawTransaction.class);
    }

    @Override
    public Request<?, NeoSendToAddress> sendToAddress(String assetId, String toAddress, String value) {
        return sendToAddress(assetId, toAddress, value, null, null);
    }

    @Override
    public Request<?, NeoSendToAddress> sendToAddress(String assetId, String toAddress, String value, String fee) {
        return sendToAddress(assetId, toAddress, value, fee, null);
    }

    @Override
    public Request<?, NeoSendToAddress> sendToAddress(String assetId, String toAddress, String value, String fee, String changeAddress) {
        return new Request<>(
                "sendtoaddress",
                Arrays.asList(assetId, toAddress, value, fee, changeAddress).stream()
                        .filter((param) -> (param != null && !isEmpty(param)))
                        .collect(Collectors.toList()),
                neow3jService,
                NeoSendToAddress.class);
    }

    @Override
    public Request<?, NeoGetTransaction> getTransaction(String txId) {
        return new Request<>(
                "getrawtransaction",
                Arrays.asList(txId, 1),
                neow3jService,
                NeoGetTransaction.class);
    }

    @Override
    public Request<?, NeoGetRawTransaction> getRawTransaction(String txId) {
        return new Request<>(
                "getrawtransaction",
                Arrays.asList(txId, 0),
                neow3jService,
                NeoGetRawTransaction.class);
    }

    @Override
    public Request<?, NeoGetBalance> getBalance(String assetId) {
        return new Request<>(
                "getbalance",
                Arrays.asList(cleanHexPrefix(assetId)),
                neow3jService,
                NeoGetBalance.class);
    }

    @Override
    public Request<?, NeoGetAssetState> getAssetState(String assetId) {
        return new Request<>(
                "getassetstate",
                Arrays.asList(cleanHexPrefix(assetId)),
                neow3jService,
                NeoGetAssetState.class);
    }

    @Override
    public Request<?, NeoSendMany> sendMany(List<TransactionOutput> outputs) {
        return sendMany(outputs, null, null);
    }

    @Override
    public Request<?, NeoSendMany> sendMany(List<TransactionOutput> outputs, String fee) {
        return sendMany(outputs, fee, null);
    }

    @Override
    public Request<?, NeoSendMany> sendMany(List<TransactionOutput> outputs, String fee, String changeAddress) {
        return new Request<>(
                "sendmany",
                Arrays.asList(outputs, fee, changeAddress).stream()
                        .filter((param) -> (param != null))
                        .collect(Collectors.toList()),
                neow3jService,
                NeoSendMany.class);
    }

    @Override
    public Request<?, NeoDumpPrivKey> dumpPrivKey(String address) {
        return new Request<>(
                "dumpprivkey",
                Arrays.asList(address),
                neow3jService,
                NeoDumpPrivKey.class);
    }

    @Override
    public Request<?, NeoGetStorage> getStorage(String contractAddress, HexParameter keyToLookUp) {
        return getStorage(contractAddress, keyToLookUp.getHexValue());
    }

    @Override
    public Request<?, NeoGetStorage> getStorage(String contractAddress, String keyToLookUpAsHexString) {
        return new Request<>(
                "getstorage",
                Arrays.asList(contractAddress, keyToLookUpAsHexString),
                neow3jService,
                NeoGetStorage.class);
    }

    @Override
    public Request<?, NeoInvoke> invoke(String contractScriptHash, List<ContractParameter> params) {
        return new Request<>(
                "invoke",
                Arrays.asList(contractScriptHash, params),
                neow3jService,
                NeoInvoke.class);
    }

    @Override
    public Request<?, NeoInvokeFunction> invokeFunction(String contractScriptHash, String functionName) {
        return invokeFunction(contractScriptHash, functionName, null);
    }

    @Override
    public Request<?, NeoInvokeFunction> invokeFunction(String contractScriptHash, String functionName, List<ContractParameter> params) {
        return new Request<>(
                "invokefunction",
                Arrays.asList(contractScriptHash, functionName, params).stream()
                        .filter((param) -> (param != null))
                        .collect(Collectors.toList()),
                neow3jService,
                NeoInvokeFunction.class);
    }

    @Override
    public Request<?, NeoInvokeScript> invokeScript(String script) {
        return new Request<>(
                "invokescript",
                Arrays.asList(script),
                neow3jService,
                NeoInvokeScript.class);
    }

    @Override
    public Request<?, NeoGetContractState> getContractState(String scriptHash) {
        return new Request<>(
                "getcontractstate",
                Arrays.asList(scriptHash),
                neow3jService,
                NeoGetContractState.class);
    }

    @Override
    public Request<?, NeoSubmitBlock> submitBlock(String serializedBlockAsHex) {
        return new Request<>(
                "submitblock",
                Arrays.asList(serializedBlockAsHex),
                neow3jService,
                NeoSubmitBlock.class);
    }

    @Override
    public Request<?, NeoGetUnspents> getUnspents(String address) {
        return new Request<>(
                "getunspents",
                Arrays.asList(address),
                neow3jService,
                NeoGetUnspents.class);
    }

    @Override
    public Request<?, NeoGetNep5Balances> getNep5Balances(String address) {
        return new Request<>(
                "getnep5balances",
                Arrays.asList(address),
                neow3jService,
                NeoGetNep5Balances.class);
    }

    @Override
    public Request<?, NeoGetClaimable> getClaimable(String address) {
        return new Request<>(
                "getclaimable",
                Arrays.asList(address),
                neow3jService,
                NeoGetClaimable.class);
    }

    @Override
    public Request<?, NeoListPlugins> listPlugins() {
        return new Request<>(
                "listplugins",
                Collections.<String>emptyList(),
                neow3jService,
                NeoListPlugins.class);
    }

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
    public Request<?, NeoGetApplicationLog> getApplicationLog(String txId) {
        return new Request<>(
                "getapplicationlog",
                Collections.singletonList(txId),
                neow3jService,
                NeoGetApplicationLog.class);
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
