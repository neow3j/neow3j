package io.neow3j.neofs.sdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ContainerListResponse {

    @JsonProperty(value = "containers")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> containerIDs = new ArrayList<>();

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
