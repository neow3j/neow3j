package io.neow3j.contract;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.RecordType;

import java.io.IOException;
import java.math.BigInteger;

import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.string;

/**
 * Represents the NameService native contract and provides methods to invoke its functions.
 */
public class NeoNameService extends NonFungibleToken {

    public final static String NAME = "NameService";
    public final static long NEF_CHECKSUM = 3740064217L;
    public static final ScriptHash SCRIPT_HASH = getScriptHashOfNativeContract(NEF_CHECKSUM, NAME);

//    private static final String ADD_ROOT = "addRoot";
//    private static final String DELETE_RECORD = "deleteRecord";
    private static final String GET_PRICE = "getPrice";
    private static final String GET_RECORD = "getRecord";
    private static final String IS_AVAILABLE = "isAvailable";
//    private static final String REGISTER = "register";
//    private static final String RENEW = "renew";
    private static final String RESOLVE = "resolve";
//    private static final String SET_ADMIN = "setAdmin";
//    private static final String SET_PRICE = "setPrice";
//    private static final String SET_RECORD = "setRecord";

    /**
     * Constructs a new {@code NeoToken} that uses the given {@link Neow3j} instance for
     * invocations.
     *
     * @param neow the {@link Neow3j} instance to use for invocations.
     */
    public NeoNameService(Neow3j neow) {
        super(SCRIPT_HASH, neow);
    }

    /**
     * Returns the name of the NeoToken contract. Doesn't require a call to the Neo node.
     *
     * @return the name.
     */
    @Override
    public String getName() {
        return NAME;
    }

    // only admins allowed?
//    public TransactionBuilder addRoot(String root) {}

    // 
//    public TransactionBuilder deleteRecord(String name, RecordType type) {}

    public BigInteger getPrice() throws IOException {
        return callFuncReturningInt(GET_PRICE);
    }

    public String getRecord(String name, RecordType type) throws IOException {
        return callFuncReturningString(GET_RECORD, string(name), integer(type.byteValue()));
    }

    public boolean isAvailable(String name) throws IOException {
        return callFuncReturningBool(IS_AVAILABLE, string(name));
    }

    public TransactionBuilder register(String name, ScriptHash owner) {
        return new TransactionBuilder(neow);
    }

    public TransactionBuilder renew(String name) {
        return new TransactionBuilder(neow);
    }

    // returns AnyStackItem=null
    public String resolve(String name, RecordType type) throws IOException {
        return callFuncReturningString(RESOLVE, string(name), integer(type.byteValue()));
    }

    // only committee allowed?
//    public TransactionBuilder setAdmin(String name, ScriptHash admin) {}

    // only admins or committee allowed?
//    public TransactionBuilder setPrice(BigInteger price) {}

    // everyone allowed?
//    public TransactionBuilder setRecord(String name, RecordType type, String data) {}

}
