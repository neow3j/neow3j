package io.neow3j.neofs;

import io.neow3j.neofs.sdk.NeoFSClient;
import io.neow3j.neofs.sdk.dto.EndpointResponse;
import io.neow3j.wallet.Account;
import neo.fs.v2.netmap.Types;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled // Remove for manual testing and once productive
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NetmapIntegrationTest {

    private NeoFSClient neofsClient;

    @RegisterExtension
    public static NeoFSTestExtension ct = new NeoFSTestExtension();

    @BeforeAll
    public void setUp() throws Throwable {
        Account account = Account.fromWIF("KzAXTwrj1VxQA746zSSMCt9g3omSDfyKnwsayEducuHvKd1LR9mx");
        neofsClient = NeoFSClient.loadAndInitialize(account, ct.getNeofsEndpoint());
    }

    @Test
    public void testGetNetworkInfo() throws Exception {
        Types.NetworkInfo networkInfo = neofsClient.getNetworkInfo();
        assertThat(networkInfo.getCurrentEpoch(), is(greaterThan(0L)));
        assertThat(networkInfo.getMagicNumber(), is(15405L));
        assertThat(networkInfo.getMsPerBlock(), is(1000L));

        Types.NetworkConfig networkConfig = networkInfo.getNetworkConfig();
        assertThat(networkConfig.getParametersCount(), is(10));
    }

    @Test
    public void testGetEndpoint() throws Exception {
        EndpointResponse endpoint = neofsClient.getEndpoint();

        assertNotNull(endpoint.getNodeInfo());
        assertNotNull(endpoint.getVersion());
    }

}
