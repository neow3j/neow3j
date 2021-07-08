package io.neow3j.protocol.core;

import io.neow3j.protocol.core.response.NeoBlockHash;
import io.neow3j.protocol.core.response.NeoExpressListContracts;
import io.neow3j.protocol.core.response.NeoGetBlock;

/**
 * NEO JSON-RPC API specific for Neo-express.
 */
public interface NeoExpress {

    Request<?, NeoBlockHash> expressShutdown();

    Request<?, NeoGetBlock> expressGetPopulatedBlocks();

    Request<?, NeoGetBlock> getApplicationLog();

    Request<?, NeoGetBlock> expressGetNep17Contracts();

    Request<?, NeoGetBlock> getNep17Balances();

    Request<?, NeoGetBlock> getNep17Transfers();

    Request<?, NeoGetBlock> expressGetContractStorage();

    Request<?, NeoExpressListContracts> expressListContracts();

    Request<?, NeoGetBlock> expressCreateCheckpoint();

    Request<?, NeoGetBlock> expressListOracleRequests();

    Request<?, NeoGetBlock> expressCreateOracleResponseTx();

}
