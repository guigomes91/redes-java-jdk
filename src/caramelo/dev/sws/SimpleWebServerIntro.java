package caramelo.dev.sws;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.SimpleFileServer;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Executors;

public class SimpleWebServerIntro {

    private static final int PORT = 9090;
    private static final Path DIRECTORY_TO_SERVE = Path.of("./docs").toAbsolutePath(); // Caminho absoluto para o diretório

    public static void main(String[] args) throws IOException {
        System.out.println("--- Criando o Simple Web Server (JDK 18+ / JEP 408) ---");

        if (!Files.exists(DIRECTORY_TO_SERVE)) {
            Files.createDirectories(DIRECTORY_TO_SERVE);
        }

        HttpHandler fileHandler = SimpleFileServer.createFileHandler(DIRECTORY_TO_SERVE);

        Path logFile = Path.of("swslog.txt");

        BufferedOutputStream logStream = new BufferedOutputStream(
                Files.newOutputStream(logFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)
        );

        Filter logFilter = SimpleFileServer.createOutputFilter(logStream, SimpleFileServer.OutputLevel.VERBOSE);

        // Cria a instância do servidor web de arquivos estáticos
        HttpServer server = HttpServer.create(
                new InetSocketAddress(PORT), // Endereço e porta de escuta
                10,
                "/booksCaramelo",
                fileHandler,
                logFilter
        );

        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

        // Inicializa o servidor web
        server.start();

        System.out.println("[OK] Simple Web Server iniciado com sucesso!");
        System.out.println(" • Acesse no navegador: http://localhost:" + PORT + "/");
        System.out.println(" • Servindo o diretório: " + DIRECTORY_TO_SERVE);
        System.out.println(" • Pressione Ctrl + C no console para encerrar.");
    }
}