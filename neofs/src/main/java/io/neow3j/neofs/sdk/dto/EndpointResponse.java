package io.neow3j.neofs.sdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.protobuf.util.JsonFormat;
import neo.fs.v2.netmap.Types;

import java.io.IOException;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EndpointResponse {

    @JsonProperty(value = "netmap.NodeInfo")
    @JsonDeserialize(using = NodeInfoDeserializer.class)
    private Types.NodeInfo nodeInfo;

    @JsonProperty(value = "version.Version")
    @JsonDeserialize(using = LatestVersionDeserializer.class)
    private neo.fs.v2.refs.Types.Version version;

    public EndpointResponse() {
    }

    public Types.NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    public neo.fs.v2.refs.Types.Version getVersion() {
        return version;
    }

    static class NodeInfoDeserializer extends JsonDeserializer<Types.NodeInfo> {

        @Override
        public Types.NodeInfo deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            String s = jp.readValueAs(String.class);
            Types.NodeInfo.Builder b = Types.NodeInfo.newBuilder();
            JsonFormat.parser().ignoringUnknownFields().merge(s, b);
            return b.build();
        }

    }

    static class LatestVersionDeserializer extends JsonDeserializer<neo.fs.v2.refs.Types.Version> {

        @Override
        public neo.fs.v2.refs.Types.Version deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            String s = jp.readValueAs(String.class);
            neo.fs.v2.refs.Types.Version.Builder b = neo.fs.v2.refs.Types.Version.newBuilder();
            JsonFormat.parser().ignoringUnknownFields().merge(s, b);
            return b.build();
        }

    }

}
