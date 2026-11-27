package caramelo.dev.sws;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.SimpleFileServer;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class SwsJavaRuntimeDirectory {

    private static final int PORT = 9009;

    public static void main(String[] args) {
        Path jrtModulesPath = obterCaminhoJavaRuntime();

        HttpServer server = SimpleFileServer.createFileServer(
                new InetSocketAddress(PORT),
                jrtModulesPath,
                SimpleFileServer.OutputLevel.VERBOSE
        );

        server.start();

        System.out.println("Simple Web Server do Java Runtime iniciado na porta " + PORT);
        System.out.println("Módulos: http://localhost:" + PORT + "/");
        System.out.println("Exemplo: http://localhost:" + PORT + "/java.base/java/io/File.class");
    }

    // Diretório 'modules' do sistema de arquivos jrt:/ (JEP 220)
    private static Path obterCaminhoJavaRuntime() {
        FileSystem jrtfs = FileSystems.getFileSystem(URI.create("jrt:/"));
        return jrtfs.getPath("modules").toAbsolutePath();
    }
}