package io.neow3j.protocol.core;

import io.neow3j.protocol.Neow3jConfig;
import io.neow3j.protocol.Neow3jService;
import io.neow3j.protocol.core.response.NeoBlockHash;
import io.neow3j.protocol.core.response.NeoExpressGetPopulatedBlocks;
import io.neow3j.protocol.core.response.NeoExpressListContracts;
import io.neow3j.protocol.core.response.NeoGetBlock;

import static java.util.Collections.emptyList;

public class Neow3jExpress extends JsonRpc2_0Neow3j implements NeoExpress {

    private Neow3jExpress(Neow3jService neow3jService, Neow3jConfig config) {
        super(neow3jService, config);
    }

    /**
     * Constructs a new Neow3j instance with the default configuration.
     *
     * @param neow3jService neow3j service instance - i.e. HTTP or IPC
     * @return new Neow3j instance
     */
    public static Neow3jExpress build(Neow3jService neow3jService) {
        return new Neow3jExpress(neow3jService, new Neow3jConfig()) {
        };
    }

    /**
     * Constructs a new Neow3j instance using the given configuration.
     *
     * @param neow3jService neow3j service instance - i.e. HTTP or IPC
     * @param config        The configuration to use.
     * @return new Neow3j instance.
     */
    public static Neow3jExpress build(Neow3jService neow3jService, Neow3jConfig config) {
        return new Neow3jExpress(neow3jService, config);
    }

    @Override
    public Request<?, NeoBlockHash> expressShutdown() {
        return null;
    }

    @Override
    public Request<?, NeoExpressGetPopulatedBlocks> expressGetPopulatedBlocks() {
        return new Request<>(
                "expressgetpopulatedblocks",
                emptyList(),
                neow3jService,
                NeoExpressGetPopulatedBlocks.class);
    }

    @Override
    public Request<?, NeoGetBlock> getApplicationLog() {
        return null;
    }

    @Override
    public Request<?, NeoGetBlock> expressGetNep17Contracts() {
        return null;
    }

    @Override
    public Request<?, NeoGetBlock> getNep17Balances() {
        return null;
    }

    @Override
    public Request<?, NeoGetBlock> getNep17Transfers() {
        return null;
    }

    @Override
    public Request<?, NeoGetBlock> expressGetContractStorage() {
        return null;
    }

    @Override
    public Request<?, NeoExpressListContracts> expressListContracts() {
        return new Request<>(
                "expresslistcontracts",
                emptyList(),
                neow3jService,
                NeoExpressListContracts.class);
    }

    @Override
    public Request<?, NeoGetBlock> expressCreateCheckpoint() {
        return null;
    }

    @Override
    public Request<?, NeoGetBlock> expressListOracleRequests() {
        return null;
    }

    @Override
    public Request<?, NeoGetBlock> expressCreateOracleResponseTx() {
        return null;
    }

}
