package io.neow3j.examples.contractdevelopment.contracts;

import io.neow3j.devpack.List;
import io.neow3j.devpack.annotations.Permission;
import io.neow3j.devpack.constants.OracleResponseCode;
import io.neow3j.devpack.contracts.OracleContract;

@Permission(contract = "0xfe924b7cfe89ddd271abaf7210a80a7e11178758")
public class OracleTestContract {

    // This contract is used for the Neow3jExpressIntegrationTest integration test.
    // It is used to test the express rpc methods expresslistoraclerequests and
    // expresscreateoracleresponsetx.

    public static void request(int gasForResponse) {
        OracleContract.request("https://www.axlabs.com", "$.info", "callback", "", gasForResponse);
    }

    // Note: This method is not relevant for the test, since the response tx is never executed.
    public static List<String> callback(String url, String userData, byte code, String result) {
        List<String> list = new List<>();
        if (code == OracleResponseCode.Success) {
            list.add(url);
            list.add(userData);
            list.add(result);
        }
        return list;
    }

}