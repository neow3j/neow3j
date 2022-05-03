package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractNef {

    @JsonProperty("magic")
    private Long magic;

    @JsonProperty("compiler")
    private String compiler;

    @JsonProperty("source")
    private String source;

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

    public ContractNef(Long magic, String compiler, String source, List<ContractMethodToken> tokens, String script,
            Long checksum) {
        this.magic = magic;
        this.compiler = compiler;
        this.source = source;
        this.tokens = tokens;
        this.script = script;
        this.checksum = checksum;
    }

    public Long getMagic() {
        return magic;
    }

    public String getCompiler() {
        return compiler;
    }

    public String getSource() {
        return source;
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
        return Objects.equals(magic, that.magic) &&
                Objects.equals(compiler, that.compiler) &&
                Objects.equals(tokens, that.tokens) &&
                Objects.equals(script, that.script) &&
                Objects.equals(checksum, that.checksum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(magic, compiler, tokens, script, checksum);
    }

    @Override
    public String toString() {
        return "ContractNef{" +
                "magic=" + magic +
                ", compiler='" + compiler + '\'' +
                ", tokens=" + tokens +
                ", script='" + script + '\'' +
                ", checksum=" + checksum +
                '}';
    }

}
