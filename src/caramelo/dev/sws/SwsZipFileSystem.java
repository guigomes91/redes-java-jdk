package caramelo.dev.sws;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.SimpleFileServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SwsZipFileSystem {

    private static final int PORT = 9009;

    public static void main(String[] args) throws IOException {
        System.out.println("--- Criando o SWS sobre um Zip File System ---");

        Path zipRootPath = prepararZipFileSystem();

        HttpServer server = SimpleFileServer.createFileServer(
                new InetSocketAddress(PORT),
                zipRootPath,
                SimpleFileServer.OutputLevel.VERBOSE
        );

        server.start();

        System.out.println("[OK] Simple Web Server para o arquivo ZIP iniciado com sucesso!");
        System.out.println(" • Acesse no navegador: http://localhost:" + PORT + "/");
        System.out.println(" • Arquivo compactado disponível em: http://localhost:" + PORT + "/bookszipped.txt");
    }

    private static Path prepararZipFileSystem() throws IOException {
        Path zipsDir = Path.of("./zips").toAbsolutePath();
        Path docsDir = Path.of("./docs").toAbsolutePath();

        if (!Files.exists(zipsDir)) {
            Files.createDirectories(zipsDir);
        }
        if (!Files.exists(docsDir)) {
            Files.createDirectories(docsDir);
        }

        Path externalTxtFile = docsDir.resolve("books.txt");
        Files.write(externalTxtFile,
                List.of(
                        "Java Coding Problems 1st Edition",
                        "Java Coding Problems 2nd Edition",
                        "jOOQ Masterclass",
                        "The Complete Coding Interview Guide in Java"),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );

        Path zipFile = zipsDir.resolve("docs.zip");
        Files.deleteIfExists(zipFile);

        Map<String, String> env = new HashMap<>();
        env.put("create", "true");

        try (FileSystem zipFs = FileSystems.newFileSystem(zipFile, env)) {
            Files.copy(externalTxtFile, zipFs.getPath("/bookszipped.txt"), StandardCopyOption.REPLACE_EXISTING);
            Files.writeString(zipFs.getPath("/index.html"), """
                    <!DOCTYPE html>
                    <html>
                    <head><meta charset="utf-8"><title>docs.zip</title></head>
                    <body>
                    <h1>Conteúdo de docs.zip</h1>
                    <ul>
                    <li><a href="bookszipped.txt">bookszipped.txt</a></li>
                    </ul>
                    </body>
                    </html>
                    """, StandardCharsets.UTF_8);
        }

        FileSystem servedZipFs = FileSystems.newFileSystem(zipFile);
        return servedZipFs.getPath("/");
    }
}