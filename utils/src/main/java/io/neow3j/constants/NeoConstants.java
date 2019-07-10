package io.neow3j.constants;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;

import java.math.BigDecimal;
import java.math.BigInteger;

public class NeoConstants {

    public static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256r1");
    public static final ECDomainParameters CURVE = new ECDomainParameters(
            CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(), CURVE_PARAMS.getH());
    public static final BigInteger HALF_CURVE_ORDER = CURVE_PARAMS.getN().shiftRight(1);

    public static final byte COIN_VERSION = 0x17;

    /**
     * The maximum number of public keys that can take part in a multi-signature address.
     * Taken from Neo.SmartContract.Contract.CreateMultiSigRedeemScript(...) in the C# neo repo
     * at https://github.com/neo-project/neo.
     */
    public static final int MAX_PUBLIC_KEYS_PER_MULTISIG_ACCOUNT = 1024;

    public static final int FIXED8_SCALE = 8;
    public static final BigDecimal FIXED8_DECIMALS = BigDecimal.TEN.pow(FIXED8_SCALE);

}
