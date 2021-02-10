package io.neow3j.constants;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;

import java.math.BigDecimal;
import java.math.BigInteger;

public class NeoConstants {

    //region Cryptography

    public static ECDomainParameters curve() {
        return new ECDomainParameters(
                curveParams().getCurve(), curveParams().getG(),
                curveParams().getN(), curveParams().getH());
    }

    public static X9ECParameters curveParams() {
        return CustomNamedCurves.getByName("secp256r1");
    }

    public static BigInteger halfCurveOrder() {
        return curveParams().getN().shiftRight(1);
    }

    //endregion

    //region Data Types

    public static final int FIXED8_SCALE = 8;

    public static final BigDecimal FIXED8_DECIMALS = BigDecimal.TEN.pow(FIXED8_SCALE);

    /**
     * Length of a Fixed8 byte array.
     */
    public static final int FIXED8_LENGTH = 8;

    //endregion

    //region Accounts, Addresses, Keys

    /**
     * The maximum number of public keys that can take part in a multi-signature address. Taken from
     * Neo.SmartContract.Contract.CreateMultiSigRedeemScript(...) in the C# neo repo at
     * https://github.com/neo-project/neo.
     */
    public static final int MAX_PUBLIC_KEYS_PER_MULTISIG_ACCOUNT = 1024;

    /**
     * The byte size of a script hash.
     */
    public static final int SCRIPTHASH_SIZE = 20;

    /**
     * Size of a private key in bytes
     */
    public static final int PRIVATE_KEY_SIZE = 32;

    /**
     * Size of a public key in bytes
     */
    public static final int PUBLIC_KEY_SIZE = 33;

    /**
     * Number of characters in a NEO address String.
     */
    public static final int ADDRESS_SIZE = 34;

    /**
     * Size of a signature in bytes.
     */
    public static final int SIGNATURE_SIZE = 64;

    /**
     * Size of an invocation (signature) script in bytes.
     * <p>
     * 1 (PUSHDATA OpCode) + 1 (byte for data length) + 64 (signature) = 66
     */
    public static final int INVOCATION_SCRIPT_SIZE = 66;

    /**
     * Size of a serialized invocation (signature) script in bytes.
     * <p>
     * 1 (byte for VarInt) + 1 (PUSHDATA OpCode) + 1 (byte for data length) + 64 (signature) = 67
     */
    public static final int SERIALIZED_INVOCATION_SCRIPT_SIZE = 67;

    /**
     * Size of a verification script in bytes.
     * <p>
     * 1 (PUSHDATA OpCode) + 1 (byte for data length) + 33 (public key) + 1 (PUSHNULL OpCode) + 1
     * (SYSCALL Opcode) + 4 (InteropServiceCode) = 41
     */
    public static final int VERIFICATION_SCRIPT_SIZE = 41;

    //endregion

    //region Fees

    /**
     * The network fee per byte of a transaction. The amount is given in fractions of GAS.
     */
    public static final long GAS_PER_BYTE = 1000;


    //endregion

    //region Transactions & Contracts

    /**
     * The current version used for Neo transaction.
     */
    public static final byte CURRENT_TX_VERSION = 0;

    /**
     * The maximum size of a transaction in bytes.
     */
    public static final int MAX_TRANSACTION_SIZE = 102400;

    /**
     * The maximum number of attributes that a transaction can have.
     */
    public static final int MAX_TRANSACTION_ATTRIBUTES = 16;

    /**
     * The maximum number of contracts or groups a signer scope can contain.
     */
    public static final int MAX_SIGNER_SUBITEMS = 16;

    /**
     * The maximum value for the 'validUntilBlock' transaction property.
     */
    public static final int MAX_VALID_UNTIL_BLOCK_INCREMENT = 5760;

    /**
     * Max byte length for a valid contract manifest.
     */
    public static final int MAX_MANIFEST_SIZE = 4096;

    //endregion

}
