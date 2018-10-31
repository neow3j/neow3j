package com.axlabs.neow3j.protocol.core;

import com.axlabs.neow3j.protocol.core.methods.response.NeoBlockCount;
import com.axlabs.neow3j.protocol.core.methods.response.NeoBlockHash;
import com.axlabs.neow3j.protocol.core.methods.response.NeoConnectionCount;
import com.axlabs.neow3j.protocol.core.methods.response.NeoGetAccountState;
import com.axlabs.neow3j.protocol.core.methods.response.NeoGetBlock;
import com.axlabs.neow3j.protocol.core.methods.response.NeoGetBlockSysFee;
import com.axlabs.neow3j.protocol.core.methods.response.NeoGetNewAddress;
import com.axlabs.neow3j.protocol.core.methods.response.NeoGetPeers;
import com.axlabs.neow3j.protocol.core.methods.response.NeoGetRawBlock;
import com.axlabs.neow3j.protocol.core.methods.response.NeoGetRawMemPool;
import com.axlabs.neow3j.protocol.core.methods.response.NeoGetTxOut;
import com.axlabs.neow3j.protocol.core.methods.response.NeoGetValidators;
import com.axlabs.neow3j.protocol.core.methods.response.NeoGetVersion;
import com.axlabs.neow3j.protocol.core.methods.response.NeoGetWalletHeight;
import com.axlabs.neow3j.protocol.core.methods.response.NeoListAddress;
import com.axlabs.neow3j.protocol.core.methods.response.NeoValidateAddress;

import java.math.BigInteger;

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

}
