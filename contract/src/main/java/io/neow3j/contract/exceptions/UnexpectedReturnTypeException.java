package io.neow3j.contract.exceptions;

import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.StackItemType;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Is thrown when the attempt to interpret a {@link StackItem}} fails because the item was not of an expected type.
 * If this exception occurs it means that either the called contract did not fulfill an expected standard or the
 * caller expected wrong return types.
 * <p>
 * This is an unchecked exception because recovering from this exception can only be done by either changing the
 * contract itself or the code calling the contract.
 */
public class UnexpectedReturnTypeException extends RuntimeException {

    public UnexpectedReturnTypeException(StackItemType type, StackItemType... expectedTypes) {
        super(String.format("Got stack item of type %s but expected %s.", type.jsonValue(),
                Stream.of(expectedTypes)
                        .map(StackItemType::jsonValue)
                        .collect(Collectors.joining(", ", "", ""))));
    }

    public UnexpectedReturnTypeException(String message, Exception e) {
        super(message, e);
    }

}
