package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.neow3j.types.Hash160;
import io.neow3j.transaction.Signer;
import io.neow3j.transaction.WitnessScope;
import io.neow3j.utils.Numeric;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionSigner {

    @JsonProperty(value = "account", required = true)
    private Hash160 account;

    @JsonProperty(value = "scopes", required = true)
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonSerialize(using = WitnessScopeSerializer.class)
    @JsonDeserialize(using = WitnessScopeDeserializer.class)
    private List<WitnessScope> scopes;

    @JsonProperty("allowedcontracts")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> allowedContracts = new ArrayList<>();

    @JsonProperty("allowedgroups")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> allowedGroups = new ArrayList<>();

    protected TransactionSigner() {
    }

    public TransactionSigner(Signer signer) {
        this.account = signer.getScriptHash();
        this.scopes = signer.getScopes();
        this.allowedContracts = signer.getAllowedContracts().stream()
                .map(Hash160::toString)
                .collect(Collectors.toList());
        this.allowedGroups = signer.getAllowedGroups().stream()
                .map(s -> Numeric.toHexStringNoPrefix(s.getEncoded(true)))
                .collect(Collectors.toList());
    }

    public TransactionSigner(Hash160 account, List<WitnessScope> scopes,
            List<String> allowedContracts, List<String> allowedGroups) {
        this.account = account;
        this.scopes = scopes;
        this.allowedContracts = allowedContracts;
        this.allowedGroups = allowedGroups;
    }

    public TransactionSigner(Hash160 account, List<WitnessScope> scopes) {
        this(account, scopes, new ArrayList<>(), new ArrayList<>());
    }

    public Hash160 getAccount() {
        return account;
    }

    public List<WitnessScope> getScopes() {
        return scopes;
    }

    public List<String> getAllowedContracts() {
        return allowedContracts;
    }

    public List<String> getAllowedGroups() {
        return allowedGroups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransactionSigner)) {
            return false;
        }
        TransactionSigner other = (TransactionSigner) o;
        return Objects.equals(getAccount(), other.getAccount()) &&
                Objects.equals(getScopes(), other.getScopes()) &&
                Objects.equals(getAllowedContracts(), other.getAllowedContracts()) &&
                Objects.equals(getAllowedGroups(), other.getAllowedGroups());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAccount(), getScopes(), getAllowedContracts(), getAllowedGroups());
    }

    @Override
    public String toString() {
        return "TransactionSigner{" +
                "account='" + account + '\'' +
                ", scopes=" + scopes +
                ", allowedContracts=" + allowedContracts +
                ", allowedGroups=" + allowedGroups +
                '}';
    }

    private static class WitnessScopeSerializer extends JsonSerializer<List<WitnessScope>> {

        @Override
        public void serialize(List<WitnessScope> scopes, JsonGenerator jgen,
                SerializerProvider provider) throws IOException {

            if (scopes.isEmpty()) {
                jgen.writeString("");
                return;
            }
            StringBuilder b = new StringBuilder();
            for (WitnessScope scope : scopes) {
                b.append(scope.jsonValue()).append(",");
            }
            b.deleteCharAt(b.length()-1); // delete last comma.
            jgen.writeString(b.toString());
        }
    }

    private static class WitnessScopeDeserializer extends StdDeserializer<List<WitnessScope>> {

        protected WitnessScopeDeserializer() {
            this(null);
        }

        protected WitnessScopeDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public List<WitnessScope> deserialize(JsonParser p, DeserializationContext ctx)
                throws IOException {

            String scopesAsString = p.getCodec().readValue(p, String.class);
            return Arrays.stream(scopesAsString.replaceAll(" ", "").split(","))
                    .map(WitnessScope::fromJsonValue)
                    .collect(Collectors.toList());
        }
    }
}
