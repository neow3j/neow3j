package io.neow3j.protocol;

import io.neow3j.protocol.core.JsonRpc2_0Neow3j;
import io.neow3j.protocol.core.NeoExpress;
import io.neow3j.protocol.core.Request;
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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/**
 * JSON-RPC 2.0 factory implementation specific to Neo-express nodes.
 */
public class Neow3jExpress extends JsonRpc2_0Neow3j implements NeoExpress {

    private Neow3jExpress(Neow3jService neow3jService, Neow3jConfig config) {
        super(neow3jService, config);
    }

    /**
     * Constructs a new Neow3jExpress instance with the default configuration.
     *
     * @param neow3jService a neow3j service instance, i.e., HTTP or IPC.
     * @return the new Neow3jExpress instance
     */
    public static Neow3jExpress build(Neow3jService neow3jService) {
        return new Neow3jExpress(neow3jService, new Neow3jConfig()) {
        };
    }

    /**
     * Constructs a new Neow3jExpress instance using the given configuration.
     *
     * @param neow3jService a neow3j service instance, i.e., HTTP or IPC.
     * @param config        the configuration to use.
     * @return the new Neow3jExpress instance.
     */
    public static Neow3jExpress build(Neow3jService neow3jService, Neow3jConfig config) {
        return new Neow3jExpress(neow3jService, config);
    }

    @Override
    public Request<?, NeoExpressGetPopulatedBlocks> expressGetPopulatedBlocks() {
        return new Request<>(
                "expressgetpopulatedblocks",
                emptyList(),
                neow3jService,
                NeoExpressGetPopulatedBlocks.class);
    }

    /**
     * Gets all deployed contracts that follow the NEP-17 standard.
     * <p>
     * Can only be used on a Neo-express node.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoExpressGetNep17Contracts> expressGetNep17Contracts() {
        return new Request<>(
                "expressgetnep17contracts",
                emptyList(),
                neow3jService,
                NeoExpressGetNep17Contracts.class);
    }

    /**
     * Gets the contract storage.
     * <p>
     * Can only be used on a Neo-express node.
     *
     * @param contractHash the contract hash.
     * @return the request object.
     */
    @Override
    public Request<?, NeoExpressGetContractStorage> expressGetContractStorage(Hash160 contractHash) {
        return new Request<>(
                "expressgetcontractstorage",
                asList(contractHash),
                neow3jService,
                NeoExpressGetContractStorage.class);
    }

    /**
     * Gets a list of all deployed contracts.
     * <p>
     * Can only be used on a Neo-express node.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoExpressListContracts> expressListContracts() {
        return new Request<>(
                "expresslistcontracts",
                emptyList(),
                neow3jService,
                NeoExpressListContracts.class);
    }

    /**
     * Creates a checkpoint of the Neo-express node and writes it to a file in the root of the
     * Neo-express instance.
     * <p>
     * Can only be used on a Neo-express node.
     *
     * @param filename the filename of the checkpoint file.
     * @return the request object.
     */
    @Override
    public Request<?, NeoExpressCreateCheckpoint> expressCreateCheckpoint(String filename) {
        return new Request<>(
                "expresscreatecheckpoint",
                asList(filename),
                neow3jService,
                NeoExpressCreateCheckpoint.class);
    }

    /**
     * Gets a list of all current oracle requests.
     * <p>
     * Can only be used on a Neo-express node.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoExpressListOracleRequests> expressListOracleRequests() {
        return new Request<>(
                "expresslistoraclerequests",
                emptyList(),
                neow3jService,
                NeoExpressListOracleRequests.class);
    }

    /**
     * Creates an oracle response transaction.
     * <p>
     * Can only be used on a Neo-express node.
     *
     * @param response the oracle response object.
     * @return the request object.
     */
    @Override
    public Request<?, NeoExpressCreateOracleResponseTx> expressCreateOracleResponseTx(OracleResponse response) {
        return new Request<>(
                "expresscreateoracleresponsetx",
                asList(response),
                neow3jService,
                NeoExpressCreateOracleResponseTx.class);
    }

    /**
     * Shuts down the neo-express instance.
     * <p>
     * Can only be used on a Neo-express node.
     *
     * @return the request object.
     */
    @Override
    public Request<?, NeoExpressShutdown> expressShutdown() {
        return new Request<>(
                "expressshutdown",
                emptyList(),
                neow3jService,
                NeoExpressShutdown.class);
    }

}
