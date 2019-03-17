package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.neow3j.model.types.AssetType;
import io.neow3j.protocol.ObjectMapperFactory;
import io.neow3j.protocol.core.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class NeoGetAssetState extends Response<NeoGetAssetState.State> {

    public State getAssetState() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class State {

        @JsonProperty("version")
        private Integer version;

        @JsonProperty("id")
        @JsonAlias({"assetId"})
        private String id;

        @JsonProperty("type")
        @JsonAlias({"assetType"})
        private AssetType type;

        @JsonProperty("name")
        @JsonDeserialize(using = NamesDeserialiser.class)
        private List<AssetName> names;

        @JsonProperty("amount")
        @JsonDeserialize(using = NumericDeserialiser.class)
        private String amount;

        @JsonProperty("available")
        @JsonDeserialize(using = NumericDeserialiser.class)
        private String available;

        @JsonProperty("precision")
        private Integer precision;

        /**
         * Used on neo-python node
         */
        @JsonProperty("fee")
        private Integer fee;

        /**
         * Used on neo-python node
         */
        @JsonProperty("address")
        private String address;

        @JsonProperty("owner")
        private String owner;

        @JsonProperty("admin")
        private String admin;

        @JsonProperty("issuer")
        private String issuer;

        @JsonProperty("expiration")
        private Long expiration;

        @JsonProperty("frozen")
        @JsonAlias({"is_frozen"})
        private Boolean frozen;

        public State() {
        }

        public Integer getVersion() {
            return version;
        }

        public String getId() {
            return id;
        }

        public AssetType getType() {
            return type;
        }

        public List<AssetName> getNames() {
            return names;
        }

        public String getAmount() {
            return amount;
        }

        public String getAvailable() {
            return available;
        }

        public Integer getPrecision() {
            return precision;
        }

        public Integer getFee() {
            return fee;
        }

        public String getAddress() {
            return address;
        }

        public String getOwner() {
            return owner;
        }

        public String getAdmin() {
            return admin;
        }

        public String getIssuer() {
            return issuer;
        }

        public Long getExpiration() {
            return expiration;
        }

        public Boolean getFrozen() {
            return frozen;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof State)) return false;
            State state = (State) o;
            return Objects.equals(getVersion(), state.getVersion()) &&
                    Objects.equals(getId(), state.getId()) &&
                    getType() == state.getType() &&
                    Objects.equals(getNames(), state.getNames()) &&
                    Objects.equals(getAmount(), state.getAmount()) &&
                    Objects.equals(getAvailable(), state.getAvailable()) &&
                    Objects.equals(getPrecision(), state.getPrecision()) &&
                    Objects.equals(getFee(), state.getFee()) &&
                    Objects.equals(getAddress(), state.getAddress()) &&
                    Objects.equals(getOwner(), state.getOwner()) &&
                    Objects.equals(getAdmin(), state.getAdmin()) &&
                    Objects.equals(getIssuer(), state.getIssuer()) &&
                    Objects.equals(getExpiration(), state.getExpiration()) &&
                    Objects.equals(getFrozen(), state.getFrozen());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getVersion(), getId(), getType(), getNames(), getAmount(), getAvailable(), getPrecision(), getFee(), getAddress(), getOwner(), getAdmin(), getIssuer(), getExpiration(), getFrozen());
        }

        @Override
        public String toString() {
            return "State{" +
                    "version=" + version +
                    ", id='" + id + '\'' +
                    ", type=" + type +
                    ", names=" + names +
                    ", amount='" + amount + '\'' +
                    ", available='" + available + '\'' +
                    ", precision=" + precision +
                    ", fee=" + fee +
                    ", address='" + address + '\'' +
                    ", owner='" + owner + '\'' +
                    ", admin='" + admin + '\'' +
                    ", issuer='" + issuer + '\'' +
                    ", expiration=" + expiration +
                    ", frozen=" + frozen +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AssetName {

        @JsonProperty("lang")
        private String lang;

        @JsonProperty("name")
        private String name;

        public AssetName() {
        }

        public AssetName(String lang, String name) {
            this.lang = lang;
            this.name = name;
        }

        public String getLang() {
            return lang;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AssetName)) return false;
            AssetName assetName = (AssetName) o;
            return Objects.equals(getLang(), assetName.getLang()) &&
                    Objects.equals(getName(), assetName.getName());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getLang(), getName());
        }

        @Override
        public String toString() {
            return "AssetName{" +
                    "lang='" + lang + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    public static class NumericDeserialiser extends JsonDeserializer<String> {

        private ObjectReader objectReader = ObjectMapperFactory.getObjectReader();

        @Override
        public String deserialize(
                JsonParser jsonParser,
                DeserializationContext deserializationContext) throws IOException {
            if (jsonParser.getCurrentToken() != JsonToken.VALUE_NUMBER_INT) {
                Integer value = objectReader.readValue(jsonParser, Integer.class);
                return value.toString();
            } else if (jsonParser.getCurrentToken() != JsonToken.VALUE_STRING) {
                return objectReader.readValue(jsonParser, String.class);
            } else {
                return null;
            }
        }
    }

    public static class NamesDeserialiser extends JsonDeserializer<List<AssetName>> {

        private ObjectReader objectReader = ObjectMapperFactory.getObjectReader();

        @Override
        public List<AssetName> deserialize(
                JsonParser jsonParser,
                DeserializationContext deserializationContext) throws IOException {

            List<AssetName> names = new ArrayList<>();

            if (jsonParser.currentToken() == JsonToken.START_ARRAY) {
                JsonToken nextToken = jsonParser.nextToken();
                if (nextToken == JsonToken.START_OBJECT) {
                    Iterator<AssetName> assetNameObjectIterator =
                            objectReader.readValues(jsonParser, AssetName.class);
                    while (assetNameObjectIterator.hasNext()) {
                        names.add(assetNameObjectIterator.next());
                    }
                }
            } else if (jsonParser.currentToken() == JsonToken.VALUE_STRING) {

                String valueAsString = jsonParser.getValueAsString();
                AssetName assetName = new AssetName("en", valueAsString);
                names.add(assetName);
            }
            return names;
        }
    }

}
