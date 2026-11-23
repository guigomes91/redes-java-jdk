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
        Path inMemoryPath = criarDiretorioEmMemoria();

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

    private static Path criarDiretorioEmMemoria() throws IOException {
        // O FileSystem não é fechado de propósito: ele precisa viver enquanto o servidor estiver no ar
        FileSystem fileSystem = Jimfs.newFileSystem(Configuration.forCurrentPlatform());

        Path docs = Files.createDirectory(fileSystem.getPath("docs"));

        Files.write(docs.resolve("linguagens.txt"), List.of(
                "Java",
                "Kotlin",
                "Scala",
                "Groovy"
        ), StandardCharsets.UTF_8);

        // O SimpleFileServer exige um caminho absoluto
        return docs.toAbsolutePath();
    }
}