package io.neow3j.transaction;

import io.neow3j.contract.ScriptHash;
import org.bouncycastle.math.ec.ECPoint;

import java.util.List;

public class Cosigner {

    private ScriptHash account;
    // TODO 17.10.19 claude:
    // Scopes' byte values can be combined, therefore this might be of type byte instead
    // WitnessScope.
    private WitnessScope scope;
    private List<ScriptHash> allowedContracts;
    // TODO 17.10.19 claude:
    // Figure out how the allowed groups work. Not sure if the use of ECPoint is correct here.
    private List<ECPoint> allowedGroups;

}
