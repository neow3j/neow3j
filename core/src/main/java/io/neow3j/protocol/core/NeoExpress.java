package io.neow3j.protocol.core;

import io.neow3j.protocol.core.response.NeoExpressCreateCheckpoint;
import io.neow3j.protocol.core.response.NeoExpressCreateOracleResponseTx;
import io.neow3j.protocol.core.response.NeoExpressGetContractStorage;
import io.neow3j.protocol.core.response.NeoExpressGetNep17Contracts;
import io.neow3j.protocol.core.response.NeoExpressGetPopulatedBlocks;
import io.neow3j.protocol.core.response.NeoExpressListContracts;
import io.neow3j.protocol.core.response.NeoExpressListOracleRequests;
import io.neow3j.protocol.core.response.NeoExpressShutdown;
import io.neow3j.protocol.core.response.OracleResponse;
import io.neow3j.types.Hash160;

/**
 * NEO JSON-RPC API specific for Neo-express nodes.
 */
public interface NeoExpress {

    Request<?, NeoExpressGetPopulatedBlocks> expressGetPopulatedBlocks();

    Request<?, NeoExpressGetNep17Contracts> expressGetNep17Contracts();

    Request<?, NeoExpressGetContractStorage> expressGetContractStorage(Hash160 contractHash);

    Request<?, NeoExpressListContracts> expressListContracts();

    Request<?, NeoExpressCreateCheckpoint> expressCreateCheckpoint(String filename);

    Request<?, NeoExpressListOracleRequests> expressListOracleRequests();

    Request<?, NeoExpressCreateOracleResponseTx> expressCreateOracleResponseTx(OracleResponse oracleResponse);

    Request<?, NeoExpressShutdown> expressShutdown();

}
