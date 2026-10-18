package caramelo.dev;

import java.io.IOException;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.nio.channels.NetworkChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Comparator;

/**
 * Introdução à Java Socket API (NIO.2) e inspeção/configuração
 * de StandardSocketOptions em ServerSocketChannel e SocketChannel.
 */
public class SocketApiOptionsIntro {

    public static void main(String[] args) {
        System.out.println("--- Inspecionando e Configurando a Java Socket API (NIO.2) ---");

        // open() devolve um canal aberto ou lança IOException; não precisa checar isOpen()
        try (ServerSocketChannel serverChannel = ServerSocketChannel.open();
             SocketChannel clientChannel = SocketChannel.open()) {

            imprimirOpcoes("ServerSocketChannel", serverChannel);
            System.out.println("\n--- Configurando ServerSocketChannel ---");
            configurar(serverChannel, StandardSocketOptions.SO_RCVBUF, 64 * 1024);
            configurar(serverChannel, StandardSocketOptions.SO_REUSEADDR, true);
            configurar(serverChannel, StandardSocketOptions.SO_KEEPALIVE, true); // não suportada aqui

            imprimirOpcoes("SocketChannel", clientChannel);
            System.out.println("\n--- Configurando SocketChannel ---");
            configurar(clientChannel, StandardSocketOptions.SO_SNDBUF, 32 * 1024);
            configurar(clientChannel, StandardSocketOptions.SO_RCVBUF, 32 * 1024);
            configurar(clientChannel, StandardSocketOptions.SO_KEEPALIVE, true);
            configurar(clientChannel, StandardSocketOptions.TCP_NODELAY, true);
            configurar(clientChannel, StandardSocketOptions.SO_LINGER, 5);

        } catch (IOException e) {
            System.err.println("[ERRO] " + e);
        }
    }

    /** Lista as opções suportadas pelo canal, em ordem alfabética, com o valor atual. */
    private static void imprimirOpcoes(String nomeCanal, NetworkChannel channel) throws IOException {
        System.out.println("\n--- Opções suportadas por " + nomeCanal + " ---");
        var opcoes = channel.supportedOptions().stream()
                .sorted(Comparator.comparing(SocketOption::name))
                .toList();
        for (SocketOption<?> opcao : opcoes) {
            System.out.printf("  %-20s = %s%n", opcao.name(), channel.getOption(opcao));
        }
    }

    /**
     * Define a opção só se o canal a suportar (senão setOption lança
     * UnsupportedOperationException) e relê o valor: tamanhos de buffer são
     * apenas uma dica, e o sistema operacional pode ajustá-los.
     */
    private static <T> void configurar(NetworkChannel channel, SocketOption<T> opcao, T valor)
            throws IOException {
        if (!channel.supportedOptions().contains(opcao)) {
            System.out.printf("  [PULADA] %-20s não é suportada por este canal%n", opcao.name());
            return;
        }
        T antes = channel.getOption(opcao);
        channel.setOption(opcao, valor);
        T depois = channel.getOption(opcao);
        System.out.printf("  [OK] %-20s %s -> pedido %s, efetivo %s%n",
                opcao.name(), antes, valor, depois);
    }
}