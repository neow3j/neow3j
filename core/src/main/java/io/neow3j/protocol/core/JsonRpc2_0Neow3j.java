package io.neow3j.protocol.core;

import static io.neow3j.utils.Numeric.cleanHexPrefix;
import static io.neow3j.utils.Strings.isEmpty;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import io.neow3j.contract.ContractParameter;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.Neow3jService;
import io.neow3j.protocol.core.methods.response.NeoBlockCount;
import io.neow3j.protocol.core.methods.response.NeoBlockHash;
import io.neow3j.protocol.core.methods.response.NeoCloseWallet;
import io.neow3j.protocol.core.methods.response.NeoConnectionCount;
import io.neow3j.protocol.core.methods.response.NeoDumpPrivKey;
import io.neow3j.protocol.core.methods.response.NeoGetApplicationLog;
import io.neow3j.protocol.core.methods.response.NeoGetBlock;
import io.neow3j.protocol.core.methods.response.NeoGetContractState;
import io.neow3j.protocol.core.methods.response.NeoGetMemPool;
import io.neow3j.protocol.core.methods.response.NeoGetNep5Balances;
import io.neow3j.protocol.core.methods.response.NeoGetNep5Transfers;
import io.neow3j.protocol.core.methods.response.NeoGetNewAddress;
import io.neow3j.protocol.core.methods.response.NeoGetPeers;
import io.neow3j.protocol.core.methods.response.NeoGetRawBlock;
import io.neow3j.protocol.core.methods.response.NeoGetRawMemPool;
import io.neow3j.protocol.core.methods.response.NeoGetRawTransaction;
import io.neow3j.protocol.core.methods.response.NeoGetStorage;
import io.neow3j.protocol.core.methods.response.NeoGetTransaction;
import io.neow3j.protocol.core.methods.response.NeoGetTransactionHeight;
import io.neow3j.protocol.core.methods.response.NeoGetUnclaimedGas;
import io.neow3j.protocol.core.methods.response.NeoGetValidators;
import io.neow3j.protocol.core.methods.response.NeoGetVersion;
import io.neow3j.protocol.core.methods.response.NeoGetWalletBalance;
import io.neow3j.protocol.core.methods.response.NeoGetWalletUnclaimedGas;
import io.neow3j.protocol.core.methods.response.NeoImportPrivKey;
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
import io.reactivex.Observable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

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

    // Blockchain Methods

    @Override
    public Request<?, NeoBlockHash> getBestBlockHash() {
        return new Request<>(
                "getbestblockhash",
                emptyList(),
                neow3jService,
                NeoBlockHash.class);
    }

    @Override
    public Request<?, NeoBlockHash> getBlockHash(BlockParameterIndex blockIndex) {
        return new Request<>(
                "getblockhash",
                asList(blockIndex.getBlockIndex()),
                neow3jService,
                NeoBlockHash.class);
    }

    @Override
    public Request<?, NeoGetBlock> getBlock(String address, boolean returnFullTransactionObjects) {
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

    @Override
    public Request<?, NeoGetRawBlock> getRawBlock(String address) {
        return new Request<>(
                "getblock",
                asList(address, 0),
                neow3jService,
                NeoGetRawBlock.class);
    }

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

    @Override
    public Request<?, NeoGetRawBlock> getRawBlock(BlockParameterIndex blockIndex) {
        return new Request<>(
                "getblock",
                asList(blockIndex.getBlockIndex(), 0),
                neow3jService,
                NeoGetRawBlock.class);
    }

    @Override
    public Request<?, NeoBlockCount> getBlockCount() {
        return new Request<>(
                "getblockcount",
                emptyList(),
                neow3jService,
                NeoBlockCount.class);
    }

    @Override
    public Request<?, NeoGetBlock> getBlockHeader(String hash) {
        return new Request<>(
                "getblockheader",
                asList(hash, 1),
                neow3jService,
                NeoGetBlock.class);
    }

    @Override
    public Request<?, NeoGetBlock> getBlockHeader(BlockParameterIndex blockIndex) {
        return new Request<>(
                "getblockheader",
                asList(blockIndex.getBlockIndex(), 1),
                neow3jService,
                NeoGetBlock.class);
    }

    @Override
    public Request<?, NeoGetRawBlock> getRawBlockHeader(String hash) {
        return new Request<>(
                "getblockheader",
                asList(hash, 0),
                neow3jService,
                NeoGetRawBlock.class);
    }

    @Override
    public Request<?, NeoGetRawBlock> getRawBlockHeader(BlockParameterIndex blockIndex) {
        return new Request<>(
                "getblockheader",
                asList(blockIndex.getBlockIndex(), 0),
                neow3jService,
                NeoGetRawBlock.class);
    }

    @Override
    public Request<?, NeoGetContractState> getContractState(String scriptHash) {
        return new Request<>(
                "getcontractstate",
                asList(scriptHash),
                neow3jService,
                NeoGetContractState.class);
    }

    @Override
    public Request<?, NeoGetMemPool> getMemPool() {
        return new Request<>(
                "getrawmempool",
                Arrays.asList(1),
                neow3jService,
                NeoGetMemPool.class);
    }

    @Override
    public Request<?, NeoGetRawMemPool> getRawMemPool() {
        return new Request<>(
                "getrawmempool",
                emptyList(),
                neow3jService,
                NeoGetRawMemPool.class);
    }

    @Override
    public Request<?, NeoGetTransaction> getTransaction(String txId) {
        return new Request<>(
                "getrawtransaction",
                asList(txId, 1),
                neow3jService,
                NeoGetTransaction.class);
    }

    @Override
    public Request<?, NeoGetRawTransaction> getRawTransaction(String txId) {
        return new Request<>(
                "getrawtransaction",
                asList(txId, 0),
                neow3jService,
                NeoGetRawTransaction.class);
    }

    @Override
    public Request<?, NeoGetStorage> getStorage(String contractAddress, HexParameter keyToLookUp) {
        return getStorage(contractAddress, keyToLookUp.getHexValue());
    }

    @Override
    public Request<?, NeoGetStorage> getStorage(String contractAddress,
            String keyToLookUpAsHexString) {
        return new Request<>(
                "getstorage",
                asList(contractAddress, keyToLookUpAsHexString),
                neow3jService,
                NeoGetStorage.class);
    }

    @Override
    public Request<?, NeoGetTransactionHeight> getTransactionHeight(String txId) {
        return new Request<>(
                "gettransactionheight",
                asList(txId),
                neow3jService,
                NeoGetTransactionHeight.class);
    }

    @Override
    public Request<?, NeoGetValidators> getValidators() {
        return new Request<>(
                "getvalidators",
                emptyList(),
                neow3jService,
                NeoGetValidators.class);
    }

    // Node Methods

    @Override
    public Request<?, NeoConnectionCount> getConnectionCount() {
        return new Request<>(
                "getconnectioncount",
                emptyList(),
                neow3jService,
                NeoConnectionCount.class);
    }

    @Override
    public Request<?, NeoGetPeers> getPeers() {
        return new Request<>(
                "getpeers",
                emptyList(),
                neow3jService,
                NeoGetPeers.class);
    }

    @Override
    public Request<?, NeoGetVersion> getVersion() {
        return new Request<>(
                "getversion",
                emptyList(),
                neow3jService,
                NeoGetVersion.class);
    }

    @Override
    public Request<?, NeoSendRawTransaction> sendRawTransaction(String rawTransactionHex) {
        return new Request<>(
                "sendrawtransaction",
                asList(rawTransactionHex),
                neow3jService,
                NeoSendRawTransaction.class);
    }

    @Override
    public Request<?, NeoSubmitBlock> submitBlock(String serializedBlockAsHex) {
        return new Request<>(
                "submitblock",
                asList(serializedBlockAsHex),
                neow3jService,
                NeoSubmitBlock.class);
    }

    // SmartContract Methods

    @Override
    public Request<?, NeoInvokeFunction> invokeFunction(String contractScriptHash,
            String functionName, Signer... witnesses) {
        return invokeFunction(contractScriptHash, functionName, null, witnesses);
    }

    @Override
    public Request<?, NeoInvokeFunction> invokeFunction(String contractScriptHash,
            String functionName, List<ContractParameter> contractParams, Signer... witnesses) {

        List<TransactionSigner> signers = new ArrayList<>();
        Arrays.stream(witnesses).map(TransactionSigner::new).forEach(signers::add);
        List<?> params;
        if (signers.size() > 0) {
            params = asList(contractScriptHash, functionName, contractParams, signers);
        } else {
            params = asList(contractScriptHash, functionName, contractParams);
        }
        return new Request<>(
                "invokefunction",
                params.stream().filter(Objects::nonNull).collect(Collectors.toList()),
                neow3jService,
                NeoInvokeFunction.class);
    }

    @Override
    public Request<?, NeoInvokeScript> invokeScript(String script, Signer... signers) {
        List<?> params;
        if (signers.length > 0) {
            params = asList(script, Arrays.stream(signers)
                    .map(TransactionSigner::new)
                    .collect(Collectors.toList()));
        } else {
            params = asList(script);
        }
        return new Request<>(
                "invokescript",
                params,
                neow3jService,
                NeoInvokeScript.class);
    }

    // TODO: 12.08.20 Michael: getunclaimedgas of a specific address
    @Override
    public Request<?, NeoGetUnclaimedGas> getUnclaimedGas(String address) {
        return new Request<>(
                "getunclaimedgas",
                asList(address),
                neow3jService,
                NeoGetUnclaimedGas.class);
    }

    // Utilities Methods

    @Override
    public Request<?, NeoListPlugins> listPlugins() {
        return new Request<>(
                "listplugins",
                emptyList(),
                neow3jService,
                NeoListPlugins.class);
    }

    @Override
    public Request<?, NeoValidateAddress> validateAddress(String address) {
        return new Request<>(
                "validateaddress",
                asList(address),
                neow3jService,
                NeoValidateAddress.class);
    }

    // Wallet Methods

    @Override
    public Request<?, NeoCloseWallet> closeWallet() {
        return new Request<>(
                "closewallet",
                emptyList(),
                neow3jService,
                NeoCloseWallet.class);
    }

    @Override
    public Request<?, NeoDumpPrivKey> dumpPrivKey(String address) {
        return new Request<>(
                "dumpprivkey",
                asList(address),
                neow3jService,
                NeoDumpPrivKey.class);
    }

    @Override
    public Request<?, NeoGetWalletBalance> getWalletBalance(String assetId) {
        return new Request<>(
                "getwalletbalance",
                asList(cleanHexPrefix(assetId)),
                neow3jService,
                NeoGetWalletBalance.class);
    }

    @Override
    public Request<?, NeoGetNewAddress> getNewAddress() {
        return new Request<>(
                "getnewaddress",
                emptyList(),
                neow3jService,
                NeoGetNewAddress.class);
    }

    @Override
    public Request<?, NeoGetWalletUnclaimedGas> getWalletUnclaimedGas() {
        return new Request<>(
                "getwalletunclaimedgas",
                emptyList(),
                neow3jService,
                NeoGetWalletUnclaimedGas.class);
    }

    @Override
    public Request<?, NeoImportPrivKey> importPrivKey(String privateKeyInWIF) {
        return new Request<>(
                "importprivkey",
                asList(privateKeyInWIF),
                neow3jService,
                NeoImportPrivKey.class);
    }

    @Override
    public Request<?, NeoListAddress> listAddress() {
        return new Request<>(
                "listaddress",
                emptyList(),
                neow3jService,
                NeoListAddress.class);
    }

    @Override
    public Request<?, NeoOpenWallet> openWallet(String walletPath, String password) {
        return new Request<>(
                "openwallet",
                asList(walletPath, password),
                neow3jService,
                NeoOpenWallet.class);
    }

    @Override
    public Request<?, NeoSendFrom> sendFrom(String fromAddress, String assetId,
            String toAddress, String value) {
        return new Request<>(
                "sendfrom",
                asList(assetId, fromAddress, toAddress, value),
                neow3jService,
                NeoSendFrom.class);
    }

    @Override
    public Request<?, NeoSendFrom> sendFrom(String fromAddress, TransactionSendAsset txSendAsset) {
        return sendFrom(fromAddress, txSendAsset.getAsset(), txSendAsset.getAddress(),
                txSendAsset.getValue());
    }

    @Override
    public Request<?, NeoSendMany> sendMany(List<TransactionSendAsset> txSendAsset) {
        return new Request<>(
                "sendmany",
                Arrays.asList(txSendAsset.stream().filter(Objects::nonNull).collect(Collectors.toList())),
                neow3jService,
                NeoSendMany.class);
    }

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

    @Override
    public Request<?, NeoSendToAddress> sendToAddress(String assetId, String toAddress,
            String value) {
        return new Request<>(
                "sendtoaddress",
                asList(assetId, toAddress, value).stream()
                        .filter((param) -> (param != null && !isEmpty(param)))
                        .collect(Collectors.toList()),
                neow3jService,
                NeoSendToAddress.class);
    }

    @Override
    public Request<?, NeoSendToAddress> sendToAddress(TransactionSendAsset txSendAsset) {
        return sendToAddress(txSendAsset.getAsset(), txSendAsset.getAddress(),
                txSendAsset.getValue());
    }

    // ApplicationLogs

    @Override
    public Request<?, NeoGetApplicationLog> getApplicationLog(String txId) {
        return new Request<>(
                "getapplicationlog",
                asList(txId),
                neow3jService,
                NeoGetApplicationLog.class);
    }

    // RpcNep5Tracker

    // TODO: 12.08.20 Michael: In the neo docs, in this request, there is a 0 as the second item in the parameter list.
    //  Check whether this was changed, or the current method works as is.
    @Override
    public Request<?, NeoGetNep5Balances> getNep5Balances(String address) {
        return new Request<>(
                "getnep5balances",
                asList(address),
                neow3jService,
                NeoGetNep5Balances.class);
    }

    @Override
    public Request<?, NeoGetNep5Transfers> getNep5Transfers(String address) {
        return new Request<>(
                "getnep5transfers",
                asList(address),
                neow3jService,
                NeoGetNep5Transfers.class);
    }

    @Override
    public Request<?, NeoGetNep5Transfers> getNep5Transfers(String address, Date until) {
        return new Request<>(
                "getnep5transfers",
                asList(address, until.getTime()),
                neow3jService,
                NeoGetNep5Transfers.class);
    }

    @Override
    public Request<?, NeoGetNep5Transfers> getNep5Transfers(String address, Date from, Date to) {
        return new Request<>(
                "getnep5transfers",
                asList(address, from.getTime(), to.getTime()),
                neow3jService,
                NeoGetNep5Transfers.class);
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
