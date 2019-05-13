package io.neow3j.crypto.transaction;

import io.neow3j.crypto.Claim;
import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.model.types.GASAsset;
import io.neow3j.model.types.NEOAsset;
import io.neow3j.model.types.TransactionType;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ClaimTransaction extends RawTransaction {

    private List<RawTransactionInput> claims;

    public ClaimTransaction() {
    }

    public ClaimTransaction(List<RawTransactionAttribute> attributes, List<RawTransactionOutput> outputs,
                            List<RawTransactionInput> claims, List<RawScript> scripts) {

        super(TransactionType.CLAIM_TRANSACTION, attributes, null, outputs, scripts);

        if (outputs.stream().anyMatch(output -> !output.getAssetId().equals(GASAsset.HASH_ID))) {
            throw new IllegalArgumentException("Outputs of a ClaimTransaction can only be of type GAS. There is one " +
                    "or more outputs that refers to a different asset type.");
        }
        this.claims = claims;
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

    public static ClaimTransaction fromClaims(List<Claim> claims, String receivingAddress) {
        BigDecimal totalClaim = claims.stream().map(Claim::getClaimValue).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        List<RawTransactionOutput> outputs = Collections.singletonList(
                new RawTransactionOutput(0, GASAsset.HASH_ID, totalClaim.toString(), receivingAddress));

        List<RawTransactionInput> claimsAsInputs = claims.stream().map(
                c -> new RawTransactionInput(c.getTxId(), c.getIndex())).collect(Collectors.toList());
        return new ClaimTransaction(null, outputs, claimsAsInputs, null);
    }
}
