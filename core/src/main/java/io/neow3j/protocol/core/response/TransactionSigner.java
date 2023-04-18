package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.neow3j.protocol.ObjectMapperFactory;
import io.neow3j.protocol.core.witnessrule.WitnessRule;
import io.neow3j.transaction.Signer;
import io.neow3j.transaction.WitnessScope;
import io.neow3j.types.Hash160;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.neow3j.utils.Numeric.toHexStringNoPrefix;
import static java.lang.String.format;

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

    @JsonProperty("rules")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<WitnessRule> rules = new ArrayList<>();

    protected TransactionSigner() {
    }

    public TransactionSigner(Signer signer) {
        this.account = signer.getScriptHash();
        this.scopes = signer.getScopes();
        this.allowedContracts = signer.getAllowedContracts().stream()
                .map(Hash160::toString)
                .collect(Collectors.toList());
        this.allowedGroups = signer.getAllowedGroups().stream()
                .map(s -> toHexStringNoPrefix(s.getEncoded(true)))
                .collect(Collectors.toList());
        this.rules = signer.getRules().stream()
                .map(r -> new WitnessRule(r.getAction(), r.getCondition().toDTO()))
                .collect(Collectors.toList());
    }

    public TransactionSigner(Hash160 account, List<WitnessScope> scopes, List<String> allowedContracts,
            List<String> allowedGroups, List<WitnessRule> rules) {
        this.account = account;
        this.scopes = scopes;
        this.allowedContracts = allowedContracts;
        this.allowedGroups = allowedGroups;
        this.rules = rules;
    }

    public TransactionSigner(Hash160 account, List<WitnessScope> scopes) {
        this(account, scopes, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public Hash160 getAccount() {
        return account;
    }

    public List<WitnessScope> getScopes() {
        return scopes;
    }

    @JsonIgnore
    public WitnessScope getFirstWitnessScope() {
        if (scopes.size() == 0) {
            throw new IndexOutOfBoundsException("This transaction signer does not have any witness scopes. It might " +
                    "be malformed, since every transaction signer needs to have a witness scope specified.");
        }
        return getWitnessScope(0);
    }

    @JsonIgnore
    public WitnessScope getWitnessScope(int index) {
        if (index >= scopes.size()) {
            throw new IndexOutOfBoundsException(format("This transaction signer only has %s witness scopes. Tried to " +
                            "access index %s.", scopes.size(), index));
        }
        return scopes.get(index);
    }

    public List<String> getAllowedContracts() {
        return allowedContracts;
    }

    @JsonIgnore
    public String getFirstAllowedContract() {
        if (allowedContracts.size() == 0) {
            throw new IndexOutOfBoundsException("This transaction signer does not allow any specific contract.");
        }
        return getAllowedContract(0);
    }

    @JsonIgnore
    public String getAllowedContract(int index) {
        if (index >= allowedContracts.size()) {
            throw new IndexOutOfBoundsException(format("This transaction signer only allows %s contracts. Tried to " +
                    "access index %s.", allowedContracts.size(), index));
        }
        return allowedContracts.get(index);
    }

    public List<String> getAllowedGroups() {
        return allowedGroups;
    }

    @JsonIgnore
    public String getFirstAllowedGroup() {
        if (allowedGroups.size() == 0) {
            throw new IndexOutOfBoundsException("This transaction signer does not allow any specific group.");
        }
        return getAllowedGroup(0);
    }

    @JsonIgnore
    public String getAllowedGroup(int index) {
        if (index >= allowedGroups.size()) {
            throw new IndexOutOfBoundsException(format("This transaction signer only allows %s groups. Tried to " +
                    "access index %s.", allowedGroups.size(), index));
        }
        return allowedGroups.get(index);
    }

    public List<WitnessRule> getRules() {
        return rules;
    }

    @JsonIgnore
    public WitnessRule getFirstWitnessRule() {
        if (rules.size() == 0) {
            throw new IndexOutOfBoundsException("This transaction signer does have any witness rules.");
        }
        return getWitnessRule(0);
    }

    @JsonIgnore
    public WitnessRule getWitnessRule(int index) {
        if (index >= rules.size()) {
            throw new IndexOutOfBoundsException(format("This transaction signer only has %s witness rules. Tried to " +
                    "access index %s.", rules.size(), index));
        }
        return rules.get(index);
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
                Objects.equals(getAllowedGroups(), other.getAllowedGroups()) &&
                Objects.equals(getRules(), other.getRules());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAccount(), getScopes(), getAllowedContracts(), getAllowedGroups(), getRules());
    }

    @Override
    public String toString() {
        try {
            return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
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
            b.deleteCharAt(b.length() - 1); // delete last comma.
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
        public List<WitnessScope> deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            String scopesAsString = p.getCodec().readValue(p, String.class);
            return Arrays.stream(scopesAsString.replaceAll(" ", "").split(","))
                    .map(WitnessScope::fromJsonValue)
                    .collect(Collectors.toList());
        }
    }

}
