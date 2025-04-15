package io.neow3j.protocol;

import io.neow3j.protocol.core.JsonRpc2_0Neow3j;
import io.neow3j.protocol.core.JsonRpc2_0Neow3jExpress;
import io.neow3j.protocol.core.NeoExpress;
import io.neow3j.protocol.core.response.NeoGetVersion;
import io.neow3j.protocol.exceptions.Neow3jBuildException;

import java.io.IOException;

import static io.neow3j.protocol.Neow3jConfig.defaultNeow3jConfig;

public abstract class Neow3jExpress extends JsonRpc2_0Neow3j implements NeoExpress {

    protected Neow3jExpress(Neow3jService neow3jService, Neow3jConfig config) throws IOException {
        super(neow3jService, config);
    }

    protected Neow3jExpress(Neow3jService neow3jService, Neow3jConfig config,
            NeoGetVersion.NeoVersion.Protocol protocol) {
        super(neow3jService, config, protocol);
    }

    /**
     * Constructs a new Neow3jExpress instance with the default configuration.
     * <p>
     * If the service is an offline service, this instance will not be able to perform any requests to a Neo node. If
     * it is not, configuration values that do not have a default value will be set based on the connected Neo node's
     * protocol.
     *
     * @param neow3jService a neow3j service instance, i.e., HTTP or IPC.
     * @return the new Neow3jExpress instance.
     */
    public static Neow3jExpress build(Neow3jService neow3jService) {
        return build(neow3jService, defaultNeow3jConfig());
    }

    /**
     * Constructs a new Neow3jExpress instance using the given configuration.
     * <p>
     * If the service is an offline service, this instance will not be able to perform any requests to a Neo node. If
     * it is not, configuration values that do not have a default value and have not been set manually in the
     * provided {@link Neow3jConfig} parameter will be set based on the connected Neo node's protocol.
     *
     * @param neow3jService a neow3j service instance, i.e., HTTP or IPC.
     * @param config        the configuration to use.
     * @return the new Neow3jExpress instance.
     */
    public static Neow3jExpress build(Neow3jService neow3jService, Neow3jConfig config) {
        try {
            return new JsonRpc2_0Neow3jExpress(neow3jService, config);
        } catch (IOException e) {
            throw new Neow3jBuildException(e.getMessage());
        }
    }

    /**
     * Constructs a new Neow3jExpress instance with an offline service and the default configuration.
     * <p>
     * The returned Neow3jExpress instance will not be able to perform any requests to a Neo node.
     *
     * @return a new Neow3jExpress instance with an {@link OfflineService}.
     */
    public static Neow3jExpress build() {
        return build(OfflineService.newInstance());
    }

}
