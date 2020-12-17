package io.neow3j.contract;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.OpCode;
import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.ContractManifest;
import io.neow3j.protocol.core.methods.response.NeoGetContractState.ContractState;
import io.neow3j.protocol.core.methods.response.StackItem;

import java.io.IOException;
import java.util.Arrays;

/**
 * Represents a Management contract and provides methods to invoke it.
 */
public class ManagementContract extends SmartContract {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String NAME = "Neo Contract Management";
    public static final ScriptHash SCRIPT_HASH = getScriptHashOfNativeContract(NAME);
    private static final String GET_CONTRACT = "getContract";
    private static final String DEPLOY = "deploy";
    private static final String UPDATE = "update";
    private static final String DESTROY = "destroy";

    /**
     * Constructs a new <tt>ManagementContract</tt> that uses the given {@link Neow3j} instance for
     * invocations.
     *
     * @param neow The {@link Neow3j} instance to use for invocations.
     */
    public ManagementContract(Neow3j neow) {
        super(SCRIPT_HASH, neow);
    }

    /**
     * Returns the state of a smart contract.
     *
     * @param scriptHash The script hash of the smart contract.
     * @return The state of the smart contract.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public ContractState getContractState(ScriptHash scriptHash) throws IOException {
        StackItem stackItem = callInvokeFunction(GET_CONTRACT,Arrays.asList(ContractParameter.hash160(scriptHash)))
                .getInvocationResult().getStack().get(0);
        if (!stackItem.getType().equals(StackItemType.BYTE_STRING)) {
            throw new UnexpectedReturnTypeException(stackItem.getType(), StackItemType.BYTE_STRING);
        }
        return stackItem.asByteString().getAsJson(ContractState.class);

        /*
         TODO: 14.12.20 Michael: check of what type the return actually is.
          If of type array, use the following:
                StackItem stackItem = callInvokeFunction(GET_CONTRACT).getInvocationResult()
                        .getStack().get(0);
                if (!stackItem.getType().equals(StackItemType.ARRAY)) {
                    throw new UnexpectedReturnTypeException(stackItem.getType(), StackItemType.ARRAY);
                }
                ArrayStackItem arrayItem = stackItem.asArray();
                BigInteger id = arrayItem.get(0).asInteger().getValue();
                BigInteger updateCounter = arrayItem.get(1).asInteger().getValue();
                ScriptHash hash = new ScriptHash(arrayItem.get(2).asByteString().getAsString());
                byte[] script = arrayItem.get(3).asByteString().getAsString().getBytes();
                ContractState state = arrayItem.get(4).asByteString().getAsJson(ContractState.class);
        */
    }

    /**
     * Creates a transaction script for deploying a smart contract and initializes
     * a {@link TransactionBuilder} based on this script.
     *
     * @param nefFile The NEF file of the new smart contract.
     * @param manifest The contract manifest of the new smart contract.
     * @return A transaction builder.
     */
    public TransactionBuilder deploy(NefFile nefFile, ContractManifest manifest)
            throws JsonProcessingException {

        // TODO: 14.12.20 Michael: Check whether parameters are in correct form.
        return invokeFunction(DEPLOY, ContractParameter.byteArray(nefFile.getScript()),
                ContractParameter.byteArray(objectMapper.writeValueAsBytes(manifest)));
    }
}
