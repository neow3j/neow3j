package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractNef {

    @JsonProperty("compiler")
    private String compiler;

    @JsonProperty("tokens")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<ContractMethodToken> tokens;

    @JsonProperty("script")
    private String script;

    @JsonProperty("checksum")
    private Long checksum;

    public ContractNef() {
    }

    public ContractNef(String compiler, List<ContractMethodToken> tokens, String script, Long checksum) {
        this.compiler = compiler;
        this.tokens = tokens;
        this.script = script;
        this.checksum = checksum;
    }

    public String getCompiler() {
        return compiler;
    }

    public List<ContractMethodToken> getTokens() {
        return tokens;
    }

    public String getScript() {
        return script;
    }

    public Long getChecksum() {
        return checksum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContractNef)) {
            return false;
        }
        ContractNef that = (ContractNef) o;
        return Objects.equals(getCompiler(), that.getCompiler()) &&
                Objects.equals(getTokens(), that.getTokens()) &&
                Objects.equals(getScript(), that.getScript()) &&
                Objects.equals(getChecksum(), that.getChecksum());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCompiler(), getTokens(), getScript(), getChecksum());
    }

    @Override
    public String toString() {
        return "ContractNef{" +
                "compiler='" + compiler + '\'' +
                ", tokens=" + tokens +
                ", script='" + script + '\'' +
                ", checksum=" + checksum +
                '}';
    }
}
