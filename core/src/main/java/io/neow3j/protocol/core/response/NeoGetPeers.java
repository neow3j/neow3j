package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.neow3j.protocol.core.Response;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;

import java.util.List;
import java.util.Objects;

public class NeoGetPeers extends Response<NeoGetPeers.Peers> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public Peers getPeers() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Peers {

        @JsonProperty("connected")
        private List<AddressEntry> connected;

        @JsonProperty("bad")
        private List<AddressEntry> bad;

        @JsonProperty("unconnected")
        private List<AddressEntry> unconnected;

        public Peers() {
        }

        public Peers(List<AddressEntry> connected, List<AddressEntry> bad, List<AddressEntry> unconnected) {
            this.connected = connected;
            this.bad = bad;
            this.unconnected = unconnected;
        }

        public List<AddressEntry> getConnected() {
            return connected;
        }

        @JsonSetter(nulls = Nulls.AS_EMPTY)
        public void setConnected(List<AddressEntry> connected) {
            this.connected = connected;
        }

        public List<AddressEntry> getBad() {
            return bad;
        }

        @JsonSetter(nulls = Nulls.AS_EMPTY)
        public void setBad(List<AddressEntry> bad) {
            this.bad = bad;
        }

        public List<AddressEntry> getUnconnected() {
            return unconnected;
        }

        @JsonSetter(nulls = Nulls.AS_EMPTY)
        public void setUnconnected(List<AddressEntry> unconnected) {
            this.unconnected = unconnected;
        }

        @Override
        public String toString() {
            return "Peers{" +
                    "connected=" + connected +
                    ", bad=" + bad +
                    ", unconnected=" + unconnected +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AddressEntry {

        @JsonProperty("address")
        private String address;

        @JsonProperty("port")
        private Integer port;

        public AddressEntry() {
        }

        public AddressEntry(String address, Integer port) {
            this.address = address;
            this.port = port;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof AddressEntry)) {
                return false;
            }
            AddressEntry that = (AddressEntry) o;
            return Objects.equals(getAddress(), that.getAddress()) &&
                    Objects.equals(getPort(), that.getPort());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getAddress(), getPort());
        }

        @Override
        public String toString() {
            return "AddressEntry{" +
                    "address='" + address + '\'' +
                    ", port=" + port +
                    '}';
        }

    }

}
