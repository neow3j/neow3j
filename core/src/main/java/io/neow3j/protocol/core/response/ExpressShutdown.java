package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExpressShutdown {

    @JsonProperty("process-id")
    private Integer processId;

    public ExpressShutdown() {
    }

    public ExpressShutdown(Integer processId) {
        this.processId = processId;
    }

    public Integer getProcessId() {
        return processId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExpressShutdown)) {
            return false;
        }
        ExpressShutdown that = (ExpressShutdown) o;
        return Objects.equals(getProcessId(), that.getProcessId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProcessId());
    }

    @Override
    public String toString() {
        return "ExpressShutdown{" +
                "processId=" + processId +
                '}';
    }

}
