package io.neow3j.contract;

import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.wallet.Wallet;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class Nep5Token extends SmartContract {

    private static final String NEP5_NAME = "name";
    private static final String NEP5_TOTAL_SUPPLY = "totalSupply";
    private static final String NEP5_SYMBOL = "symbol";
    private static final String NEP5_DECIMALS = "decimals";
    private static final String NEP5_BALANCE_OF = "balanceOf";
    private static final String NEP5_TRANSFER = "transfer";

    private String name;
    // TODO: Determine if this is the amount in fractions of the token.
    private BigInteger totalSupply;
    private Integer decimals;
    private String symbol;

    public Nep5Token(ScriptHash scriptHash, Neow3j neow) {
        super(scriptHash, neow);
    }

    public String getName() throws IOException {
        if (this.name == null) {
            fetchName();
        }
        return this.name;
    }

    public String getSymbol() throws IOException {
        if (this.symbol == null) {
            fetchSymbol();
        }
        return this.symbol;
    }

    public BigInteger getTotalSupply() throws Exception {
        if (this.totalSupply == null) {
            fetchTotalSupply();
        }
        return this.totalSupply;
    }

    public int getDecimals() throws Exception {
        if (this.decimals == null) {
            fetchDecimals();
        }
        return this.decimals;
    }

    public boolean transfer(Wallet wallet, ScriptHash from, ScriptHash to, BigDecimal amount)
            throws Exception {
        BigInteger intAmount = amount.multiply(BigDecimal.TEN.pow(getDecimals())).toBigInteger();
        invoke(NEP5_TRANSFER)
                .withWallet(wallet)
                .withParameters(
                        ContractParameter.byteArrayFromAddress(from.toAddress()),
                        ContractParameter.byteArrayFromAddress(to.toAddress()),
                        ContractParameter.integer(intAmount)
                ).build()
                .send();
        return true;
        // TODO: Add an ASSERT OpCode at the end of the script to make the transfer invocation fail
        //  if the return value is false.
        // TODO: Do error checking.
    }

    private void fetchName() throws IOException {
        this.name = callFuncReturningString(NEP5_NAME);
    }

    private void fetchSymbol() throws IOException {
        this.symbol = callFuncReturningString(NEP5_SYMBOL);
    }

    private void fetchTotalSupply() throws Exception {
        this.totalSupply = callFuncReturningInt(NEP5_TOTAL_SUPPLY);
    }

    private void fetchDecimals() throws Exception {
        this.decimals = callFuncReturningInt(NEP5_DECIMALS).intValue();
    }

    private String callFuncReturningString(String function) throws IOException {
        return invoke(function).run().getInvocationResult().getStack().get(0)
                .asByteArray().getAsString();
    }

    private BigInteger callFuncReturningInt(String function) throws Exception {
        StackItem item = invoke(function).run().getInvocationResult().getStack().get(0);
        if (item.getType().equals(StackItemType.INTEGER)) {
            return item.asInteger().getValue();
        }
        if (item.getType().equals(StackItemType.BYTE_ARRAY)) {
            return item.asByteArray().getAsNumber();
        }
        // TODO: Throw specific exception.
        throw new Exception();
    }

//    protected class Nep5InvocationBuilder extends BaseInvocationBuilder<Nep5InvocationBuilder> {
//
//        public Nep5InvocationBuilder(String function) {
//            super(function);
//        }
//
//        @Override
//        public Nep5InvocationBuilder getThis() {
//            return this;
//        }
//    }
}
