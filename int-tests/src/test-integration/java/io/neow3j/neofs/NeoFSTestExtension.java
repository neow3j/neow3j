package io.neow3j.neofs;

import com.github.dockerjava.api.model.ContainerNetwork;
import io.neow3j.helper.NeoFSAIOHelper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.ContainerState;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import static io.neow3j.helper.NeoFSAIOHelper.DOCKER_COMPOSE_CROSS_FILENAME;

public class NeoFSTestExtension implements AfterAllCallback, BeforeAllCallback {

    private DockerComposeContainer container;
    private String grpcEndpoint;

    public NeoFSTestExtension() {
    }

    @Override
    public void beforeAll(ExtensionContext context) throws IOException, GitAPIException, InterruptedException {
        File tempDir = NeoFSAIOHelper.NEOFS_AIO_TMP_DIR_PATH;

        Git.cloneRepository()
                .setURI(NeoFSAIOHelper.NEOFS_AIO_REPO)
                .setDirectory(tempDir)
                .call();

        ClassLoader classLoader = getClass().getClassLoader();
        File baseDockerCompose = prepareBaseDockerCompose(tempDir);
        copyExtendDockerFiles(classLoader, tempDir);

        Map<String, String> envMap = prepareEnvVariables();

        container = new DockerComposeContainer(baseDockerCompose).withEnv(envMap);
        container.start();

        // Retrieve the morph container
        Optional<ContainerState> morphContainerOpt = container.getContainerByServiceName("morph");
        if (!morphContainerOpt.isPresent()) {
            throw new RuntimeException("Could not find container 'morph'");
        }
        ContainerState morphContainer = morphContainerOpt.get();
        String morphContainerName = morphContainer.getContainerInfo().getName();

        // Get the first network allocated to the morph container
        ContainerNetwork morphNetwork = morphContainer
                .getContainerInfo()
                .getNetworkSettings()
                .getNetworks()
                .entrySet()
                .iterator()
                .next()
                .getValue();

        // TODO: fail if `morphNetwork` not present.

        // Build a network based on an existing network ID.
        // This way it becomes possible to tell docker to instantiate a
        // container attaching to an existing network.
        Network net = new Network() {

            @NotNull
            @Override
            public Statement apply(@NotNull Statement statement, @NotNull Description description) {
                return statement;
            }

            @Override
            public String getId() {
                return morphNetwork.getNetworkID();
            }

            @Override
            public void close() {

            }
        };

        HashMap<String, String> envMapExtend = new HashMap<>();
        envMapExtend.put("NEOGO_HOST", "http://morph:30333");
        envMapExtend.put("MORPH_CONTAINER_NAME", morphContainerName.substring(1));

        GenericContainer extendContainer = new GenericContainer<>(
                // Build the "extend" docker image
                new ImageFromDockerfile().withDockerfile(
                        tempDir.toPath().resolve(NeoFSAIOHelper.EXTEND_DOCKERFILE_FILENAME)))
                // Set the network to the same one as the docker-compose
                .withNetwork(net)
                // Set env vars important for the `make`
                .withEnv(envMapExtend)
                .withPrivilegedMode(true)
                // Map the temp dir to the container filesystem
                .withFileSystemBind(
                        tempDir.getAbsolutePath(), "/neofs-aio", BindMode.READ_WRITE
                )
                // Map the docker unix socket from host to the docker CLI
                .withFileSystemBind(
                        getRemoteDockerUnixSocketPath(), "/var/run/docker.sock", BindMode.READ_WRITE
                )
                // The commands to be executed once the docker image is created.
                // This is where the magic happens :-)
                // The image will start and "die"/exit right after.
                .withCommand("sh", "-c",
                        "getent hosts sn | awk '{ print $1 }' | cat && " +
                                "getent hosts morph | awk '{ print $1 }' | cat && " +
                                "cd /neofs-aio && " +
                                "make prepare.ir && " +
                                "make tick.epoch"
                )
                // The command modifier is important to set a pseudo TTY
                .withCreateContainerCmdModifier(command -> command.withTty(Boolean.TRUE));

        // Starting the container...
        extendContainer.start();

        // TODO: this was used for debugging. It can be safely removed.
        String log = extendContainer.getLogs(OutputFrame.OutputType.STDOUT, OutputFrame.OutputType.STDERR,
                OutputFrame.OutputType.END);

        setGRPC();
    }

    private void setGRPC() {
        Optional<ContainerState> neofsAIOContainerOpt = container.getContainerByServiceName("sn");
        if (!neofsAIOContainerOpt.isPresent()) {
            throw new RuntimeException("Could not find container 'sn'");
        }
        ContainerState neofsAIOContainer = neofsAIOContainerOpt.get();
        grpcEndpoint = "grpc://" + neofsAIOContainer.getHost() + ":8080";
    }

    @Override
    public void afterAll(ExtensionContext context) {
        container.stop();
    }

    private File prepareBaseDockerCompose(File tempDir) throws IOException {
        return removeContainerNames(new File(tempDir, DOCKER_COMPOSE_CROSS_FILENAME));
    }

    private void copyExtendDockerFiles(ClassLoader classLoader, File tempDir) throws IOException {
        String dockerfileExtendPath = getTempDirPath(NeoFSAIOHelper.EXTEND_DOCKERFILE_FILENAME);
        File dockerfileExtend = new File(
                Objects.requireNonNull(classLoader.getResource(dockerfileExtendPath)).getPath());
        Files.copy(dockerfileExtend.toPath(), new File(tempDir, NeoFSAIOHelper.EXTEND_DOCKERFILE_FILENAME).toPath());
    }

    // need for container startup
    private HashMap<String, String> prepareEnvVariables() {
        HashMap<String, String> envMap = new HashMap<>(); // todo: read from .env
        envMap.put("NEOGO_VERSION", "0.99.4");
        envMap.put("AIO_VERSION", "0.34.0");
        envMap.put("HTTPGW_VERSION", "0.25.0");
        envMap.put("RESTGW_VERSION", "0.5.0");
        envMap.put("DOCKER_SOCKET", getRemoteDockerUnixSocketPath());
        return envMap;
    }

    private String getRemoteDockerUnixSocketPath() {
        return DockerClientFactory.instance().getRemoteDockerUnixSocketPath();
    }

    private File removeContainerNames(File inputFile) throws IOException {
        String outputFilename = NeoFSAIOHelper.BASE_DOCKER_COMPOSE_FILENAME;
        File tempFile = new File(NeoFSAIOHelper.NEOFS_AIO_TMP_DIR_PATH, outputFilename);

        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

        String currentLine;

        while ((currentLine = reader.readLine()) != null) {
            boolean matches = Pattern.matches("^\u0020+container_name:.*", currentLine);
            if (matches) {
                continue;
            }
            writer.write(currentLine);
            writer.newLine();
        }
        writer.close();
        reader.close();
        return tempFile;
    }

    private String getTempDirPath(String filename) {
        return Paths.get(NeoFSAIOHelper.NEOFS_RESOURCE_DIR, filename).toString();
    }

    public String getNeofsEndpoint() {
        return grpcEndpoint;
    }

}
