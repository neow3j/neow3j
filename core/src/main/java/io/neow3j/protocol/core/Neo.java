package io.neow3j.protocol.core;

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
import io.neow3j.transaction.Signer;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * Core NEO JSON-RPC API.
 */
public interface Neo {

    //region Blockchain Methods

    Request<?, NeoBlockHash> getBestBlockHash();

    Request<?, NeoBlockHash> getBlockHash(BigInteger blockIndex);

    Request<?, NeoGetBlock> getBlock(Hash256 blockHash, boolean returnFullTransactionObjects);

    Request<?, NeoGetBlock> getBlock(BigInteger blockIndex, boolean returnFullTransactionObjects);

    Request<?, NeoGetRawBlock> getRawBlock(Hash256 blockHash);

    Request<?, NeoGetRawBlock> getRawBlock(BigInteger blockIndex);

    Request<?, NeoBlockHeaderCount> getBlockHeaderCount();

    Request<?, NeoBlockCount> getBlockCount();

    Request<?, NeoGetBlock> getBlockHeader(Hash256 blockHash);

    Request<?, NeoGetBlock> getBlockHeader(BigInteger blockIndex);

    Request<?, NeoGetRawBlock> getRawBlockHeader(Hash256 blockHash);

    Request<?, NeoGetRawBlock> getRawBlockHeader(BigInteger blockIndex);

    Request<?, NeoGetNativeContracts> getNativeContracts();

    Request<?, NeoGetContractState> getContractState(Hash160 contractHash);

    Request<?, NeoGetContractState> getNativeContractState(String contractName);

    Request<?, NeoGetMemPool> getMemPool();

    Request<?, NeoGetRawMemPool> getRawMemPool();

    Request<?, NeoGetTransaction> getTransaction(Hash256 txHash);

    Request<?, NeoGetRawTransaction> getRawTransaction(Hash256 txHash);

    Request<?, NeoGetStorage> getStorage(Hash160 contractHash, String keyHexString);

    Request<?, NeoGetTransactionHeight> getTransactionHeight(Hash256 txHash);

    Request<?, NeoGetNextBlockValidators> getNextBlockValidators();

    Request<?, NeoGetCommittee> getCommittee();

    //endregion

    //region Node Methods

    Request<?, NeoConnectionCount> getConnectionCount();

    Request<?, NeoGetPeers> getPeers();

    Request<?, NeoGetVersion> getVersion();

    Request<?, NeoSendRawTransaction> sendRawTransaction(String rawTransactionHex);

    Request<?, NeoSubmitBlock> submitBlock(String serializedBlockAsHex);

    //endregion

    //region SmartContract Methods

    Request<?, NeoInvokeFunction> invokeFunction(Hash160 contractHash, String functionName, Signer... signers);

    Request<?, NeoInvokeFunction> invokeFunction(Hash160 contractHash, String functionName,
            List<ContractParameter> params, Signer... signers);

    Request<?, NeoInvokeFunction> invokeFunctionDiagnostics(Hash160 contractHash, String functionName,
            Signer... signers);

    Request<?, NeoInvokeFunction> invokeFunctionDiagnostics(Hash160 contractHash, String functionName,
            List<ContractParameter> params, Signer... signers);

    Request<?, NeoInvokeScript> invokeScript(String scriptHex, Signer... signers);

    Request<?, NeoInvokeScript> invokeScriptDiagnostics(String scriptHex, Signer... signers);

    Request<?, NeoTraverseIterator> traverseIterator(String sessionId, String iteratorId, int count);

    Request<?, NeoTerminateSession> terminateSession(String sessionId);

    Request<?, NeoInvokeContractVerify> invokeContractVerify(Hash160 contractHash,
            List<ContractParameter> methodParameters, Signer... signers);

    Request<?, NeoGetUnclaimedGas> getUnclaimedGas(Hash160 scriptHash);

    //endregion

    //region Utilities Methods

    Request<?, NeoListPlugins> listPlugins();

    Request<?, NeoValidateAddress> validateAddress(String address);

    //endregion

    //region Wallet Methods

    Request<?, NeoCloseWallet> closeWallet();

    Request<?, NeoOpenWallet> openWallet(String walletPath, String password);

    Request<?, NeoDumpPrivKey> dumpPrivKey(Hash160 scriptHash);

    Request<?, NeoGetWalletBalance> getWalletBalance(Hash160 tokenHash);

    Request<?, NeoGetNewAddress> getNewAddress();

    Request<?, NeoGetWalletUnclaimedGas> getWalletUnclaimedGas();

    Request<?, NeoImportPrivKey> importPrivKey(String privateKeyInWIF);

    Request<?, NeoCalculateNetworkFee> calculateNetworkFee(String transactionHex);

    Request<?, NeoListAddress> listAddress();

    Request<?, NeoSendFrom> sendFrom(Hash160 tokenHash, Hash160 from, Hash160 to, BigInteger amount);

    Request<?, NeoSendFrom> sendFrom(Hash160 from, TransactionSendToken txSendToken);

    Request<?, NeoSendMany> sendMany(List<TransactionSendToken> txSendTokens);

    Request<?, NeoSendMany> sendMany(Hash160 from, List<TransactionSendToken> txSendTokens);

    Request<?, NeoSendToAddress> sendToAddress(Hash160 tokenHash, Hash160 to, BigInteger amount);

    Request<?, NeoSendToAddress> sendToAddress(TransactionSendToken txSendToken);

    //endregion

    //region TokenTracker

    Request<?, NeoGetNep17Balances> getNep17Balances(Hash160 scriptHash);

    Request<?, NeoGetNep17Transfers> getNep17Transfers(Hash160 scriptHash);

    Request<?, NeoGetNep17Transfers> getNep17Transfers(Hash160 scriptHash, Date from);

    Request<?, NeoGetNep17Transfers> getNep17Transfers(Hash160 scriptHash, Date from, Date to);

    Request<?, NeoGetNep11Balances> getNep11Balances(Hash160 scriptHash);

    Request<?, NeoGetNep11Transfers> getNep11Transfers(Hash160 scriptHash);

    Request<?, NeoGetNep11Transfers> getNep11Transfers(Hash160 scriptHash, Date from);

    Request<?, NeoGetNep11Transfers> getNep11Transfers(Hash160 scriptHash, Date from, Date to);

    Request<?, NeoGetNep11Properties> getNep11Properties(Hash160 scriptHash, String tokenId);

    //endregion

    //region ApplicationLogs

    Request<?, NeoGetApplicationLog> getApplicationLog(Hash256 txHash);

    //endregion

    //region StateService

    Request<?, NeoGetStateRoot> getStateRoot(Long blockIndex);

    Request<?, NeoGetProof> getProof(Hash256 rootHash, Hash160 contractHash, String storageKeyHex);

    Request<?, NeoVerifyProof> verifyProof(Hash256 rootHash, String proofDataHex);

    Request<?, NeoGetStateHeight> getStateHeight();

    Request<?, NeoGetState> getState(Hash256 rootHash, Hash160 contractHash, String keyHex);

    Request<?, NeoFindStates> findStates(Hash256 rootHash, Hash160 contractHash, String keyPrefixHex,
            String startKeyHex, Integer countFindResultItems);

    Request<?, NeoFindStates> findStates(Hash256 rootHash, Hash160 contractHash, String keyPrefixHex,
            String startKeyHex);

    Request<?, NeoFindStates> findStates(Hash256 rootHash, Hash160 contractHash, String keyPrefixHex,
            Integer countFindResultItems);

    Request<?, NeoFindStates> findStates(Hash256 rootHash, Hash160 contractHash, String keyPrefixHex);

    //endregion

}
