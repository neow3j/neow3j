package io.neow3j.contract;

import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.contract.exceptions.UnresolvableDomainNameException;
import io.neow3j.contract.types.NNSName;
import io.neow3j.protocol.Neow3j;
import io.neow3j.transaction.ContractSigner;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;

import java.io.IOException;
import java.math.BigInteger;

import static io.neow3j.transaction.AccountSigner.calledByEntry;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;

/**
 * Represents a fungible token contract that is compliant with the NEP-17 standard and provides methods to invoke it.
 */
public class FungibleToken extends Token {

    private static final String BALANCE_OF = "balanceOf";
    private static final String TRANSFER = "transfer";

    /**
     * Constructs a new {@code FungibleToken} representing the token contract with the given script hash. Uses the
     * given {@link Neow3j} instance for all invocations.
     *
     * @param scriptHash the token contract's script hash
     * @param neow       the {@link Neow3j} instance to use for invocations.
     */
    public FungibleToken(Hash160 scriptHash, Neow3j neow) {
        super(scriptHash, neow);
    }

    /**
     * Gets the token balance for the given account.
     * <p>
     * The token amount is returned in token fractions. E.g., an amount of 1 GAS is returned as 1*10^8 GAS fractions.
     * <p>
     * The balance is not cached locally. Every time this method is called requests are sent to the Neo node.
     *
     * @param account the account to fetch the balance for.
     * @return the token balance.
     * @throws IOException                   if there was a problem fetching information from the Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something interpretable as a
     *                                       number.
     */
    public BigInteger getBalanceOf(Account account) throws IOException, UnexpectedReturnTypeException {
        return getBalanceOf(account.getScriptHash());
    }

    /**
     * Gets the token balance for the given account script hash.
     * <p>
     * The token amount is returned in token fractions. E.g., an amount of 1 GAS is returned as 1*10^8 GAS fractions.
     * <p>
     * The balance is not cached locally. Every time this method is called requests are sent to the Neo node.
     *
     * @param scriptHash the script hash of the account to fetch the balance for.
     * @return the token balance.
     * @throws IOException                   if there was a problem fetching information from the Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something interpretable as a
     *                                       number.
     */
    public BigInteger getBalanceOf(Hash160 scriptHash) throws IOException, UnexpectedReturnTypeException {
        ContractParameter ofParam = hash160(scriptHash);
        return callFunctionReturningInt(BALANCE_OF, ofParam);
    }

    /**
     * Gets the token balance for the given wallet, i.e., all accounts in the wallet.
     * <p>
     * The token amount is returned in token fractions. E.g., an amount of 1 GAS is returned as 1*10^8 GAS fractions.
     * <p>
     * The balance is not cached locally. Every time this method is called requests are sent to the Neo node.
     *
     * @param wallet the wallet to fetch the balance for.
     * @return the token balance.
     * @throws IOException                   if there was a problem fetching information from the Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something interpretable as a
     *                                       number.
     */
    public BigInteger getBalanceOf(Wallet wallet) throws IOException, UnexpectedReturnTypeException {
        BigInteger sum = BigInteger.ZERO;
        for (Account a : wallet.getAccounts()) {
            sum = sum.add(getBalanceOf(a.getScriptHash()));
        }
        return sum;
    }

    /**
     * Creates a transfer transaction.
     * <p>
     * The {@code from} account is set as a signer of the transaction.
     *
     * @param from   the sender account.
     * @param to     the script hash of the recipient.
     * @param amount the amount to transfer in token fractions.
     * @return a transaction builder ready for signing.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transfer(Account from, Hash160 to, BigInteger amount) throws IOException {
        return transfer(from, to, amount, null);
    }

    /**
     * Creates a transfer transaction.
     * <p>
     * The {@code from} account is set as a signer of the transaction.
     * <p>
     * Only use this method when the recipient is a deployed smart contract to avoid unnecessary additional fees.
     * Otherwise, use the method without a contract parameter for data.
     *
     * @param from   the sender account.
     * @param to     the script hash of the recipient.
     * @param amount the amount to transfer in token fractions.
     * @param data   the data that is passed to the {@code onPayment} method if the recipient is a contract.
     * @return a transaction builder ready for signing.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transfer(Account from, Hash160 to, BigInteger amount, ContractParameter data)
            throws IOException {
        return transfer(from.getScriptHash(), to, amount, data).signers(calledByEntry(from));
    }

    /**
     * Creates a transfer transaction.
     * <p>
     * No signers are set on the returned transaction builder. It is up to you to set the correct ones, e.g., a
     * {@link ContractSigner} in case the {@code from} address is a contract.
     *
     * @param from   the script hash of the sender.
     * @param to     the script hash of the recipient.
     * @param amount the amount to transfer in token fractions.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transfer(Hash160 from, Hash160 to, BigInteger amount) throws IOException {
        return transfer(from, to, amount, null);
    }

    /**
     * Creates a transfer transaction.
     * <p>
     * No signers are set on the returned transaction builder. It is up to you to set the correct ones, e.g., a
     * {@link ContractSigner} in case the {@code from} address is a contract.
     *
     * @param from   the script hash of the sender.
     * @param to     the script hash of the recipient.
     * @param amount the amount to transfer in token fractions.
     * @param data   the data that is passed to the {@code onPayment} method if the recipient is a contract.
     * @return a transaction builder ready for signing.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transfer(Hash160 from, Hash160 to, BigInteger amount, ContractParameter data)
            throws IOException {

        if (amount.signum() < 0) {
            throw new IllegalArgumentException("The amount must be greater than or equal to 0.");
        }
        byte[] transferScript = buildTransferScript(from, to, amount, data);
        return new TransactionBuilder(neow3j).script(transferScript);
    }

    /**
     * Builds a script that invokes the transfer method on the fungible token.
     *
     * @param from   the sender.
     * @param to     the recipient.
     * @param amount the transfer amount.
     * @param data   the data that is passed to the {@code onPayment} method if the recipient is a contract.
     * @return a transfer script.
     */
    public byte[] buildTransferScript(Hash160 from, Hash160 to, BigInteger amount, ContractParameter data) {
        return buildInvokeFunctionScript(TRANSFER, hash160(from), hash160(to), integer(amount), data);
    }

    // region transfer using NNS

    /**
     * Creates a transfer transaction.
     * <p>
     * Resolves the text record of the recipient's NNS domain name. The resolved value is expected to be a valid Neo
     * address.
     * <p>
     * The {@code from} account is set as a signer of the transaction.
     *
     * @param from   the sender account.
     * @param to     the NNS domain name to resolve.
     * @param amount the amount to transfer in token fractions.
     * @return a transaction builder ready for signing.
     * @throws IOException if there was a problem fetching information from the Neo node.
     * @throws UnresolvableDomainNameException if the NNS text record could not be resolved.
     */
    public TransactionBuilder transfer(Account from, NNSName to, BigInteger amount)
            throws IOException, UnresolvableDomainNameException {
        return transfer(from, to, amount, null);
    }

    /**
     * Creates a transfer transaction.
     * <p>
     * Resolves the text record of the recipient's NNS domain name. The resolved value is expected to be a valid Neo
     * address.
     * <p>
     * The {@code from} account is set as a signer of the transaction.
     * <p>
     * Only use this method when the recipient is a deployed smart contract to avoid unnecessary additional fees.
     * Otherwise, use the method without a contract parameter for data.
     *
     * @param from   the sender account.
     * @param to     the NNS domain name to resolve.
     * @param amount the amount to transfer in token fractions.
     * @param data   the data that is passed to the {@code onPayment} method if the recipient is a contract.
     * @return a transaction builder ready for signing.
     * @throws IOException if there was a problem fetching information from the Neo node.
     * @throws UnresolvableDomainNameException if the NNS text record could not be resolved.
     */
    public TransactionBuilder transfer(Account from, NNSName to, BigInteger amount, ContractParameter data)
            throws IOException, UnresolvableDomainNameException {
        return transfer(from.getScriptHash(), to, amount, data).signers(calledByEntry(from));
    }

    /**
     * Creates a transfer transaction.
     * <p>
     * Resolves the text record of the recipient's NNS domain name. The resolved value is expected to be a valid Neo
     * address.
     * <p>
     * No signers are set on the returned transaction builder. It is up to you to set the correct ones, e.g., a
     * {@link ContractSigner} in case the {@code from} address is a contract.
     *
     * @param from   the sender hash.
     * @param to     the NNS domain name to resolve.
     * @param amount the amount to transfer in token fractions.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     * @throws UnresolvableDomainNameException if the NNS text record could not be resolved.
     */
    public TransactionBuilder transfer(Hash160 from, NNSName to, BigInteger amount)
            throws IOException, UnresolvableDomainNameException {
        return transfer(from, to, amount, null);
    }

    /**
     * Creates a transfer transaction.
     * <p>
     * Resolves the text record of the recipient's NNS domain name. The resolved value is expected to be a valid Neo
     * address.
     * <p>
     * No signers are set on the returned transaction builder. It is up to you to set the correct ones, e.g., a
     * {@link ContractSigner} in case the {@code from} address is a contract.
     *
     * @param from   the sender account.
     * @param to     the NNS domain name to resolve.
     * @param amount the amount to transfer in token fractions.
     * @param data   the data that is passed to the {@code onPayment} method if the recipient is a contract.
     * @return a transaction builder ready for signing.
     * @throws IOException if there was a problem fetching information from the Neo node.
     * @throws UnresolvableDomainNameException if the NNS text record could not be resolved.
     */
    public TransactionBuilder transfer(Hash160 from, NNSName to, BigInteger amount, ContractParameter data)
            throws IOException, UnresolvableDomainNameException {

        Hash160 toScriptHash = resolveNNSTextRecord(to);
        return transfer(from, toScriptHash, amount, data);
    }

    // endregion

}
