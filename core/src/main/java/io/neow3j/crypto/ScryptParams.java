package io.neow3j.crypto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * NEP-2 defines the attributes "n", "r", and "p". However, some wallets use different attribute names. Thus, this
 * class includes aliases for compatibility purposes.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScryptParams {

    @JsonProperty("n")
    @JsonAlias("cost")
    private int n;

    @JsonProperty("r")
    @JsonAlias({"blockSize", "blocksize"})
    private int r;

    @JsonProperty("p")
    @JsonAlias("parallel")
    private int p;

    public ScryptParams() {
    }

    public ScryptParams(int n, int r, int p) {
        this.n = n;
        this.r = r;
        this.p = p;
    }

    public int getN() {
        return n;
    }

    public int getR() {
        return r;
    }

    public int getP() {
        return p;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ScryptParams)) {
            return false;
        }
        ScryptParams that = (ScryptParams) o;
        return getN() == that.getN() &&
                getR() == that.getR() &&
                getP() == that.getP();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getN(), getR(), getP());
    }

    @Override
    public String toString() {
        return "ScryptParams{" +
                "n=" + n +
                ", r=" + r +
                ", p=" + p +
                '}';
    }

}
