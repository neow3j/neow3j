package io.neow3j.neofs;

import io.neow3j.helper.NeoFSAIOHelper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.ContainerState;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Optional;
import java.util.regex.Pattern;

import static io.neow3j.helper.NeoFSAIOHelper.DOCKER_COMPOSE_CROSS_FILENAME;
import static io.neow3j.helper.NeoFSAIOHelper.EXTEND_DOCKER_COMPOSE_FILENAME;

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
        File extendDockerCompose = prepareExtendDockerCompose(classLoader, tempDir);
        HashMap<String, String> envMap = prepareEnvVariables();

        container = new DockerComposeContainer(baseDockerCompose, extendDockerCompose).withEnv(envMap);
        container.start();

        prepareNeoFSAIO();
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

    private void prepareNeoFSAIO() throws IOException, InterruptedException {
        Optional<ContainerState> morphContainerOpt = container.getContainerByServiceName("morph");
        if (!morphContainerOpt.isPresent()) {
            throw new RuntimeException("Could not find container 'morph'");
        }
        ContainerState morphContainer = morphContainerOpt.get();
        String morphName = morphContainer.getContainerInfo().getName();


        Optional<ContainerState> extendContainerOpt = container.getContainerByServiceName("extend");
        if (!extendContainerOpt.isPresent()) {
            throw new RuntimeException("Could not find container 'extend'");
        }
        ContainerState extendContainer = extendContainerOpt.get();
        Container.ExecResult execResult = extendContainer.execInContainer("sh", "-c",
                "cd /neofs-aio && MORPH_CONTAINER_NAME=" + morphName.substring(1) + " make prepare.ir && " +
                        "MORPH_CONTAINER_NAME=" + morphName + " make tick.epoch");
        String stdout = execResult.getStdout();
        System.out.printf("stdout:'%s'%n", stdout);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        container.stop();
    }

    private File prepareBaseDockerCompose(File tempDir) throws IOException {
        return removeContainerNames(new File(tempDir, DOCKER_COMPOSE_CROSS_FILENAME));
    }

    private File prepareExtendDockerCompose(ClassLoader classLoader, File tempDir) throws IOException {
        String extendDockerCompose = classLoader.getResource(getTempDirPath(EXTEND_DOCKER_COMPOSE_FILENAME)).getPath();
        Path extendDockerComposePath = Paths.get(extendDockerCompose);

        String dockerfileExtendPath = getTempDirPath(NeoFSAIOHelper.EXTEND_DOCKERFILE_FILENAME);
        File dockerfileExtend = new File(classLoader.getResource(dockerfileExtendPath).getPath());

        Files.copy(dockerfileExtend.toPath(), new File(tempDir, NeoFSAIOHelper.EXTEND_DOCKERFILE_FILENAME).toPath());
        return Files.copy(extendDockerComposePath, new File(tempDir, EXTEND_DOCKER_COMPOSE_FILENAME).toPath()).toFile();
    }

    // need for container startup
    private HashMap<String, String> prepareEnvVariables() {
        HashMap<String, String> envMap = new HashMap<>(); // todo: read from .env
        envMap.put("NEOGO_VERSION", "0.99.4");
        envMap.put("AIO_VERSION", "0.34.0");
        envMap.put("HTTPGW_VERSION", "0.25.0");
        envMap.put("RESTGW_VERSION", "0.5.0");
        envMap.put("DOCKER_SOCKET", DockerClientFactory.instance().getRemoteDockerUnixSocketPath());
        return envMap;
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
