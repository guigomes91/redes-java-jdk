package caramelo.dev.sws;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.SimpleFileServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Simple Web Server (JDK 18+) servindo arquivos de um sistema de arquivos em memória (Jimfs).
 */
public class SwsInMemoryFileSystem {

    private static final int PORT = 9009;

    public static void main(String[] args) throws IOException {
        Path inMemoryPath = criarBucketEmMemoria();

        HttpServer server = SimpleFileServer.createFileServer(
                new InetSocketAddress(PORT),
                inMemoryPath,
                SimpleFileServer.OutputLevel.VERBOSE
        );

        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(0)));
        server.start();

        System.out.println("Simple Web Server em memória iniciado");
        System.out.println(" • http://localhost:" + PORT + "/");
        System.out.println(" • http://localhost:" + PORT + "/linguagens.txt");
        System.out.println(" • Caminho virtual: " + inMemoryPath);
    }

    private static Path criarBucketEmMemoria() throws IOException {
        FileSystem fileSystem = Jimfs.newFileSystem(Configuration.forCurrentPlatform());
        Path bucket = Files.createDirectory(fileSystem.getPath("meu-bucket"));

        Path precos = Files.createDirectories(bucket.resolve("precos"));
        Files.write(precos.resolve("2026-11-23.csv"), List.of(
                "sku;preco",
                "CAM-001;79.90",
                "CAL-002;149.90"
        ), StandardCharsets.UTF_8);

        Path catalogo = Files.createDirectories(bucket.resolve("catalogo"));
        Files.writeString(catalogo.resolve("produtos.json"),
                "[{\"sku\":\"CAM-001\",\"nome\":\"Camiseta básica\"}]");

        return bucket.toAbsolutePath();
    }
}