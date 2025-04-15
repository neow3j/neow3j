package io.neow3j.protocol.core;

import io.neow3j.protocol.Neow3jConfig;
import io.neow3j.protocol.Neow3jExpress;
import io.neow3j.protocol.Neow3jService;
import io.neow3j.protocol.core.response.NeoExpressCreateCheckpoint;
import io.neow3j.protocol.core.response.NeoExpressCreateOracleResponseTx;
import io.neow3j.protocol.core.response.NeoExpressGetContractStorage;
import io.neow3j.protocol.core.response.NeoExpressGetNep17Contracts;
import io.neow3j.protocol.core.response.NeoExpressGetPopulatedBlocks;
import io.neow3j.protocol.core.response.NeoExpressListContracts;
import io.neow3j.protocol.core.response.NeoExpressListOracleRequests;
import io.neow3j.protocol.core.response.NeoExpressShutdown;
import io.neow3j.protocol.core.response.NeoGetVersion;
import io.neow3j.protocol.core.response.OracleResponse;
import io.neow3j.types.Hash160;

import java.io.IOException;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/**
 * JSON-RPC 2.0 factory implementation specific to Neo-express nodes.
 */
public class JsonRpc2_0Neow3jExpress extends Neow3jExpress {

    /**
     * Constructs a new JsonRpc2_0Neow3jExpress instance.
     * <p>
     * If the service is an offline service, this instance will not be able to perform any requests to a Neo node. If
     * it is not, configuration values that do not have a default value and have not been set manually in the
     * provided {@link Neow3jConfig} parameter will be set based on the connected Neo node's protocol.
     *
     * @param neow3jService a neow3j service.
     * @param config        the configuration to use.
     * @throws IOException if the service is not an offline service and there was a problem fetching information from
     *                     the Neo node.
     */
    public JsonRpc2_0Neow3jExpress(Neow3jService neow3jService, Neow3jConfig config) throws IOException {
        super(neow3jService, config);
    }

    /**
     * Constructs a new JsonRpc2_0Neow3jExpress instance.
     * <p>
     * Does not require a connection to a Neo node.
     *
     * @param neow3jService a neow3j service.
     * @param config        the configuration to use.
     * @param protocol      the protocol to use.
     */
    public JsonRpc2_0Neow3jExpress(Neow3jService neow3jService, Neow3jConfig config,
            NeoGetVersion.NeoVersion.Protocol protocol) {
        super(neow3jService, config, protocol);
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
