package io.neow3j.transaction;

import io.neow3j.script.VerificationScript;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Account;

import static io.neow3j.constants.NeoConstants.MAX_PUBLIC_KEYS_PER_MULTISIG_ACCOUNT;
import static java.lang.String.format;

/**
 * A signer of a transaction. It defines a in which scope the witness/signature of an account is valid, i.e., which
 * contracts can use the witness in an invocation.
 * <p>
 * If an {@code AccountSigner} is created with an private key-holding {@code Account}, it can be used to
 * automatically sign a transaction via {@link TransactionBuilder#sign()}.
 */
public class AccountSigner extends Signer {

    private final Account account;

    private AccountSigner(Hash160 signerHash, WitnessScope scope) {
        super(signerHash, scope);
        account = Account.fromAddress(signerHash.toAddress());
    }

    private AccountSigner(Account account, WitnessScope scope) {
        super(account.getScriptHash(), scope);
        this.account = account;
    }

    /**
     * @return the account of this signer.
     */
    public Account getAccount() {
        return account;
    }

    /**
     * Checks whether this signer's account holds a verification script.
     * <p>
     * A signer created from a script hash does not. Building still prices that signer as a standard single-sig
     * account, which is accurate for an ordinary address. A multi-sig signer created from a script hash, signing
     * threshold, and participant count also has no script; its network fee is sized from those dimensions. Exporting
     * a contract parameters context requires the actual single-sig or multi-sig verification script in either case,
     * because that script carries the public keys.
     *
     * @return true if the account has a verification script.
     */
    public boolean hasVerificationScript() {
        return account.getVerificationScript() != null;
    }

    /**
     * Checks whether this signer can be witnessed locally by {@link TransactionBuilder#sign()}.
     * <p>
     * That requires a decrypted single-sig key whose verification script is a standard signature check. A hash-only
     * account, an account with only a verification script, an account whose private key is still encrypted, and a
     * multi-sig account are not locally signable.
     *
     * @return true if {@link TransactionBuilder#sign()} can produce this signer's witness.
     */
    public boolean canSign() {
        if (account.isMultiSig() || account.getECKeyPair() == null) {
            return false;
        }
        VerificationScript verificationScript = account.getVerificationScript();
        return verificationScript != null && verificationScript.isSingleSigScript();
    }

    /**
     * Creates a signer for the given account with no witness scope ({@link WitnessScope#NONE}).
     * <p>
     * The signature of this signer is only used for transactions and is disabled in contracts.
     *
     * @param account the signer account.
     * @return the signer.
     */
    public static AccountSigner none(Account account) {
        return new AccountSigner(account, WitnessScope.NONE);
    }

    /**
     * Creates a signer for the given account with no witness scope ({@link WitnessScope#NONE}).
     * <p>
     * The signature of this signer is only used for transactions and is disabled in contracts.
     * <p>
     * The account is hash-only. Network fee calculation assumes a standard single-sig verification script, which is
     * accurate for an ordinary address. For a multi-sig account, use {@link #none(Hash160, int, int)}. Local signing
     * and a contract parameters context still require the verification script and, for signing, the private key.
     *
     * @param accountHash the script hash of the signer account.
     * @return the signer.
     */
    public static AccountSigner none(Hash160 accountHash) {
        return new AccountSigner(accountHash, WitnessScope.NONE);
    }

    /**
     * Creates a multi-sig signer for the given script hash with no witness scope ({@link WitnessScope#NONE}).
     * <p>
     * Network fee calculation uses {@code signingThreshold} and {@code nrOfParticipants}. The threshold must be at
     * least 1 and not higher than the participant count, which itself must not exceed
     * {@link io.neow3j.constants.NeoConstants#MAX_PUBLIC_KEYS_PER_MULTISIG_ACCOUNT}. The public keys are not required
     * to build the transaction. Local signing is not supported, and exporting a contract parameters context requires
     * the verification script that contains the public keys. A high-priority transaction also needs those keys to
     * decide whether the account contains a committee member.
     *
     * @param accountHash      the script hash of the multi-sig account.
     * @param signingThreshold the number of signatures required.
     * @param nrOfParticipants the number of participating keys.
     * @return the signer.
     */
    public static AccountSigner none(Hash160 accountHash, int signingThreshold, int nrOfParticipants) {
        return multiSig(accountHash, signingThreshold, nrOfParticipants, WitnessScope.NONE);
    }

    /**
     * Creates a signer for the given account with a scope ({@link WitnessScope#CALLED_BY_ENTRY}) that only allows
     * the entry point contract to use this signer's witness.
     *
     * @param account the signer account.
     * @return the signer.
     */
    public static AccountSigner calledByEntry(Account account) {
        return new AccountSigner(account, WitnessScope.CALLED_BY_ENTRY);
    }

    /**
     * Creates a signer for the given account with a scope ({@link WitnessScope#CALLED_BY_ENTRY}) that only allows
     * the entry point contract to use this signer's witness.
     * <p>
     * The account is hash-only. Network fee calculation assumes a standard single-sig verification script, which is
     * accurate for an ordinary address. For a multi-sig account, use {@link #calledByEntry(Hash160, int, int)}.
     * Local signing and a contract parameters context still require the verification script and, for signing, the
     * private key.
     *
     * @param accountHash the script hash of the signer account.
     * @return the signer.
     */
    public static AccountSigner calledByEntry(Hash160 accountHash) {
        return new AccountSigner(accountHash, WitnessScope.CALLED_BY_ENTRY);
    }

    /**
     * Creates a multi-sig signer for the given script hash with scope {@link WitnessScope#CALLED_BY_ENTRY}.
     * <p>
     * Network fee calculation uses {@code signingThreshold} and {@code nrOfParticipants}. The threshold must be at
     * least 1 and not higher than the participant count, which itself must not exceed
     * {@link io.neow3j.constants.NeoConstants#MAX_PUBLIC_KEYS_PER_MULTISIG_ACCOUNT}. The public keys are not required
     * to build the transaction. Local signing is not supported, and exporting a contract parameters context requires
     * the verification script that contains the public keys. A high-priority transaction also needs those keys to
     * decide whether the account contains a committee member.
     *
     * @param accountHash      the script hash of the multi-sig account.
     * @param signingThreshold the number of signatures required.
     * @param nrOfParticipants the number of participating keys.
     * @return the signer.
     */
    public static AccountSigner calledByEntry(Hash160 accountHash, int signingThreshold, int nrOfParticipants) {
        return multiSig(accountHash, signingThreshold, nrOfParticipants, WitnessScope.CALLED_BY_ENTRY);
    }

    /**
     * Creates a signer for the given account with global witness scope ({@link WitnessScope#GLOBAL}).
     *
     * @param account the account.
     * @return the signer.
     */
    public static AccountSigner global(Account account) {
        return new AccountSigner(account, WitnessScope.GLOBAL);
    }

    /**
     * Creates a signer for the given account with global witness scope ({@link WitnessScope#GLOBAL}).
     * <p>
     * The account is hash-only. Network fee calculation assumes a standard single-sig verification script, which is
     * accurate for an ordinary address. For a multi-sig account, use {@link #global(Hash160, int, int)}. Local
     * signing and a contract parameters context still require the verification script and, for signing, the private
     * key.
     *
     * @param accountHash the script hash of the signer account.
     * @return the signer.
     */
    public static AccountSigner global(Hash160 accountHash) {
        return new AccountSigner(accountHash, WitnessScope.GLOBAL);
    }

    /**
     * Creates a multi-sig signer for the given script hash with global witness scope ({@link WitnessScope#GLOBAL}).
     * <p>
     * Network fee calculation uses {@code signingThreshold} and {@code nrOfParticipants}. The threshold must be at
     * least 1 and not higher than the participant count, which itself must not exceed
     * {@link io.neow3j.constants.NeoConstants#MAX_PUBLIC_KEYS_PER_MULTISIG_ACCOUNT}. The public keys are not required
     * to build the transaction. Local signing is not supported, and exporting a contract parameters context requires
     * the verification script that contains the public keys. A high-priority transaction also needs those keys to
     * decide whether the account contains a committee member.
     *
     * @param accountHash      the script hash of the multi-sig account.
     * @param signingThreshold the number of signatures required.
     * @param nrOfParticipants the number of participating keys.
     * @return the signer.
     */
    public static AccountSigner global(Hash160 accountHash, int signingThreshold, int nrOfParticipants) {
        return multiSig(accountHash, signingThreshold, nrOfParticipants, WitnessScope.GLOBAL);
    }

    private static AccountSigner multiSig(Hash160 accountHash, int signingThreshold, int nrOfParticipants,
            WitnessScope scope) {
        validateMultiSigDimensions(signingThreshold, nrOfParticipants);
        return new AccountSigner(
                Account.createMultiSigAccount(accountHash.toAddress(), signingThreshold, nrOfParticipants), scope);
    }

    /**
     * Checks that a multi-sig threshold and participant count can size a witness.
     *
     * @param signingThreshold the number of signatures required.
     * @param nrOfParticipants the number of participating keys.
     * @throws IllegalArgumentException if the threshold is below 1 or higher than the participant count, or more than
     *                                  {@code MAX_PUBLIC_KEYS_PER_MULTISIG_ACCOUNT} keys take part.
     */
    static void validateMultiSigDimensions(int signingThreshold, int nrOfParticipants) {
        if (signingThreshold < 1 || nrOfParticipants < 1 || signingThreshold > nrOfParticipants) {
            throw new IllegalArgumentException(format("Signing threshold must be at least 1 and not higher than the " +
                    "number of participants (%s).", nrOfParticipants));
        }
        if (nrOfParticipants > MAX_PUBLIC_KEYS_PER_MULTISIG_ACCOUNT) {
            throw new IllegalArgumentException(format("At most %s public keys can take part in a multi-sig account.",
                    MAX_PUBLIC_KEYS_PER_MULTISIG_ACCOUNT));
        }
    }

}
