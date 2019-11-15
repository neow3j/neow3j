package io.neow3j.transaction;

import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.model.types.GASAsset;
import io.neow3j.model.types.TransactionType;
import io.neow3j.protocol.core.methods.response.NeoGetClaimable.Claimables;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ClaimTransaction extends RawTransaction {

    private List<RawTransactionInput> claims;

    public ClaimTransaction() {}

    protected ClaimTransaction(Builder builder) {
        super(builder);
        this.claims = builder.claims;
    }

    public List<RawTransactionInput> getClaims() {
        return claims;
    }

    @Override
    public void serializeExclusive(BinaryWriter writer) throws IOException {
        writer.writeSerializableVariable(claims);
    }

    @Override
    public void deserializeExclusive(BinaryReader reader) throws IOException, IllegalAccessException, InstantiationException {
        claims = reader.readSerializableList(RawTransactionInput.class);
    }

    public static ClaimTransaction fromClaimables(Claimables claimables, String receivingAddress) {
        List<RawTransactionOutput> outputs = Collections.singletonList(
                new RawTransactionOutput(GASAsset.HASH_ID, claimables.getTotalUnclaimed(), receivingAddress));

        List<RawTransactionInput> claims = claimables.getClaims().stream().map(
                c -> new RawTransactionInput(c.getTxId(), c.getIndex())).collect(Collectors.toList());

        return new Builder().claims(claims).outputs(outputs).build();
    }

    public static class Builder extends RawTransaction.Builder<Builder> {

        private List<RawTransactionInput> claims;

        public Builder() {
            super();
            claims = new ArrayList<>();
            transactionType(TransactionType.CLAIM_TRANSACTION);
        }

        public Builder claims(List<RawTransactionInput> claims) {
            this.claims.addAll(claims); return this;
        }

        public Builder claim(RawTransactionInput claim) {
            return claims(Arrays.asList(claim));
        }

        @Override
        public Builder outputs(List<RawTransactionOutput> outputs) {
            if (outputs.stream().anyMatch(output -> !output.getAssetId().equals(GASAsset.HASH_ID))) {
                throw new IllegalArgumentException("Outputs of a ClaimTransaction can only be of " +
                        "type GAS.");
            }
            super.outputs(outputs); return this;
        }

        @Override
        public Builder inputs(List<RawTransactionInput> inputs) {
            return claims(inputs);
        }

        @Override
        public ClaimTransaction build() {
            return new ClaimTransaction(this);
        }
    }
}
