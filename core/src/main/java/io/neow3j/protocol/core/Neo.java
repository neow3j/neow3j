package io.neow3j.protocol.core;

import io.neow3j.protocol.core.methods.response.NeoBlockCount;
import io.neow3j.protocol.core.methods.response.NeoBlockHash;
import io.neow3j.protocol.core.methods.response.NeoConnectionCount;
import io.neow3j.protocol.core.methods.response.NeoGetAccountState;
import io.neow3j.protocol.core.methods.response.NeoGetBlock;
import io.neow3j.protocol.core.methods.response.NeoGetBlockSysFee;
import io.neow3j.protocol.core.methods.response.NeoGetNewAddress;
import io.neow3j.protocol.core.methods.response.NeoGetPeers;
import io.neow3j.protocol.core.methods.response.NeoGetRawBlock;
import io.neow3j.protocol.core.methods.response.NeoGetRawMemPool;
import io.neow3j.protocol.core.methods.response.NeoGetTxOut;
import io.neow3j.protocol.core.methods.response.NeoGetValidators;
import io.neow3j.protocol.core.methods.response.NeoGetVersion;
import io.neow3j.protocol.core.methods.response.NeoGetWalletHeight;
import io.neow3j.protocol.core.methods.response.NeoListAddress;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.methods.response.NeoValidateAddress;

/**
 * Core NEO JSON-RPC API.
 */
public interface Neo {

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

}
