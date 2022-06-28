package io.neow3j.protocol.jsonrpc;

public class JsonRpcErrorConstants {

    public static final int PARSE_ERROR_CODE = -32700;
    public static final String PARSE_ERROR_MESSAGE = "Parse error";

    public static final int INVALID_REQUEST_CODE = -32600;
    public static final String INVALID_REQUEST_MESSAGE = "Invalid request";

    public static final int METHOD_NOT_FOUND_CODE = -32601;
    public static final String METHOD_NOT_FOUND_MESSAGE = "Method not found";

    public static final int INVALID_PARAMS_CODE = -32602;
    public static final String INVALID_PARAMS_MESSAGE = "Invalid params";

    public static final int INTERNAL_ERROR_CODE = -32603;
    public static final String INTERNAL_ERROR_MESSAGE = "Internal Error";

}
