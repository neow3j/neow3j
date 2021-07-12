package io.neow3j.protocol.core;

import io.neow3j.protocol.core.response.NeoBlockHash;
import io.neow3j.protocol.core.response.NeoExpressGetContractStorage;
import io.neow3j.protocol.core.response.NeoExpressGetNep17Contracts;
import io.neow3j.protocol.core.response.NeoExpressGetPopulatedBlocks;
import io.neow3j.protocol.core.response.NeoExpressListContracts;
import io.neow3j.protocol.core.response.NeoGetBlock;
import io.neow3j.types.Hash160;

/**
 * NEO JSON-RPC API specific for Neo-express.
 */
public interface NeoExpress {

    Request<?, NeoBlockHash> expressShutdown();

    Request<?, NeoExpressGetPopulatedBlocks> expressGetPopulatedBlocks();

    Request<?, NeoGetBlock> getApplicationLog();

    Request<?, NeoExpressGetNep17Contracts> expressGetNep17Contracts();

    Request<?, NeoExpressGetContractStorage> expressGetContractStorage(Hash160 contractHash);

    Request<?, NeoExpressListContracts> expressListContracts();

    Request<?, NeoGetBlock> expressCreateCheckpoint();

    Request<?, NeoGetBlock> expressListOracleRequests();

    Request<?, NeoGetBlock> expressCreateOracleResponseTx();

}
