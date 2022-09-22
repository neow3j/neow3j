package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;

import java.util.Objects;

public class NeoGetWalletBalance extends Response<NeoGetWalletBalance.Balance> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public Balance getWalletBalance() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Balance {

        @JsonProperty("balance")
        @JsonAlias("Balance")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String balance;

        public Balance() {
        }

        public Balance(String balance) {
            this.balance = balance;
        }

        public String getBalance() {
            return balance;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Balance)) {
                return false;
            }
            Balance balance1 = (Balance) o;
            return Objects.equals(getBalance(), balance1.getBalance());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getBalance());
        }

        @Override
        public String toString() {
            return "Balance{" +
                    "balance='" + balance + '\'' +
                    '}';
        }

    }

}
