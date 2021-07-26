package io.neow3j.protocol;

import io.neow3j.protocol.core.response.PopulatedBlocks;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.test.NeoTestContainer;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;

import static io.neow3j.protocol.IntegrationTestHelper.NEO_HASH;
import static io.neow3j.test.NeoTestContainer.getNodeUrl;
import static io.neow3j.utils.Await.waitUntilOpenWalletHasBalanceGreaterThanOrEqualTo;
import static org.junit.Assert.assertNotNull;

public class Neow3jExpressIntegrationTest {
    protected static Neow3jExpress neow3jExpress;

    @ClassRule
    public static NeoTestContainer neoTestContainer = new NeoTestContainer(true);

    @BeforeClass
    public static void setUp() {
        neow3jExpress = Neow3jExpress.build(new HttpService(getNodeUrl(neoTestContainer)));
//        waitUntilOpenWalletHasBalanceGreaterThanOrEqualTo("1", NEO_HASH, getNeow3jExpress());
    }

    private static Neow3jExpress getNeow3jExpress() {
        return neow3jExpress;
    }

    @Test
    public void test() throws IOException {
        PopulatedBlocks populatedBlocks = getNeow3jExpress()
                .expressGetPopulatedBlocks()
                .send()
                .getPopulatedBlocks();

        assertNotNull(populatedBlocks);
    }

}
