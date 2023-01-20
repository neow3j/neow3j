package io.neow3j.neofs;

import io.neow3j.neofs.sdk.NeoFSClient;
import io.neow3j.neofs.sdk.dto.EndpointResponse;
import io.neow3j.wallet.Account;
import neo.fs.v2.netmap.Types;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static io.neow3j.neofs.NeoFSIntegrationTestHelper.neofsEndpoint;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled // Remove for manual testing and once productive
public class NetmapIntegrationTest {

    private static final Account account = Account.create();
    private static NeoFSClient neofsClient;

    @BeforeAll
    public static void setUp() throws Throwable {
        neofsClient = NeoFSClient.loadAndInitialize(account, neofsEndpoint);
    }

    @AfterAll
    public static void after() {
        neofsClient.deleteClient();
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
