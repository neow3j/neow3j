package io.neow3j.contract;

import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.transaction.Signer;
import io.neow3j.transaction.VerificationScript;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import io.neow3j.wallet.exceptions.InsufficientFundsException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a NEP-5 token contract and provides methods to invoke it.
 */
public class Nep5Token extends SmartContract {

    private static final String NEP5_NAME = "name";
    private static final String NEP5_TOTAL_SUPPLY = "totalSupply";
    private static final String NEP5_SYMBOL = "symbol";
    private static final String NEP5_DECIMALS = "decimals";
    private static final String NEP5_BALANCE_OF = "balanceOf";
    private static final String NEP5_TRANSFER = "transfer";

    private String name;
    // It is expected that Nep5 contracts return the total supply in fractions of their token.
    // Therefore an integer is used here instead of a decimal number.
    private BigInteger totalSupply;
    private Integer decimals;
    private String symbol;

    /**
     * Constructs a new {@code Nep5Token} representing the token contract with the given script
     * hash. Uses the given {@link Neow3j} instance for all invocations.
     *
     * @param scriptHash The token contract's script hash
     * @param neow       The {@link Neow3j} instance to use for invocations.
     */
    public Nep5Token(ScriptHash scriptHash, Neow3j neow) {
        super(scriptHash, neow);
    }

    /**
     * Gets the name of this token.
     * <p>
     * The return value is retrieved form the neo-node only once and then cached.
     *
     * @return the name.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something
     *                                       interpretable as a string.
     */
    public String getName() throws IOException, UnexpectedReturnTypeException {
        if (this.name == null) {
            this.name = callFuncReturningString(NEP5_NAME);
        }
        return this.name;
    }

    /**
     * Gets the symbol of this token.
     * <p>
     * The return value is retrieved form the neo-node only once and then cached.
     *
     * @return the symbol.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something
     *                                       interpretable as a string.
     */
    public String getSymbol() throws IOException, UnexpectedReturnTypeException {
        if (this.symbol == null) {
            this.symbol = callFuncReturningString(NEP5_SYMBOL);
        }
        return this.symbol;
    }

    /**
     * Gets the total supply of this token in fractions.
     * <p>
     * The return value is retrieved form the neo-node only once and then cached.
     *
     * @return the total supply.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something
     *                                       interpretable as a number.
     */
    public BigInteger getTotalSupply() throws IOException, UnexpectedReturnTypeException {
        if (this.totalSupply == null) {
            this.totalSupply = callFuncReturningInt(NEP5_TOTAL_SUPPLY);
        }
        return this.totalSupply;
    }

    /**
     * Gets the number of fractions that one unit of this token can be divided into.
     * <p>
     * The return value is retrieved form the neo-node only once and then cached.
     *
     * @return the the number of fractions.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something
     *                                       interpretable as a number.
     */
    public int getDecimals() throws IOException, UnexpectedReturnTypeException {
        if (this.decimals == null) {
            this.decimals = callFuncReturningInt(NEP5_DECIMALS).intValue();
        }
        return this.decimals;
    }

    /**
     * Gets the token balance for the given account script hash.
     * <p>
     * The token amount is returned in token fractions. E.g., an amount of 1 GAS is returned as
     * 1*10^8 GAS fractions.
     * <p>
     * The balance is not cached locally. Every time this method is called requests are send to the
     * neo-node.
     *
     * @param scriptHash The script hash of the account to fetch the balance for.
     * @return the token balance.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something
     *                                       interpretable as a number.
     */
    public BigInteger getBalanceOf(ScriptHash scriptHash) throws IOException,
            UnexpectedReturnTypeException {

        ContractParameter ofParam = ContractParameter.hash160(scriptHash);
        return callFuncReturningInt(NEP5_BALANCE_OF, ofParam);
    }

    /**
     * Gets the token balance for the given wallet, i.e., all accounts in the wallet.
     * <p>
     * The token amount is returned in token fractions. E.g., an amount of 1 GAS is returned as
     * 1*10^8 GAS fractions.
     * <p>
     * The balance is not cached locally. Every time this method is called requests are send to the
     * neo-node.
     *
     * @param wallet The wallet to fetch the balance for.
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
     * Creates and sends a transfer transaction that uses all accounts in the wallet to cover the
     * amount.
     * <p>
     * The default account is used first to cover the amount. If it cannot cover the full amount,
     * the other accounts in the wallet are iterated one by one to cover the remaining amount. If
     * the amount can be covered, all necessary transfers are sent in one transaction.
     *
     * @param wallet The wallet from which to send the tokens from.
     * @param to     The script hash of the receiver.
     * @param amount The amount to transfer as a decimal number (not token fractions).
     * @return The transaction id.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public NeoSendRawTransaction transfer(Wallet wallet, ScriptHash to, BigDecimal amount)
            throws IOException {
        if (amount.signum() < 0) {
            throw new IllegalArgumentException(
                    "The parameter amount must be greater than or equal to 0");
        }
        Invocation inv = buildTransferScript(wallet, to, amount);
        return inv.send();
    }

    // Method extracted for testability.
    Invocation buildTransferScript(Wallet wallet, ScriptHash to, BigDecimal amount)
            throws IOException {
        List<Account> accountsOrdered = new ArrayList<>(wallet.getAccounts());
        accountsOrdered.remove(wallet.getDefaultAccount());
        accountsOrdered.add(0, wallet.getDefaultAccount()); // Start with default account.
        return buildMultiTransferInvocation(wallet, to, amount, accountsOrdered);
    }

    /**
     * Creates and sends a transfer transaction that uses the provided accounts.
     * <p>
     * The accounts are used in the order provided to cover the transaction amount. If the first
     * account cannot cover the full amount, the second account is used to cover the remaining
     * amount and so on. If the amount can be covered, all necessary transfers are sent in one
     * transaction.
     *
     * @param wallet The wallet from which to send the tokens from.
     * @param to     The script hash of the receiver.
     * @param amount The amount to transfer as a decimal number (not token fractions).
     * @param from   The script hashes of the accounts in the wallet that should be used to cover
     *               the amount.
     * @return The transaction id.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public NeoSendRawTransaction transferFromSpecificAccounts(Wallet wallet, ScriptHash to,
            BigDecimal amount, ScriptHash... from) throws IOException {

        if (from.length == 0) {
            throw new IllegalArgumentException(
                    "An account address must be provided to build an invocation.");
        }
        if (amount.signum() < 0) {
            throw new IllegalArgumentException(
                    "The parameter amount must be greater than or equal to 0");
        }

        List<Account> accounts = new ArrayList<>();
        for (ScriptHash fromScriptHash : from) {
            Account a = wallet.getAccount(fromScriptHash);
            // Verify that potential multi-sig accounts can be used.
            if (a.isMultiSig() && !privateKeysArePresentForMultiSig(wallet, fromScriptHash)) {
                throw new IllegalArgumentException("The multi-sig account with script hash "
                        + fromScriptHash.toString() + " does not have the corresponding private "
                        + "keys in the wallet that are required for signing the transfer "
                        + "transaction.");
            }
            accounts.add(a);
        }
        return buildMultiTransferInvocation(wallet, to, amount, accounts).send();
    }

    Invocation buildMultiTransferInvocation(Wallet wallet, ScriptHash to, BigDecimal amount,
            List<Account> accounts) throws IOException {

        List<byte[]> scripts = new ArrayList<>(); // List of the individual invocation scripts.
        List<Signer> signers = new ArrayList<>(); // Accounts taking part in the transfer.
        Iterator<Account> it = accounts.iterator();
        BigInteger remainingAmount = getAmountAsBigInteger(amount);
        while (remainingAmount.signum() > 0 && it.hasNext()) {
            Account a = it.next();
            if (a.isMultiSig() && !privateKeysArePresentForMultiSig(wallet, a.getScriptHash())) {
                continue;
            }
            BigInteger balance = getBalanceOf(a.getScriptHash());
            if (balance.signum() <= 0) {
                continue;
            }
            signers.add(Signer.calledByEntry(a.getScriptHash()));
            if (balance.compareTo(remainingAmount) >= 0) {
                // Full remaining amount can be covered by current account.
                scripts.add(buildSingleTransferScript(a, to, remainingAmount));
            } else {
                // Full balance of account is needed but doesn't yet cover the full amount.
                scripts.add(buildSingleTransferScript(a, to, balance));
            }
            remainingAmount = remainingAmount.subtract(balance);
        }

        if (remainingAmount.signum() > 0) {
            BigInteger amountToCover = getAmountAsBigInteger(amount);
            BigInteger coveredAmount = amountToCover.subtract(remainingAmount);
            throw new InsufficientFundsException("The wallet does not hold enough tokens, resp. "
                    + "token-holding accounts with available private keys. The transfer amount is "
                    + amountToCover.toString() + " " + getSymbol() + " but the wallet only holds "
                    + coveredAmount.toString() + " " + getSymbol() + " (in token fractions).");
        }
        return buildTransferInvocation(wallet, scripts, signers);
    }

    private byte[] buildSingleTransferScript(Account acc, ScriptHash to, BigInteger amount) {
        List<ContractParameter> params = Arrays.asList(
                ContractParameter.hash160(acc.getScriptHash()),
                ContractParameter.hash160(to),
                ContractParameter.integer(amount));

        return new ScriptBuilder().contractCall(scriptHash, NEP5_TRANSFER, params).toArray();
    }

    private Invocation buildTransferInvocation(Wallet wallet, List<byte[]> scripts,
            List<Signer> signers) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (byte[] script : scripts) {
            byteArrayOutputStream.write(script);
        }
        byte[] concatenatedScript = byteArrayOutputStream.toByteArray();

        Invocation.Builder invocationBuilder = new Invocation.Builder(neow)
                .withWallet(wallet)
                .withScript(concatenatedScript)
                .failOnFalse();

        for (Signer signer : signers) {
            invocationBuilder.withSigners(signer);
        }

        return invocationBuilder.build().sign();
    }

    private boolean privateKeysArePresentForMultiSig(Wallet wallet, ScriptHash multiSig) {
        VerificationScript multiSigVerifScript = wallet.getAccount(multiSig)
                .getVerificationScript();
        int signers = 0;
        Account account;
        for (ECPublicKey pubKey : multiSigVerifScript.getPublicKeys()) {
            ScriptHash scriptHash = ScriptHash.fromPublicKey(pubKey.getEncoded(true));
            if (wallet.holdsAccount(scriptHash)) {
                account = wallet.getAccount(scriptHash);
                if (account != null && account.getECKeyPair() != null) {
                    signers += 1;
                }
            }
        }
        int signingThreshold = multiSigVerifScript.getSigningThreshold();
        return signers >= signingThreshold;
    }

    /**
     * Creates and sends a transfer transaction.
     * <p>
     * Uses only the wallet's default account to cover the token amount.
     *
     * @param wallet The wallet from which to send the tokens from.
     * @param to     The script hash of the receiver.
     * @param amount The amount to transfer as a decimal number (not token fractions).
     * @return The transaction id.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public NeoSendRawTransaction transferFromDefaultAccount(Wallet wallet, ScriptHash to,
            BigDecimal amount) throws IOException {
        if (amount.signum() < 0) {
            throw new IllegalArgumentException(
                    "The parameter amount must be greater than or equal to 0");
        }

        return buildTransferInvocation(wallet, to, amount).send();
    }

    // Method extracted for testability.
    Invocation buildTransferInvocation(Wallet wallet, ScriptHash to, BigDecimal amount)
            throws IOException {

        Account acc = wallet.getDefaultAccount();
        BigInteger fractions = getAmountAsBigInteger(amount);
        BigInteger accBalance = getBalanceOf(acc.getScriptHash());
        if (accBalance.compareTo(fractions) < 0) {
            throw new InsufficientFundsException("The wallet's default account does not hold enough"
                    + " tokens. Transfer amount is " + fractions.toString() + " but account"
                    + " only holds " + accBalance.toString() + " (in token fractions).");
        }
        return invoke(NEP5_TRANSFER)
                .withSigners(Signer.calledByEntry(acc.getScriptHash()))
                .withWallet(wallet)
                .withParameters(
                        ContractParameter.hash160(acc.getScriptHash()),
                        ContractParameter.hash160(to),
                        ContractParameter.integer(fractions)
                )
                .build()
                .sign();
    }

    private BigInteger getAmountAsBigInteger(BigDecimal amount) throws IOException {
        BigDecimal factor = BigDecimal.TEN.pow(getDecimals());
        return amount.multiply(factor).toBigInteger();
    }
}
