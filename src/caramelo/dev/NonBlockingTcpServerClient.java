package caramelo.dev;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class NonBlockingTcpServerClient {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 4444;
    private static final int MAX_IDLE_LOOPS = 10;

    public static void main(String[] args) throws InterruptedException {
        Thread serverThread = new Thread(NonBlockingTcpServerClient::executarServidor);
        serverThread.start();

        // Dá tempo do servidor fazer o bind antes do cliente conectar
        Thread.sleep(1000);

        executarCliente();
        serverThread.join();
    }

    private static void executarServidor() {
        try (Selector selector = Selector.open();
             ServerSocketChannel serverChannel = ServerSocketChannel.open()) {

            serverChannel.configureBlocking(false);
            serverChannel.setOption(StandardSocketOptions.SO_RCVBUF, 256 * 1024);
            serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            serverChannel.bind(new InetSocketAddress(HOST, PORT));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("[SERVIDOR] Aguardando clientes na porta " + PORT);

            int idleLoops = 0;
            while (idleLoops < MAX_IDLE_LOOPS) {
                if (selector.select(2000) == 0) {
                    idleLoops++;
                    continue;
                }

                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isAcceptable()) {
                        aceitar(key, selector);
                    } else if (key.isReadable()) {
                        ecoar(key);
                    }
                }
            }

            System.out.println("[SERVIDOR] Encerrado por inatividade");
        } catch (IOException e) {
            System.err.println("[SERVIDOR] Erro: " + e.getMessage());
        }
    }

    private static void aceitar(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel client = server.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
        System.out.println("[SERVIDOR] Conexão aceita de " + client.getRemoteAddress());
    }

    private static void ecoar(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        buffer.clear();

        if (client.read(buffer) == -1) {
            System.out.println("[SERVIDOR] Cliente desconectou");
            key.cancel();
            client.close();
            return;
        }

        buffer.flip();
        String mensagem = StandardCharsets.UTF_8.decode(buffer.duplicate()).toString();
        System.out.println("[SERVIDOR] Recebido: " + mensagem);

        while (buffer.hasRemaining()) {
            client.write(buffer);
        }
    }

    private static void executarCliente() {
        try (Selector selector = Selector.open();
             SocketChannel channel = SocketChannel.open()) {

            channel.configureBlocking(false);
            channel.setOption(StandardSocketOptions.SO_RCVBUF, 128 * 1024);
            channel.setOption(StandardSocketOptions.SO_SNDBUF, 128 * 1024);
            channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);

            // Em loopback o connect pode concluir na hora, sem disparar OP_CONNECT
            if (channel.connect(new InetSocketAddress(HOST, PORT))) {
                enviar(channel);
                channel.register(selector, SelectionKey.OP_READ);
            } else {
                channel.register(selector, SelectionKey.OP_CONNECT);
            }

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            boolean concluido = false;

            while (!concluido) {
                if (selector.select(1000) == 0) {
                    continue;
                }

                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (key.isConnectable()) {
                        channel.finishConnect();
                        enviar(channel);
                        key.interestOps(SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        buffer.clear();
                        if (channel.read(buffer) > 0) {
                            buffer.flip();
                            System.out.println("[CLIENTE] Eco recebido: " + StandardCharsets.UTF_8.decode(buffer));
                            concluido = true;
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[CLIENTE] Erro: " + e.getMessage());
        }
    }

    private static void enviar(SocketChannel channel) throws IOException {
        System.out.println("[CLIENTE] Conectado, enviando mensagem");
        ByteBuffer mensagem = StandardCharsets.UTF_8.encode("Olá do cliente NIO!");
        while (mensagem.hasRemaining()) {
            channel.write(mensagem);
        }
    }
}