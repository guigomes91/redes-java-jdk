package caramelo.dev;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class TcpServers {

    public static void main(String[] args) {
        Thread.ofPlatform().start(TcpServers::bloqueante);
        Thread.ofPlatform().start(TcpServers::naoBloqueante);
    }

    // Bloqueante: cada cliente segura uma thread
    static void bloqueante() {
        try (ServerSocketChannel server = ServerSocketChannel.open()) {
            server.bind(new InetSocketAddress(8080));
            System.out.println("[bloqueante] ouvindo na 8080");

            while (true) {
                SocketChannel client = server.accept(); // fica parado aqui até alguém conectar
                Thread.ofPlatform().start(() -> eco(client));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void eco(SocketChannel client) {
        try (client) {
            System.out.println("[bloqueante] " + Thread.currentThread() + " atendendo " + client.getRemoteAddress());
            ByteBuffer buffer = ByteBuffer.allocate(256);
            while (client.read(buffer) != -1) { // fica parado aqui até o cliente mandar algo
                buffer.flip();
                client.write(buffer);
                buffer.clear();
            }
        } catch (IOException e) {
            System.out.println("[bloqueante] conexão encerrada");
        }
    }

    // Não bloqueante: uma thread, um Selector e muitas conexões
    static void naoBloqueante() {
        try (Selector selector = Selector.open();
             ServerSocketChannel server = ServerSocketChannel.open()) {

            server.bind(new InetSocketAddress(8081));
            server.configureBlocking(false);
            server.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("[nio] ouvindo na 8081");

            ByteBuffer buffer = ByteBuffer.allocate(256);
            while (true) {
                selector.select(); // acorda só quando algum socket precisa de atenção

                for (SelectionKey key : selector.selectedKeys()) {
                    if (key.isAcceptable()) {
                        SocketChannel client = server.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                        System.out.println("[nio] " + Thread.currentThread() + " aceitou " + client.getRemoteAddress());
                    } else if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        if (client.read(buffer) == -1) {
                            client.close();
                        } else {
                            buffer.flip();
                            client.write(buffer);
                        }
                        buffer.clear();
                    }
                }
                selector.selectedKeys().clear();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
