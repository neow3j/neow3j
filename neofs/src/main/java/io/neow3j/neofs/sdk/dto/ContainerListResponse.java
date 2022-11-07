package io.neow3j.neofs.sdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ContainerListResponse {

    @JsonProperty(value = "containers")
    private List<String> containerIDs;

    public ContainerListResponse() {
    }

    public List<String> getContainerIDs() {
        return containerIDs;
    }

    @Override
    public String toString() {
        return "Containers=" + containerIDs;
    }

}
