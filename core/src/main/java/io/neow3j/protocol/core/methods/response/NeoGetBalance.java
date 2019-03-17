package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.protocol.core.Response;

import java.util.Objects;

public class NeoGetBalance extends Response<NeoGetBalance.Balance> {

    public NeoGetBalance.Balance getBalance() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Balance {

        @JsonProperty("balance")
        @JsonAlias("Balance")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String balance;

        @JsonProperty("confirmed")
        @JsonAlias("Confirmed")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String confirmed;

        public Balance() {
        }

        public Balance(String balance, String confirmed) {
            this.balance = balance;
            this.confirmed = confirmed;
        }

        public String getBalance() {
            return balance;
        }

        public String getConfirmed() {
            return confirmed;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Balance)) return false;
            Balance balance1 = (Balance) o;
            return Objects.equals(getBalance(), balance1.getBalance()) &&
                    Objects.equals(getConfirmed(), balance1.getConfirmed());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getBalance(), getConfirmed());
        }

        @Override
        public String toString() {
            return "Balance{" +
                    "balance='" + balance + '\'' +
                    ", confirmed='" + confirmed + '\'' +
                    '}';
        }
    }

}
