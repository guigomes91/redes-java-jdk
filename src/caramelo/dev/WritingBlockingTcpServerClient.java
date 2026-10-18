package caramelo.dev;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Servidor/cliente TCP em modo bloqueante usando SocketChannel.
 *
 * Protocolo: uma requisição por conexão. O cliente envia a mensagem e fecha
 * o lado de escrita (shutdownOutput); o servidor lê até o EOF, responde e
 * fecha a conexão; o cliente lê a resposta até o EOF.
 */
public class WritingBlockingTcpServerClient {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 5555;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- Iniciando Aplicação TCP Server/Client Bloqueante ---");

        // Substitui o Thread.sleep(1000): o cliente só conecta depois do bind
        CountDownLatch servidorPronto = new CountDownLatch(1);

        Thread serverThread = Thread.ofPlatform()
                .name("servidor-tcp")
                .start(() -> iniciarServidor(servidorPronto));

        if (!servidorPronto.await(5, TimeUnit.SECONDS)) {
            System.err.println("[MAIN] Servidor não ficou pronto a tempo. Abortando.");
            serverThread.interrupt(); // canais NIO são interrompíveis: o accept() é desbloqueado
            return;
        }

        iniciarCliente();

        serverThread.join();
        System.out.println("--- Fim ---");
    }

    private static void iniciarServidor(CountDownLatch servidorPronto) {
        try (ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
            serverChannel.configureBlocking(true);
            serverChannel.setOption(StandardSocketOptions.SO_RCVBUF, 64 * 1024);
            serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            serverChannel.bind(new InetSocketAddress(HOST, PORT));

            System.out.println("[SERVIDOR] Aguardando conexões em " + serverChannel.getLocalAddress());
            servidorPronto.countDown();

            try (SocketChannel clientChannel = serverChannel.accept()) {
                System.out.println("[SERVIDOR] Cliente conectado de: " + clientChannel.getRemoteAddress());

                String mensagem = lerAteFimDoStream(clientChannel);
                System.out.println("[SERVIDOR] Mensagem recebida: '" + mensagem + "'");

                String resposta = "PROCESSADO_COM_SUCESSO: " + mensagem.toUpperCase(Locale.ROOT);
                escreverTudo(clientChannel, resposta);
                System.out.println("[SERVIDOR] Resposta enviada.");
            }
        } catch (IOException e) {
            System.err.println("[SERVIDOR] Erro na execução: " + e);
        }
    }

    private static void iniciarCliente() {
        try (SocketChannel clientChannel = SocketChannel.open()) {
            clientChannel.configureBlocking(true);
            // Em modo bloqueante, connect() retorna conectado ou lança exceção
            clientChannel.connect(new InetSocketAddress(HOST, PORT));
            System.out.println("[CLIENTE] Conectado a " + clientChannel.getRemoteAddress());

            escreverTudo(clientChannel, "Requisição do cliente via TCP NIO");
            clientChannel.shutdownOutput(); // envia EOF: avisa o servidor que a mensagem acabou
            System.out.println("[CLIENTE] Mensagem enviada.");

            String resposta = lerAteFimDoStream(clientChannel);
            System.out.println("[CLIENTE] Resposta recebida: '" + resposta + "'");
        } catch (IOException e) {
            System.err.println("[CLIENTE] Erro na execução: " + e);
        }
    }

    /** write() pode escrever só parte do buffer; repete até esvaziar. */
    private static void escreverTudo(SocketChannel channel, String texto) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(texto.getBytes(StandardCharsets.UTF_8));
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    /**
     * TCP é stream, não mensagem: um read() pode trazer só um pedaço.
     * Acumula os bytes até o EOF e só então decodifica, para não quebrar
     * caracteres UTF-8 de vários bytes no meio.
     */
    private static String lerAteFimDoStream(SocketChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        ByteArrayOutputStream acumulado = new ByteArrayOutputStream();
        while (channel.read(buffer) != -1) {
            buffer.flip();
            acumulado.write(buffer.array(), buffer.position(), buffer.remaining());
            buffer.clear();
        }
        return acumulado.toString(StandardCharsets.UTF_8);
    }
}