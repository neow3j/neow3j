package io.neow3j.protocol.core.response;

import io.neow3j.protocol.core.Response;

import java.util.List;

public class NeoExpressListContracts extends Response<List<ExpressContractState>> {

    public List<ExpressContractState> getContracts() {
        return getResult();
    }

}
