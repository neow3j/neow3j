using System.Numerics;
using Neo.SmartContract.Framework;
using Neo.SmartContract.Framework.Services.Neo;

public class TestContract : SmartContract
{

    public static int main(int i) {
        return i;
    }

    public static BigInteger callNeoToken() {
        return NEO.GetGasPerBlock();
    }

    public static BigInteger callGasToken() {
        return GAS.TotalSupply();
    }
}