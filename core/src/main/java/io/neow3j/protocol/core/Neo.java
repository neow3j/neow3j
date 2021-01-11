package io.neow3j.protocol.core;

import io.neow3j.contract.ContractParameter;
import io.neow3j.protocol.core.methods.response.NeoBlockCount;
import io.neow3j.protocol.core.methods.response.NeoBlockHash;
import io.neow3j.protocol.core.methods.response.NeoCalculateNetworkFee;
import io.neow3j.protocol.core.methods.response.NeoCloseWallet;
import io.neow3j.protocol.core.methods.response.NeoConnectionCount;
import io.neow3j.protocol.core.methods.response.NeoDumpPrivKey;
import io.neow3j.protocol.core.methods.response.NeoGetApplicationLog;
import io.neow3j.protocol.core.methods.response.NeoGetCommittee;
import io.neow3j.protocol.core.methods.response.NeoGetUnclaimedGas;
import io.neow3j.protocol.core.methods.response.NeoGetWalletBalance;
import io.neow3j.protocol.core.methods.response.NeoGetBlock;
import io.neow3j.protocol.core.methods.response.NeoGetContractState;
import io.neow3j.protocol.core.methods.response.NeoGetMemPool;
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
import io.neow3j.protocol.core.methods.response.NeoGetWalletUnclaimedGas;
import io.neow3j.protocol.core.methods.response.NeoGetNextBlockValidators;
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
import io.neow3j.transaction.Signer;

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

    Request<?, NeoGetTransactionHeight> getTransactionHeight(String txId);

    Request<?, NeoGetNextBlockValidators> getNextBlockValidators();

    Request<?, NeoGetCommittee> getCommittee();

    // Node Methods

    Request<?, NeoConnectionCount> getConnectionCount();

    Request<?, NeoGetPeers> getPeers();

    Request<?, NeoGetVersion> getVersion();

    Request<?, NeoSendRawTransaction> sendRawTransaction(String rawTransactionHex);

    Request<?, NeoSubmitBlock> submitBlock(String serializedBlockAsHex);

    // SmartContract Methods

    Request<?, NeoInvokeFunction> invokeFunction(String contractScriptHash, String functionName,
            Signer... signers);

    Request<?, NeoInvokeFunction> invokeFunction(String contractScriptHash, String functionName,
            List<ContractParameter> params, Signer... signers);

    Request<?, NeoInvokeScript> invokeScript(String script, Signer... signers);

    Request<?, NeoGetUnclaimedGas> getUnclaimedGas(String address);

    // Utilities Methods

    Request<?, NeoListPlugins> listPlugins();

    Request<?, NeoValidateAddress> validateAddress(String address);

    // Wallet Methods

    Request<?, NeoCloseWallet> closeWallet();

    Request<?, NeoOpenWallet> openWallet(String walletPath, String password);

    Request<?, NeoDumpPrivKey> dumpPrivKey(String address);

    Request<?, NeoGetWalletBalance> getWalletBalance(String assetId);

    Request<?, NeoGetNewAddress> getNewAddress();

    Request<?, NeoGetWalletUnclaimedGas> getWalletUnclaimedGas();

    Request<?, NeoImportPrivKey> importPrivKey(String privateKeyInWIF);

    Request<?, NeoCalculateNetworkFee> calculateNetworkFee(String transactionHex);

    Request<?, NeoListAddress> listAddress();

    Request<?, NeoSendFrom> sendFrom(String fromAddress, String assetId, String toAddress,
            String value);

    Request<?, NeoSendFrom> sendFrom(String fromAddress, TransactionSendAsset txSendAsset);

    Request<?, NeoSendMany> sendMany(List<TransactionSendAsset> txSendAsset);

    Request<?, NeoSendMany> sendMany(String fromAddress, List<TransactionSendAsset> txSendAsset);

    Request<?, NeoSendToAddress> sendToAddress(String assetId, String toAddress, String value);

    Request<?, NeoSendToAddress> sendToAddress(TransactionSendAsset txSendAsset);

    // RpcNep17Tracker

    Request<?, NeoGetNep17Transfers> getNep17Transfers(String address);

    Request<?, NeoGetNep17Transfers> getNep17Transfers(String address, Date until);

    Request<?, NeoGetNep17Transfers> getNep17Transfers(String address, Date from, Date to);

    Request<?, NeoGetNep17Balances> getNep17Balances(String address);

    // ApplicationLogs

    Request<?, NeoGetApplicationLog> getApplicationLog(String txId);

}
