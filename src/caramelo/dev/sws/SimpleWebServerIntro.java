package caramelo.dev.sws;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.SimpleFileServer;

import java.net.InetSocketAddress;
import java.nio.file.Path;

public class SimpleWebServerIntro {

    private static final int PORT = 8080;
    private static final Path DIRECTORY_TO_SERVE = Path.of("./docs").toAbsolutePath(); // Caminho absoluto para o diretório

    public static void main(String[] args) {
        System.out.println("--- Criando o Simple Web Server (JDK 18+ / JEP 408) ---");

        // 1 - Cria a instância do servidor web de arquivos estáticos
        HttpServer server = SimpleFileServer.createFileServer(
                new InetSocketAddress(PORT), // Endereço e porta de escuta
                DIRECTORY_TO_SERVE, // Diretório raiz a ser servido
                SimpleFileServer.OutputLevel.VERBOSE // Nível de log no console (VERBOSE)
        );

        // 2 - Inicializa o servidor web
        server.start();

        System.out.println("[OK] Simple Web Server iniciado com sucesso!");
        System.out.println(" • Acesse no navegador: http://localhost:" + PORT + "/");
        System.out.println(" • Servindo o diretório: " + DIRECTORY_TO_SERVE);
        System.out.println(" • Pressione Ctrl + C no console para encerrar.");
    }
}