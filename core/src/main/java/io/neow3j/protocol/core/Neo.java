package io.neow3j.protocol.core;

import io.neow3j.contract.ContractParameter;
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

import java.util.List;

/**
 * Core NEO JSON-RPC API.
 */
public interface Neo {

    // API 2.9.*

    Request<?, NeoGetVersion> getVersion();

    Request<?, NeoGetBlock> getBlock(String address, boolean returnFullTransactionObjects);

    Request<?, NeoGetRawBlock> getRawBlock(String address);

    Request<?, NeoGetBlock> getBlock(BlockParameterIndex blockIndex, boolean returnFullTransactionObjects);

    Request<?, NeoGetRawBlock> getRawBlock(BlockParameterIndex blockIndex);

    Request<?, NeoBlockCount> getBlockCount();

    Request<?, NeoBlockHash> getBestBlockHash();

    Request<?, NeoBlockHash> getBlockHash(BlockParameterIndex blockIndex);

    Request<?, NeoGetBlock> getBlockHeader(String hash);

    Request<?, NeoGetBlock> getBlockHeader(BlockParameterIndex blockIndex);

    Request<?, NeoGetRawBlock> getRawBlockHeader(String hash);

    Request<?, NeoGetRawBlock> getRawBlockHeader(BlockParameterIndex blockIndex);

    Request<?, NeoConnectionCount> getConnectionCount();

    Request<?, NeoListAddress> listAddress();

    Request<?, NeoGetPeers> getPeers();

    Request<?, NeoGetRawMemPool> getRawMemPool();

    Request<?, NeoGetValidators> getValidators();

    Request<?, NeoValidateAddress> validateAddress(String address);

    Request<?, NeoGetAccountState> getAccountState(String address);

    Request<?, NeoGetNewAddress> getNewAddress();

    Request<?, NeoGetWalletHeight> getWalletHeight();

    Request<?, NeoGetBlockSysFee> getBlockSysFee(BlockParameterIndex blockIndex);

    Request<?, NeoGetTxOut> getTxOut(String transactionHash, int txIndex);

    Request<?, NeoSendRawTransaction> sendRawTransaction(String rawTransactionHex);

    Request<?, NeoSendToAddress> sendToAddress(String assetId, String toAddress, String value);

    Request<?, NeoSendToAddress> sendToAddress(String assetId, String toAddress, String value, String fee);

    Request<?, NeoSendToAddress> sendToAddress(String assetId, String toAddress, String value, String fee, String changeAddress);

    Request<?, NeoGetTransaction> getTransaction(String txId);

    Request<?, NeoGetRawTransaction> getRawTransaction(String txId);

    Request<?, NeoGetBalance> getBalance(String assetId);

    Request<?, NeoGetAssetState> getAssetState(String assetId);

    Request<?, NeoSendMany> sendMany(List<TransactionOutput> outputs);

    Request<?, NeoSendMany> sendMany(List<TransactionOutput> outputs, String fee);

    Request<?, NeoSendMany> sendMany(List<TransactionOutput> outputs, String fee, String changeAddress);

    Request<?, NeoDumpPrivKey> dumpPrivKey(String address);

    Request<?, NeoGetStorage> getStorage(String contractAddress, HexParameter keyToLookUp);

    Request<?, NeoGetStorage> getStorage(String contractAddress, String keyToLookUpAsHexString);

    Request<?, NeoInvoke> invoke(String contractScriptHash, List<ContractParameter> params);

    Request<?, NeoInvokeFunction> invokeFunction(String contractScriptHash, String functionName);

    Request<?, NeoInvokeFunction> invokeFunction(String contractScriptHash, String functionName, List<ContractParameter> params);

    Request<?, NeoInvokeScript> invokeScript(String script);

    Request<?, NeoGetContractState> getContractState(String scriptHash);

    Request<?, NeoSubmitBlock> submitBlock(String serializedBlockAsHex);

    // API 2.10.*

    Request<?, NeoGetUnspents> getUnspents(String address);

    Request<?, NeoGetNep5Balances> getNep5Balances(String address);

    Request<?, NeoGetClaimable> getClaimable(String address);

    Request<?, NeoListPlugins> listPlugins();

    // Plugins

    Request<?, NeoGetApplicationLog> getApplicationLog(String txId);

}
