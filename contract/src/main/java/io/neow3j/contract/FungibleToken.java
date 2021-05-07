package io.neow3j.contract;

import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.protocol.Neow3j;
import io.neow3j.transaction.Signer;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import io.neow3j.wallet.exceptions.InsufficientFundsException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static io.neow3j.contract.ContractParameter.hash160;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.transaction.Signer.calledByEntry;
import static java.util.Arrays.asList;

/**
 * Represents a fungible token contract that is compliant with the NEP-17 standard and provides
 * methods to invoke it.
 */
public class FungibleToken extends Token {

    private static final String BALANCE_OF = "balanceOf";
    private static final String TRANSFER = "transfer";

    /**
     * Constructs a new {@code FungibleToken} representing the token contract with the given script
     * hash. Uses the given {@link Neow3j} instance for all invocations.
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
     * The token amount is returned in token fractions. E.g., an amount of 1 GAS is returned as
     * 1*10^8 GAS fractions.
     * <p>
     * The balance is not cached locally. Every time this method is called requests are sent to the
     * Neo node.
     *
     * @param account the account to fetch the balance for.
     * @return the token balance.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something
     *                                       interpretable as a number.
     */
    public BigInteger getBalanceOf(Account account) throws IOException,
            UnexpectedReturnTypeException {
        return getBalanceOf(account.getScriptHash());
    }

    /**
     * Gets the token balance for the given account script hash.
     * <p>
     * The token amount is returned in token fractions. E.g., an amount of 1 GAS is returned as
     * 1*10^8 GAS fractions.
     * <p>
     * The balance is not cached locally. Every time this method is called requests are sent to the
     * Neo node.
     *
     * @param scriptHash the script hash of the account to fetch the balance for.
     * @return the token balance.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something
     *                                       interpretable as a number.
     */
    public BigInteger getBalanceOf(Hash160 scriptHash) throws IOException,
            UnexpectedReturnTypeException {

        ContractParameter ofParam = hash160(scriptHash);
        return callFuncReturningInt(BALANCE_OF, ofParam);
    }

    /**
     * Gets the token balance for the given wallet, i.e., all accounts in the wallet.
     * <p>
     * The token amount is returned in token fractions. E.g., an amount of 1 GAS is returned as
     * 1*10^8 GAS fractions.
     * <p>
     * The balance is not cached locally. Every time this method is called requests are sent to the
     * Neo node.
     *
     * @param wallet the wallet to fetch the balance for.
     * @return the token balance.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something
     *                                       interpretable as a number.
     */
    public BigInteger getBalanceOf(Wallet wallet) throws IOException,
            UnexpectedReturnTypeException {

        BigInteger sum = BigInteger.ZERO;
        for (Account a : wallet.getAccounts()) {
            sum = sum.add(getBalanceOf(a.getScriptHash()));
        }
        return sum;
    }

    /**
     * Creates a transfer transaction that uses all accounts in the wallet to cover the amount.
     * <p>
     * The default account is used first to cover the amount. If it cannot cover the full amount,
     * the other accounts in the wallet are iterated one by one to cover the remaining amount. If
     * the amount can be covered, all necessary transfers packed in one transaction.
     *
     * @param wallet the wallet from which to send the tokens from.
     * @param to     the script hash of the receiver.
     * @param amount the amount to transfer in token fractions.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transfer(Wallet wallet, Hash160 to, BigInteger amount)
            throws IOException {
        return transfer(wallet, to, amount, null);
    }

    /**
     * Creates a transfer transaction that uses all accounts in the wallet to cover the amount.
     * <p>
     * The default account is used first to cover the amount. If it cannot cover the full amount,
     * the other accounts in the wallet are iterated one by one to cover the remaining amount. If
     * the amount can be covered, all necessary transfers packed in one transaction.
     * <p>
     * Only use this method when the receiver is a deployed smart contract to avoid unnecessary
     * additional fees. Otherwise, use the method without a contract parameter for data.
     *
     * @param wallet the wallet from which to send the tokens from.
     * @param to     the script hash of the receiver.
     * @param amount the amount to transfer in token fractions.
     * @param data   the data that is passed to the {@code onPayment} method of the receiving
     *               smart contract.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transfer(Wallet wallet, Hash160 to, BigInteger amount,
            ContractParameter data) throws IOException {
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("The parameter amount must be greater than or " +
                    "equal to 0");
        }

        List<Account> accountsOrdered = new ArrayList<>(wallet.getAccounts());
        accountsOrdered.remove(wallet.getDefaultAccount());
        accountsOrdered.add(0, wallet.getDefaultAccount()); // Start with default account.
        return buildMultiTransferInvocation(wallet, to, amount, accountsOrdered, data);
    }

    /**
     * Creates a transfer transaction that uses the provided accounts.
     * <p>
     * The accounts are used in the order provided to cover the transaction amount. If the first
     * account cannot cover the full amount, the second account is used to cover the remaining
     * amount and so on. If the amount can be covered by the specified accounts, all necessary
     * transfers are packed in one transaction.
     *
     * @param wallet the wallet from which to send the tokens from.
     * @param to     the address of the receiver.
     * @param amount the amount to transfer in token fractions.
     * @param from   the script hashes of the accounts in the wallet that should be used to cover
     *               the amount.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transferFromSpecificAccounts(Wallet wallet, Hash160 to,
            BigInteger amount, Hash160... from) throws IOException {
        return transferFromSpecificAccounts(wallet, to, amount, null, from);
    }

    /**
     * Creates a transfer transaction that uses the provided accounts.
     * <p>
     * The accounts are used in the order provided to cover the transaction amount. If the first
     * account cannot cover the full amount, the second account is used to cover the remaining
     * amount and so on. If the amount can be covered by the specified accounts, all necessary
     * transfers are packed in one transaction.
     * <p>
     * Only use this method when the receiver is a deployed smart contract to avoid unnecessary
     * additional fees. Otherwise, use the method without a contract parameter for data.
     *
     * @param wallet the wallet from which to send the tokens from.
     * @param to     the script hash of the receiver.
     * @param amount the amount to transfer in token fractions.
     * @param data   the data that is passed to the {@code onPayment} method of the receiving
     *               smart contract.
     * @param from   the script hashes of the accounts in the wallet that should be used to cover
     *               the amount.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transferFromSpecificAccounts(Wallet wallet, Hash160 to,
            BigInteger amount, ContractParameter data, Hash160... from) throws IOException {

        if (from.length == 0) {
            throw new IllegalArgumentException(
                    "An account address must be provided to build an invocation.");
        }
        if (amount.signum() < 0) {
            throw new IllegalArgumentException(
                    "The parameter amount must be greater than or equal to 0");
        }

        List<Account> accounts = new ArrayList<>();
        for (Hash160 fromScriptHash : from) {
            Account a = wallet.getAccount(fromScriptHash);
            // TODO: 15.10.20 Michael: Remove this multi-sig check. The signers for a multi-sig can
            //  still be added to the TransactionBuilder.
            // Verify that potential multi-sig accounts can be used.
            if (a.isMultiSig() && a.getVerificationScript() != null &&
                    !wallet.privateKeysArePresentForMultiSig(a.getVerificationScript())) {
                throw new IllegalArgumentException("The multi-sig account with script hash " +
                        fromScriptHash.toString() + " does not have the corresponding private " +
                        "keys in the wallet that are required for signing the transfer " +
                        "transaction.");
            }
            accounts.add(a);
        }
        return buildMultiTransferInvocation(wallet, to, amount, accounts, data);
    }

    TransactionBuilder buildMultiTransferInvocation(Wallet wallet, Hash160 to, BigInteger amount,
            List<Account> accounts, ContractParameter data) throws IOException {

        List<byte[]> scripts = new ArrayList<>(); // List of the individual invocation scripts.
        List<Signer> signers = new ArrayList<>(); // Accounts taking part in the transfer.
        Iterator<Account> it = accounts.iterator();
        BigInteger remainingAmount = amount;
        while (remainingAmount.signum() > 0 && it.hasNext()) {
            Account a = it.next();
            if (a.isMultiSig() && a.getVerificationScript() != null &&
                    !wallet.privateKeysArePresentForMultiSig(a.getVerificationScript())) {
                continue;
            }
            BigInteger balance = getBalanceOf(a.getScriptHash());
            if (balance.signum() <= 0) {
                continue;
            }
            signers.add(calledByEntry(a.getScriptHash()));
            if (balance.compareTo(remainingAmount) >= 0) {
                // Full remaining amount can be covered by current account.
                scripts.add(buildSingleTransferScript(a, to, remainingAmount, data));
            } else {
                // Full balance of account is needed but doesn't yet cover the full amount.
                scripts.add(buildSingleTransferScript(a, to, balance, data));
            }
            remainingAmount = remainingAmount.subtract(balance);
        }

        if (remainingAmount.signum() > 0) {
            BigInteger coveredAmount = amount.subtract(remainingAmount);
            throw new InsufficientFundsException("The wallet does not hold enough tokens (i.e. " +
                    "token-holding accounts with available private keys). The transfer amount is " +
                    amount + " " + getSymbol() + " but the wallet only holds " + coveredAmount +
                    " " + getSymbol() + " (in token fractions).");
        }
        return assembleMultiTransferTransaction(wallet, scripts, signers);
    }

    private byte[] buildSingleTransferScript(Account acc, Hash160 to, BigInteger amount,
            ContractParameter data) {
        List<ContractParameter> params;
        params = asList(
                hash160(acc.getScriptHash()),
                hash160(to),
                integer(amount),
                data);

        return new ScriptBuilder().contractCall(scriptHash, TRANSFER, params).toArray();
    }

    private TransactionBuilder assembleMultiTransferTransaction(Wallet wallet, List<byte[]> scripts,
            List<Signer> signers) throws IOException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (byte[] script : scripts) {
            byteArrayOutputStream.write(script);
        }
        byte[] concatenatedScript = byteArrayOutputStream.toByteArray();

        return new TransactionBuilder(neow3j)
                .wallet(wallet)
                .script(concatenatedScript)
                .signers(signers.toArray(new Signer[]{}));
    }

    /**
     * Creates a transfer transaction that uses only the wallet's default account to cover the
     * token amount.
     *
     * @param wallet the wallet from which to send the tokens from.
     * @param to     the address of the receiver.
     * @param amount the amount to transfer in token fractions.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transferFromDefaultAccount(Wallet wallet, Hash160 to,
            BigInteger amount) throws IOException {
        return transferFromDefaultAccount(wallet, to, amount, null);
    }

    /**
     * Creates a transfer transaction that uses only the wallet's default account to cover the
     * token amount.
     * <p>
     * Only use this method when the receiver is a deployed smart contract to avoid unnecessary
     * additional fees. Otherwise, use the method without a contract parameter for data.
     *
     * @param wallet the wallet from which to send the tokens from.
     * @param to     the script hash of the receiver.
     * @param amount the amount to transfer in token fractions.
     * @param data   the data that is passed to the {@code onPayment} method of the receiving
     *               smart contract.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transferFromDefaultAccount(Wallet wallet, Hash160 to,
            BigInteger amount, ContractParameter data) throws IOException {
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("The amount must be greater than or equal to 0.");
        }

        Account acc = wallet.getDefaultAccount();
        BigInteger accBalance = getBalanceOf(acc.getScriptHash());
        if (accBalance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("The wallet's default account does not hold " +
                    "enough tokens. Transfer amount is " + amount + " but account only holds " +
                    accBalance + ".");
        }

        TransactionBuilder b;
        b = invokeFunction(TRANSFER,
                hash160(acc.getScriptHash()),
                hash160(to),
                integer(amount),
                data);


        return b.wallet(wallet)
                .signers(calledByEntry(acc.getScriptHash()));
    }

}
