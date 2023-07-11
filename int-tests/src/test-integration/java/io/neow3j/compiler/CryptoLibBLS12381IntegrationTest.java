package io.neow3j.compiler;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.InteropInterface;
import io.neow3j.devpack.contracts.CryptoLib;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.types.NeoVMStateType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;

import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CryptoLibBLS12381IntegrationTest {

    private String testName;

    private final String G1_HEX =
            "97f1d3a73197d7942695638c4fa9ac0fc3688c4f9774b905a14e3a3f171bac586c55e83ff97a1aeffb3af00adb22c6bb";
    private final byte[] G1 = hexStringToByteArray(G1_HEX);

    private final String G2_HEX =
            "93e02b6052719f607dacd3a088274f65596bd0d09920b61ab5da61bbdc7f5049334cf11213945d57e5ac7d055d042b7e024aa2b2f08f0a91260805272dc51051c6e47ad4fa403b02b4510b647ae3d1770bac0326a805bbefd48056c8c121bdb8";
    private final byte[] G2 = hexStringToByteArray(G2_HEX);

    private final String GT_HEX =
            "0f41e58663bf08cf068672cbd01a7ec73baca4d72ca93544deff686bfd6df543d48eaa24afe47e1efde449383b67663104c581234d086a9902249b64728ffd21a189e87935a954051c7cdba7b3872629a4fafc05066245cb9108f0242d0fe3ef03350f55a7aefcd3c31b4fcb6ce5771cc6a0e9786ab5973320c806ad360829107ba810c5a09ffdd9be2291a0c25a99a211b8b424cd48bf38fcef68083b0b0ec5c81a93b330ee1a677d0d15ff7b984e8978ef48881e32fac91b93b47333e2ba5706fba23eb7c5af0d9f80940ca771b6ffd5857baaf222eb95a7d2809d61bfe02e1bfd1b68ff02f0b8102ae1c2d5d5ab1a19f26337d205fb469cd6bd15c3d5a04dc88784fbb3d0b2dbdea54d43b2b73f2cbb12d58386a8703e0f948226e47ee89d018107154f25a764bd3c79937a45b84546da634b8f6be14a8061e55cceba478b23f7dacaa35c8ca78beae9624045b4b601b2f522473d171391125ba84dc4007cfbf2f8da752f7c74185203fcca589ac719c34dffbbaad8431dad1c1fb597aaa5193502b86edb8857c273fa075a50512937e0794e1e65a7617c90d8bd66065b1fffe51d7a579973b1315021ec3c19934f1368bb445c7c2d209703f239689ce34c0378a68e72a6b3b216da0e22a5031b54ddff57309396b38c881c4c849ec23e87089a1c5b46e5110b86750ec6a532348868a84045483c92b7af5af689452eafabf1a8943e50439f1d59882a98eaa0170f1250ebd871fc0a92a7b2d83168d0d727272d441befa15c503dd8e90ce98db3e7b6d194f60839c508a84305aaca1789b6";
    private final byte[] GT = hexStringToByteArray(GT_HEX);

    private final byte[] NOT_G1 = hexStringToByteArray(
            "8123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
    private final byte[] NOT_G2 = hexStringToByteArray(
            "8123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension(
            CryptoLibIntegrationTest.CryptoLibIntegrationTestContract.class.getName());

    @BeforeEach
    void init(TestInfo testInfo) {
        testName = testInfo.getTestMethod().get().getName();
    }

    @Test

    public void testG1() throws IOException {
        InvocationResult result = ct.callInvokeFunction(testName, byteArray(G1)).getInvocationResult();
        assertThat(result.getState(), is(NeoVMStateType.HALT));
        assertThat(result.getStack().get(0).getHexString(),
                is("97f1d3a73197d7942695638c4fa9ac0fc3688c4f9774b905a14e3a3f171bac586c55e83ff97a1aeffb3af00adb22c6bb"));
    }

    @Test
    public void bls12381Serialize() throws IOException {
        NeoInvokeFunction response = ct.callInvokeFunction(testName, byteArray("0102030405"));
        assertThat(response.getInvocationResult().getStack().get(0).getHexString(),
                is("74f81fe167d99b4cb41d6d0ccda82278caee9f3e2f25d5e5a3936ff3dcec60d0"));
    }

    static class CryptoLibBLS12381IntegrationTestContract {

        static final CryptoLib cryptoLib = new CryptoLib();

        public static InteropInterface testG1(ByteString value) {
            return cryptoLib.Bls12381Deserialize(value);
        }

        public static ByteString bls12381Serialize(ByteString value) {
            return cryptoLib.sha256(value);
        }

    }

}
