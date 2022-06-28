package io.neow3j.compiler;

import io.neow3j.contract.SmartContract;
import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.Permission;
import io.neow3j.devpack.contracts.NonDivisibleNonFungibleToken;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;

import static io.neow3j.devpack.Helper.reverse;
import static io.neow3j.devpack.StringLiteralHelper.hexToBytes;
import static io.neow3j.types.ContractParameter.byteArrayFromString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class NonDivisibleNFTIntegrationTest {

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule(NonDivisibleNFTTestContract.class.getName());

    @BeforeClass
    public static void setUp() throws Throwable {
        SmartContract sc = ct.deployContract(ConcreteNonDivisibleNFT.class.getName());
    }

    @Test
    public void testOwnerOf() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArrayFromString("io/neow3j/test"));
        io.neow3j.types.Hash160 owner = io.neow3j.types.Hash160.fromAddress(
                response.getInvocationResult().getStack().get(0).getAddress());
        assertThat(owner, is(io.neow3j.types.Hash160.ZERO));
    }

    @Permission(contract = "*")
    static class NonDivisibleNFTTestContract {

        static NonDivisibleNonFungibleToken nft = new NonDivisibleNonFungibleToken(
                new Hash160(reverse(hexToBytes("bd65d4664574381f4fc97da31e50738eac12a8b6").toByteArray())));

        public static Hash160 testOwnerOf(ByteString tokenId) {
            return nft.ownerOf(tokenId);
        }

    }

    static class ConcreteNonDivisibleNFT {

        public static Hash160 ownerOf(ByteString tokenId) {
            return Hash160.zero();
        }

    }

}
