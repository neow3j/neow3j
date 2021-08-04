package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.types.Hash160;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Nep17Contract {

    @JsonProperty("scriptHash")
    private Hash160 scriptHash;

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("decimals")
    private int decimals;

    public Nep17Contract() {
    }

    public Nep17Contract(Hash160 scriptHash, String symbol, int decimals) {
        this.scriptHash = scriptHash;
        this.symbol = symbol;
        this.decimals = decimals;
    }

    public Hash160 getScriptHash() {
        return scriptHash;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getDecimals() {
        return decimals;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getScriptHash(), getSymbol(), getDecimals());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Nep17Contract)) {
            return false;
        }
        Nep17Contract that = (Nep17Contract) o;
        return Objects.equals(getScriptHash(), that.getScriptHash()) &&
                Objects.equals(getSymbol(), that.getSymbol()) &&
                Objects.equals(getDecimals(), that.getDecimals());
    }

    @Override
    public String toString() {
        return "Nep17Contract{" +
                "scriptHash='" + scriptHash + "'" +
                ", symbol='" + symbol + "'" +
                ", decimals=" + decimals +
                '}';
    }

}
