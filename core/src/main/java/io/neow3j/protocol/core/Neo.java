package io.neow3j.protocol.core;

import io.neow3j.contract.ContractParameter;
import io.neow3j.protocol.core.methods.response.NeoBlockCount;
import io.neow3j.protocol.core.methods.response.NeoBlockHash;
import io.neow3j.protocol.core.methods.response.NeoCloseWallet;
import io.neow3j.protocol.core.methods.response.NeoConnectionCount;
import io.neow3j.protocol.core.methods.response.NeoDumpPrivKey;
import io.neow3j.protocol.core.methods.response.NeoGetApplicationLog;
import io.neow3j.protocol.core.methods.response.NeoGetBalance;
import io.neow3j.protocol.core.methods.response.NeoGetBlock;
import io.neow3j.protocol.core.methods.response.NeoGetBlockSysFee;
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
import java.util.Date;
import java.util.List;

/**
 * Core NEO JSON-RPC API.
 */
public interface Neo {

    // Blockchain Methods

    Request<?, NeoBlockHash> getBestBlockHash();

    Request<?, NeoBlockHash> getBlockHash(BlockParameterIndex blockIndex);

    Request<?, NeoGetBlock> getBlock(String address, boolean returnFullTransactionObjects);

    Request<?, NeoGetRawBlock> getRawBlock(String address);

    Request<?, NeoGetBlock> getBlock(BlockParameterIndex blockIndex,
            boolean returnFullTransactionObjects);

    Request<?, NeoGetRawBlock> getRawBlock(BlockParameterIndex blockIndex);

    Request<?, NeoBlockCount> getBlockCount();

    Request<?, NeoGetBlock> getBlockHeader(String hash);

    Request<?, NeoGetBlock> getBlockHeader(BlockParameterIndex blockIndex);

    Request<?, NeoGetRawBlock> getRawBlockHeader(String hash);

    Request<?, NeoGetRawBlock> getRawBlockHeader(BlockParameterIndex blockIndex);

    Request<?, NeoGetContractState> getContractState(String scriptHash);

    Request<?, NeoGetMemPool> getMemPool();

    Request<?, NeoGetRawMemPool> getRawMemPool();

    Request<?, NeoGetTransaction> getTransaction(String txId);

    Request<?, NeoGetRawTransaction> getRawTransaction(String txId);

    Request<?, NeoGetStorage> getStorage(String contractAddress, HexParameter keyToLookUp);

    Request<?, NeoGetStorage> getStorage(String contractAddress, String keyToLookUpAsHexString);

    // TODO: 11.02.20 Guil: test missing
    Request<?, NeoGetTransactionHeight> getTransactionHeight(String txId);

    Request<?, NeoGetValidators> getValidators();

    // Node Methods

    Request<?, NeoConnectionCount> getConnectionCount();

    Request<?, NeoGetPeers> getPeers();

    Request<?, NeoGetVersion> getVersion();

    Request<?, NeoSendRawTransaction> sendRawTransaction(String rawTransactionHex);

    Request<?, NeoSubmitBlock> submitBlock(String serializedBlockAsHex);

    // SmartContract Methods

    Request<?, NeoInvokeFunction> invokeFunction(String contractScriptHash, String functionName,
            String... witnesses);

    Request<?, NeoInvokeFunction> invokeFunction(String contractScriptHash, String functionName,
            List<ContractParameter> params, String... witnesses);

    Request<?, NeoInvokeScript> invokeScript(String script, String... witnesses);

    // Utilities Methods

    Request<?, NeoListPlugins> listPlugins();

    Request<?, NeoValidateAddress> validateAddress(String address);

    // Wallet Methods

    Request<?, NeoCloseWallet> closeWallet();

    Request<?, NeoOpenWallet> openWallet(String walletPath, String password);

    Request<?, NeoDumpPrivKey> dumpPrivKey(String address);

    Request<?, NeoGetBalance> getBalance(String assetId);

    Request<?, NeoGetNewAddress> getNewAddress();

    Request<?, NeoGetUnclaimedGas> getUnclaimedGas();

    Request<?, NeoImportPrivKey> importPrivKey(String privateKeyInWIF);

    Request<?, NeoListAddress> listAddress();

    Request<?, NeoSendFrom> sendFrom(String fromAddress, String assetId, String toAddress,
            String value);

    Request<?, NeoSendFrom> sendFrom(String fromAddress, TransactionSendAsset txSendAsset);

    Request<?, NeoSendMany> sendMany(List<TransactionSendAsset> txSendAsset);

    Request<?, NeoSendMany> sendMany(List<TransactionSendAsset> txSendAsset, String fee);

    Request<?, NeoSendMany> sendMany(List<TransactionSendAsset> txSendAsset, String fee,
            String changeAddress);

    Request<?, NeoSendToAddress> sendToAddress(String assetId, String toAddress, String value);

    Request<?, NeoSendToAddress> sendToAddress(TransactionSendAsset txSendAsset);

    Request<?, NeoSendToAddress> sendToAddress(String assetId, String toAddress, String value,
            String fee);

    Request<?, NeoSendToAddress> sendToAddress(TransactionSendAsset txSendAsset, String fee);

    Request<?, NeoSendToAddress> sendToAddress(String assetId, String toAddress, String value,
            String fee, String changeAddress);

    Request<?, NeoSendToAddress> sendToAddress(TransactionSendAsset txSendAsset, String fee,
            String changeAddress);

    // RpcNep5Tracker

    Request<?, NeoGetNep5Transfers> getNep5Transfers(String address);

    Request<?, NeoGetNep5Transfers> getNep5Transfers(String address, Date until);

    Request<?, NeoGetNep5Transfers> getNep5Transfers(String address, Date from, Date to);

    Request<?, NeoGetNep5Balances> getNep5Balances(String address);

    // ApplicationLogs

    Request<?, NeoGetApplicationLog> getApplicationLog(String txId);

}
