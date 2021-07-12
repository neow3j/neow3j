package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PopulatedBlocks {

    @JsonProperty("cacheId")
    private String cacheId;

    @JsonProperty("blocks")
    private List<Integer> blocks;

    public PopulatedBlocks() {
    }

    public PopulatedBlocks(String cacheId, List<Integer> blocks) {
        this.cacheId = cacheId;
        this.blocks = blocks;
    }

    public String getCacheId() {
        return cacheId;
    }

    public List<Integer> getBlocks() {
        return blocks;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCacheId(), getBlocks());
    }

    @Override
    public String toString() {
        return "PopulatedBlocks{" +
                "cacheId=" + cacheId +
                ", blocks=" + blocks +
                '}';
    }

}
