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
    /**
     * Length of a Fixed8 byte array.
     */
    public static final int FIXED8_LENGTH = 8;


    public static final int SCRIPTHASH_LENGHT_BITS = 160;
    public static final int SCRIPTHASH_LENGHT_BYTES = SCRIPTHASH_LENGHT_BITS / 8;
    public static final int SCRIPTHASH_LENGHT_HEXSTRING = SCRIPTHASH_LENGHT_BYTES * 2;

    /**
     * Size of a global asset id in bits.
     */
    public static final int ASSET_ID_LENGHT_BITS = 256;
    public static final int ASSET_ID_LENGHT_BYTES = ASSET_ID_LENGHT_BITS / 8;
    public static final int ASSET_ID_LENGHT_HEXSTRING = ASSET_ID_LENGHT_BYTES * 2;

    /**
     * The amount of GAS that is free in every execution/invocation of a smart contract.
     */
    public static final BigDecimal FREE_GAS_AMOUNT = BigDecimal.TEN;

    /**
     * Size of a private key in bytes
     */
    public static final int PRIVATE_KEY_SIZE = 32;
    public static final int PRIVATE_KEY_LENGTH_IN_HEX = PRIVATE_KEY_SIZE << 1;

    /**
     * Size of a public key in bytes
     */
    public static final int PUBLIC_KEY_SIZE = 33;

    /**
     * Number of characters in a NEO address String.
     */
    public static final int ADDRESS_SIZE = 34;

    /**
     * Standard size of a signature used in NEO.
     */
    public static final int SIGNATURE_SIZE_BYTES = 64;
    public static final int SIGNATURE_SIZE_HEXSTRING = SIGNATURE_SIZE_BYTES * 2;

    /**
     * The basic GAS fee to be paid when deploying or migrating a contract.
     */
    public static final int CONTRACT_DEPLOY_BASIC_FEE = 100;

    /**
     * The additional GAS fee to be paid when deploying a contract that needs storage.
     */
    public static final int CONTRACT_DEPLOY_STORAGE_FEE = 400;

    /**
     * The additional GAS fee to be paid when deploying a contract that needs dynamic invokes.
     */
    public static final int CONTRACT_DEPLOY_DYNAMIC_INVOKE_FEE = 500;

    /**
     * The amount of GAS that is free in every contract execution (invocation or deployment).
     */
    public static final int FREE_OF_CHARGE_EXECUTION_COST = 10;

    /**
     * The maximum transaction size in bytes up to that a transaction is free. Transactions larger
     * need to add a network fee according to the formula (transaction size - 1024) * 0.00001 GAS +
     * 0.001 GAS. See <a href="https://neo.org/blog/details/4148">this</a> blog entry.
     */
    public static final int MAX_FREE_TRANSACTION_SIZE = 1024;

    /**
     * The network fee per byte for transactions bigger than
     * {@link NeoConstants#MAX_FREE_TRANSACTION_SIZE} bytes.
     */
    public static final BigDecimal FEE_PER_EXTRA_BYTE = new BigDecimal("0.00001");

    /**
     * The network fee threshold above which a transaction becomes a high priority transaction.
     * A transaction with a network fee below this threshold must not be bigger than 1024 bytes.
     */
    public static final BigDecimal PRIORITY_THRESHOLD_FEE = new BigDecimal("0.001");

}
