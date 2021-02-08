package io.neow3j.contract;

import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.core.methods.response.ArrayStackItem;
import io.neow3j.protocol.core.methods.response.ContractManifest;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI.ContractEvent;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI.ContractMethod;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.protocol.core.methods.response.StructStackItem;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ManifestFile extends ContractManifest {

    public ManifestFile(String name,
            List<ContractGroup> groups, List<String> supportedStandards,
            ContractABI abi,
            List<ContractPermission> permissions, List<String> trusts, Object extra) {
        super(name, groups, supportedStandards, abi, permissions, trusts, extra);
    }

    public static ManifestFile fromStackItem(Object stackItem) {

        if (!(stackItem instanceof StackItem)) {
            throw new IllegalArgumentException("Trying to read from an object which is not "
                    + "StackItem.");
        }
        StackItem si = ((StackItem) stackItem);
        // the 'nef' is represented in a ByteString stack item
        if (!si.getType().equals(StackItemType.STRUCT)) {
            throw new UnexpectedReturnTypeException(si.getType(), StackItemType.STRUCT);
        }
        StructStackItem structStackItem = si.asStruct();
        List<StackItem> listOfStackItems = structStackItem.getValue();

        // name:
        String name = listOfStackItems.get(0).asByteString().getAsString();

        // groups:
        ArrayStackItem groupsStackItems = listOfStackItems.get(1).asArray();
        List<ContractGroup> groups = new ArrayList<>();
        for (StackItem item : groupsStackItems.getValue()) {
            StructStackItem groupObjStackItem = item.asStruct();
            String pubKey = groupObjStackItem.get(0).asByteString().getAsString();
            String signature = groupObjStackItem.get(1).asByteString().getAsString();
            ContractGroup contractGroup = new ContractGroup(pubKey, signature);
            groups.add(contractGroup);
        }

        // supportedstandards:
        ArrayStackItem standardStackItems = listOfStackItems.get(2).asArray();
        List<String> supportedStandards = new ArrayList<>();
        for (StackItem item : standardStackItems.getValue()) {
            String standard = item.asByteString().getAsString();
            supportedStandards.add(standard);
        }

        // abi:
        StructStackItem abiStackItems = listOfStackItems.get(3).asStruct();
        List<ContractMethod> abiMethods = new ArrayList<>();
        List<ContractEvent> abiEvents = new ArrayList<>();
        // abi: the first position (i=0) is always the methods
        if (abiStackItems.get(0) != null) {
            // abi methods:
            ArrayStackItem methodsStackItems = abiStackItems.get(0).asArray();
            for (StackItem item : methodsStackItems.getValue()) {
                StructStackItem methodObjStackItem = item.asStruct();

                // ContractMethod:

                // method name:
                String methodName = methodObjStackItem.get(0).asByteString().getAsString();

                // method params:
                List<ContractParameter> methodParams = new ArrayList<>();
                ArrayStackItem paramStackItems = methodObjStackItem.get(1).asArray();
                for (StackItem paramStackItem : paramStackItems.getValue()) {
                    StructStackItem contractParamObj = paramStackItem.asStruct();

                    // ContractParameter
                    // parameter name:
                    String paramName = contractParamObj.get(0).asByteString().getAsString();

                    // parameter type:
                    BigInteger paramTypeInt = contractParamObj.get(1).asInteger().getValue();
                    ContractParameterType paramType = ContractParameterType
                            .valueOf(paramTypeInt.byteValue());

                    // TODO: 07.02.21 Guil:
                    // We do not consider the "value" here. Is it relevant for the manifest file?
                    // I don't think so. But I will leave this note here until we're 100%
                    // sure that it's not relevant.
                    // StackItem valueStackItem = contractParamObj.get(2);

                    // ContractParameter object
                    ContractParameter contractParameter = new ContractParameter(paramName,
                            paramType);
                    methodParams.add(contractParameter);
                }

                // return param:
                BigInteger returnParamTypeInt = methodObjStackItem.get(2).asInteger().getValue();
                ContractParameterType returnParamType = ContractParameterType
                        .valueOf(returnParamTypeInt.byteValue());

                // offset:
                BigInteger offset = methodObjStackItem.get(3).asInteger().getValue();

                // safe:
                boolean isSafe = methodObjStackItem.get(4).asBoolean().getValue();

                // ContractMethod object
                ContractMethod contractMethod = new ContractMethod(methodName, methodParams,
                        offset.intValue(), returnParamType, isSafe);

                // add to the ABI methods's list
                abiMethods.add(contractMethod);
            }
        }
        // abi: the second position (i=1) is always the events
        if (abiStackItems.get(1) != null) {

            // abi events:
            ArrayStackItem eventsStackItems = abiStackItems.get(1).asArray();
            for (StackItem item : eventsStackItems.getValue()) {
                StructStackItem eventObjStackItem = item.asStruct();

                // event name:
                String eventName = eventObjStackItem.get(0).asByteString().getAsString();
                // event parameters:
                List<ContractParameter> eventParams = new ArrayList<>();
                ArrayStackItem paramStackItems = eventObjStackItem.get(1).asArray();
                for (StackItem paramStackItem : paramStackItems.getValue()) {
                    StructStackItem contractParamObj = paramStackItem.asStruct();

                    // ContractParameter
                    // parameter name:
                    String paramName = contractParamObj.get(0).asByteString().getAsString();

                    // parameter type:
                    BigInteger paramTypeInt = contractParamObj.get(1).asInteger().getValue();
                    ContractParameterType paramType = ContractParameterType
                            .valueOf(paramTypeInt.byteValue());

                    // TODO: 07.02.21 Guil:
                    // We do not consider the "value" here. Is it relevant for the manifest file?
                    // I don't think so. But I will leave this note here until we're 100%
                    // sure that it's not relevant.
                    // StackItem valueStackItem = contractParamObj.get(2);

                    // ContractParameter object
                    ContractParameter contractParameter = new ContractParameter(paramName,
                            paramType);
                    eventParams.add(contractParameter);
                }

                ContractEvent contractEvent = new ContractEvent(eventName, eventParams);
                abiEvents.add(contractEvent);
            }
        }
        ContractABI contractABI = new ContractABI(abiMethods, abiEvents);

        // permissions:
        List<ContractPermission> contractPermissions = new ArrayList<>();
        ArrayStackItem permissionStackItems = listOfStackItems.get(4).asArray();
        for (StackItem item : permissionStackItems.getValue()) {
            StructStackItem contractPermissionObj = item.asStruct();

            // contract:
            StackItem contractStackItemObj = contractPermissionObj.get(0);
            String contract = null;
            if (contractStackItemObj.getType() == StackItemType.ANY) {
                contract = "*";
            } else if (contractStackItemObj.getType() == StackItemType.BYTE_STRING) {
                contract = contractStackItemObj.asByteString().getAsString();
            }

            // methods:
            List<String> methodPermissions = new ArrayList<>();
            StackItem methodStackItemObj = contractPermissionObj.get(1);
            if (methodStackItemObj.getType() == StackItemType.ANY) {
                methodPermissions.add("*");
            } else if (methodStackItemObj.getType() == StackItemType.ARRAY) {
                ArrayStackItem methodStackItems = methodStackItemObj.asArray();
                for (StackItem methodPermissionItem : methodStackItems.getValue()) {
                    String methodPermission = methodPermissionItem.asByteString().getAsString();
                    methodPermissions.add(methodPermission);
                }
            }

            ContractPermission contractPermission = new ContractPermission(contract,
                    methodPermissions);
            contractPermissions.add(contractPermission);
        }

        // trusts:
        List<String> trusts = new ArrayList<>();
        ArrayStackItem trustStackItems = listOfStackItems.get(5).asArray();
        for (StackItem item : trustStackItems.getValue()) {
            if (item.getType() == StackItemType.BYTE_STRING) {
                String trust = item.asByteString().getAsString();
                trusts.add(trust);
            }
        }

        return new ManifestFile(name, groups, supportedStandards, contractABI,
                contractPermissions, trusts, null);

    }


}
