package caramelo.dev;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class SocketBasicsIntro {

    public static void main(String[] args) {
        System.out.println("--- Conceitos Básicos de Rede e Sockets em Java ---");
        try {
            // 1. Identificando o IP do Localhost (127.0.0.1)
            InetAddress localHost = InetAddress.getLocalHost();
            System.out.println("Host Local Name: " + localHost.getHostName());
            System.out.println("Host Local IP: " + localHost.getHostAddress());

            // 2. Mapeando o IP especial de Loopback (127.0.0.1)
            InetAddress loopback = InetAddress.getByName("127.0.0.1");
            System.out.println("É um endereço de Loopback/Localhost? " + loopback.isLoopbackAddress());

            // 3. Criando um Endpoint de Socket (IP + Porta)
            int serverPort = 8080; // Porta no intervalo válidos de 0 a 65535
            InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", serverPort);

            System.out.println("\n--- Coordenadas do Endpoint ---");
            System.out.println("Host Name: " + socketAddress.getHostName());
            System.out.println("Porta: " + socketAddress.getPort());
            System.out.println("String do Endpoint: " + socketAddress.toString());
        } catch (UnknownHostException e) {
            System.err.println("Erro ao resolver o endereço IP: " + e.getMessage());
        }
    }
}