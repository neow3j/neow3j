package io.neow3j.contract;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.OracleRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents an Oracle contract and provides methods to invoke it.
 */
public class OracleContract extends SmartContract {

    private static final String NAME = "Oracle";

    public static final ScriptHash SCRIPT_HASH = ScriptHash.fromScript(
            new ScriptBuilder()
                    .pushData(NAME)
                    .sysCall(InteropServiceCode.NEO_NATIVE_CALL)
                    .toArray());

    private static final String GET_REQUEST = "getRequest";
    private static final String GET_REQUESTS = "getRequests";
    private static final String GET_REQUESTS_BY_URL = "getRequestsByUrl";

    /**
     * Constructs a new <tt>Oracle</tt> that uses the given {@link Neow3j} instance for
     * invocations.
     *
     * @param neow The {@link Neow3j} instance to use for invocations.
     */
    public OracleContract(Neow3j neow) {
        super(SCRIPT_HASH, neow);
    }

    public OracleRequest getRequest(int id) throws IOException {
        NeoInvokeFunction invokeFunction = neow.invokeFunction(SCRIPT_HASH.toString(), GET_REQUEST,
                Arrays.asList(ContractParameter.integer(id)))
                .send();
        // TODO: 10.12.20 Michael: deserialize oracle request into OracleRequest dto
        return new OracleRequest();
    }

    public List<OracleRequest> getRequests() throws IOException {
        List<OracleRequest> requests = new ArrayList<>();
        NeoInvokeFunction invokeFunction = neow.invokeFunction(SCRIPT_HASH.toString(), GET_REQUESTS).send();
        // TODO: 10.12.20 Michael: deserialize oracle request into OracleRequest dto and collect in list.
        return requests;
    }

    public List<OracleRequest> getRequestsByUrl(String url) throws IOException {
        List<OracleRequest> requests = new ArrayList<>();
        NeoInvokeFunction invokeFunction = neow.invokeFunction(SCRIPT_HASH.toString(), GET_REQUESTS_BY_URL,
                Arrays.asList(ContractParameter.string(url)))
                .send();
        // TODO: 10.12.20 Michael: deserialize oracle request into OracleRequest dto and collect in list.
        return requests;
    }
}
