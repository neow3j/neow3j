package io.neow3j.protocol.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.neow3j.protocol.deserializer.KeepAsJsonDeserialzier;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;

/**
 * JSON-RPC response type.
 *
 * @param <T> the object type contained within the response
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response<T> {
    private long id;
    private String jsonrpc;
    private T result;
    private Error error;
    private String rawResponse;

    public Response() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public T getResult() {
        throwOnError();
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public boolean hasError() {
        return error != null;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }

    public void throwOnError() {
        if (hasError()) {
            throw new RpcResponseErrorException(error);
        }
    }

    public static class Error {
        private int code;
        private String message;

        @JsonDeserialize(using = KeepAsJsonDeserialzier.class)
        private String data;

        public Error() {
        }

        public Error(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Error)) {
                return false;
            }

            Error error = (Error) o;

            if (getCode() != error.getCode()) {
                return false;
            }
            if (getMessage() != null
                    ? !getMessage().equals(error.getMessage()) : error.getMessage() != null) {
                return false;
            }
            return getData() != null ? getData().equals(error.getData()) : error.getData() == null;
        }

        @Override
        public int hashCode() {
            int result = getCode();
            result = 31 * result + (getMessage() != null ? getMessage().hashCode() : 0);
            result = 31 * result + (getData() != null ? getData().hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Error{" +
                    "code=" + getCode() +
                    ", message='" + getMessage() + '\'' +
                    ", data='" + getData() + '\'' +
                    "}";
        }

    }

}
