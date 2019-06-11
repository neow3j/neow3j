package io.neow3j.protocol.core.methods.response.notification;

import io.neow3j.model.types.ContractParameter;
import io.neow3j.protocol.core.ContractParameterParser;

import java.math.BigInteger;

public class NotificationParameter extends ContractParameter {

    public String getAsAddress() {
        return ContractParameterParser.readAddress(this);
    }

    public String getAsString() {
        return ContractParameterParser.readString(this);
    }

    public BigInteger getAsNumber() {
        return ContractParameterParser.readNumber(this);
    }

}
