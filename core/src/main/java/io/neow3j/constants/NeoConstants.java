package io.neow3j.constants;

import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;

import java.math.BigInteger;

public class NeoConstants {

    // region Accounts, Addresses, Keys

    /**
     * The maximum number of public keys that can take part in a multi-signature address. Taken from
     * Neo.SmartContract.Contract.CreateMultiSigRedeemScript(...) in the C# neo repo at
     * <a href="https://github.com/neo-project/neo">https://github.com/neo-project/neo</a>.
     */
    public static final int MAX_PUBLIC_KEYS_PER_MULTISIG_ACCOUNT = 1024;

    /**
     * The byte size of a {@link Hash160} hash.
     */
    public static final int HASH160_SIZE = 20;

    /**
     * The byte size of a {@link Hash256} hash.
     */
    public static final int HASH256_SIZE = 32;

    /**
     * Size of a private key in bytes.
     */
    public static final int PRIVATE_KEY_SIZE = 32;

    /**
     * Size of a compressed public key in bytes.
     */
    public static final int PUBLIC_KEY_SIZE_COMPRESSED = 33;

    /**
     * Size of a signature in bytes.
     */
    public static final int SIGNATURE_SIZE = 64;

    /**
     * Size of a single signature verification script in bytes.
     * <p>
     * 1 (PUSHDATA OpCode) + 1 (byte for data length) + 33 (public key) + 1 (SYSCALL Opcode) + 4 (InteropServiceCode)
     * = 41
     */
    public static final int VERIFICATION_SCRIPT_SIZE = 40;

    // endregion

    // region Transactions & Contracts

    /**
     * The current version used for Neo transaction.
     */
    public static final byte CURRENT_TX_VERSION = 0;

    /**
     * The maximum size of a transaction.
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
     * The maximum byte length for a valid contract manifest.
     */
    public static final int MAX_MANIFEST_SIZE = 0xFFFF;

    /**
     * The default maximum number of iterator items returned in an RPC response.
     */
    public static final int MAX_ITERATOR_ITEMS_DEFAULT = 100;

    // endregion

    // region Cryptography

    public static ECDomainParameters secp256r1DomainParams() {
        return new ECDomainParameters(
                secp256r1CurveParams().getCurve(), secp256r1CurveParams().getG(),
                secp256r1CurveParams().getN(), secp256r1CurveParams().getH());
    }

    public static X9ECParameters secp256r1CurveParams() {
        return CustomNamedCurves.getByName("secp256r1");
    }

    public static BigInteger secp256r1HalfCurveOrder() {
        return secp256r1CurveParams().getN().shiftRight(1);
    }

    // endregion

}
