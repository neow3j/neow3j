package io.neow3j.test;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

public class NeoExpressTestContainer extends GenericContainer<NeoExpressTestContainer> {

    static final String DEFAULT_NEO_EXPRESS_SOURCE = "/default.neo-express";
    static final String DEFAULT_NEO_EXPRESS_DESTINATION = "/app/default.neo-express";

    // This is the port of neo-express node which is exposed by the container.
    static final int EXPOSED_JSONRPC_PORT = 40332;

    public NeoExpressTestContainer(int secondsPerBlock, String... resources) {
        super(DockerImageName.parse(TestProperties.neo3ExpressPrivateNetContainerImg()));
        withCopyFileToContainer(MountableFile.forClasspathResource(DEFAULT_NEO_EXPRESS_SOURCE, 777),
                DEFAULT_NEO_EXPRESS_DESTINATION);
        withExposedPorts(EXPOSED_JSONRPC_PORT);

        int i = 0;
        while (resources != null && i+1 < resources.length) {
            String src = resources[i++];
            String dest = resources[i++];
            withClasspathResourceMapping(src, dest, BindMode.READ_ONLY);
        }
        if (secondsPerBlock != 0) {
            withCommand("--seconds-per-block " + secondsPerBlock);
        }
        waitingFor(Wait.forListeningPort());
    }

    public String getNodeUrl() {
        return "http://" + this.getContainerIpAddress() + ":" +
                this.getMappedPort(EXPOSED_JSONRPC_PORT);
    }
}