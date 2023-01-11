package io.neow3j.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class NeoFSAIOHelper {

//    public static String NEOFS_AIO_REPO = "https://github.com/nspcc-dev/neofs-aio.git";
    public static String NEOFS_AIO_REPO = "https://github.com/AxLabs/neofs-aio.git";

    public static String DOCKER_COMPOSE_CROSS_FILENAME = "docker-compose.cross.yml";
    public static String BASE_DOCKER_COMPOSE_FILENAME = "docker-compose.base.cross.yml";

    public static String EXTEND_DOCKERFILE_FILENAME = "extend.Dockerfile";
    public static String EXTEND_DOCKER_COMPOSE_FILENAME = "docker-compose.extend.yml";

    public static String NEOFS_RESOURCE_DIR = "neofs";

    public static String TMP_DIR_PREFIX = "neofs-aio";
    public static File NEOFS_AIO_TMP_DIR_PATH;

    static {
        try {
            NEOFS_AIO_TMP_DIR_PATH = Files.createTempDirectory(TMP_DIR_PREFIX).toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
