package com.axlabs.neow3j.crypto;

import com.axlabs.neow3j.utils.Numeric;

public class TestStuff {

    public static void main(String[] args) {

        byte[] privateKey = WIF.getPrivateKeyFromWIF("KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr");

        Credentials credential = Credentials.create(Numeric.toHexStringNoPrefix(privateKey));



    }

}
