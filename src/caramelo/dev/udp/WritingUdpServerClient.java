package caramelo.dev.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;

public class WritingUdpServerClient {

    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 4444;

    // Tamanho máximo da carga útil de um datagrama UDP sobre IPv4
    private static final int MAX_UDP_PACKET_SIZE = 65507;

    // Quantidade de datagramas que o servidor vai atender (um por cliente de teste)
    private static final int TOTAL_REQUESTS = 2;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- Iniciando Aplicação UDP Server/Client (Java NIO) ---");

        // 1. Sobe o servidor UDP em uma thread dedicada
        Thread serverThread = new Thread(WritingUdpServerClient::iniciarServidorUdp);
        serverThread.start();

        // Dá tempo para o servidor fazer o bind na porta
        Thread.sleep(1000);

        // 2. Cliente UDP sem conexão (connectionless)
        iniciarClienteUdpSemConexao();

        // 3. Cliente UDP com conexão (connected)
        iniciarClienteUdpComConexao();

        serverThread.join();
    }

    /**
     * Servidor UDP Echo: recebe datagramas e devolve a mensagem para o remetente.
     */
    private static void iniciarServidorUdp() {
        System.out.println("[SERVIDOR UDP] Abrindo DatagramChannel para IPv4...");

        try (DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET)) {
            // Ajusta os buffers de recepção e envio do socket
            channel.setOption(StandardSocketOptions.SO_RCVBUF, 4 * 1024);
            channel.setOption(StandardSocketOptions.SO_SNDBUF, 4 * 1024);

            channel.bind(new InetSocketAddress(SERVER_IP, SERVER_PORT));
            System.out.println("[SERVIDOR UDP] Servidor pronto no endereço: " + channel.getLocalAddress());

            ByteBuffer buffer = ByteBuffer.allocateDirect(MAX_UDP_PACKET_SIZE);

            for (int i = 0; i < TOTAL_REQUESTS; i++) {
                buffer.clear();

                // receive() bloqueia até chegar um pacote e devolve o endereço do cliente
                SocketAddress clientAddress = channel.receive(buffer);
                buffer.flip();

                int tamanho = buffer.limit();
                String mensagem = StandardCharsets.UTF_8.decode(buffer).toString();
                System.out.println("[SERVIDOR UDP] Recebido (" + tamanho + " bytes) de "
                        + clientAddress + ": '" + mensagem + "'");

                // Echo: responde para quem enviou
                ByteBuffer resposta = ByteBuffer.wrap(("ECHO_UDP: " + mensagem).getBytes(StandardCharsets.UTF_8));
                channel.send(resposta, clientAddress);
            }

            System.out.println("[SERVIDOR UDP] Atendimento concluído. Encerrando servidor.");
        } catch (IOException e) {
            System.err.println("[SERVIDOR UDP] Erro: " + e.getMessage());
        }
    }

    /**
     * Cliente UDP sem conexão: informa o destino a cada send() e usa receive().
     */
    private static void iniciarClienteUdpSemConexao() {
        System.out.println("\n[CLIENTE UDP - SEM CONEXÃO] Enviando datagrama...");

        try (DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET)) {
            InetSocketAddress serverAddress = new InetSocketAddress(SERVER_IP, SERVER_PORT);

            String mensagem = "Olá via UDP Sem Conexão!";
            ByteBuffer sendBuffer = ByteBuffer.wrap(mensagem.getBytes(StandardCharsets.UTF_8));

            int bytesEnviados = channel.send(sendBuffer, serverAddress);
            System.out.println("[CLIENTE UDP - SEM CONEXÃO] Enviados " + bytesEnviados + " bytes para " + serverAddress);

            ByteBuffer receiveBuffer = ByteBuffer.allocateDirect(MAX_UDP_PACKET_SIZE);
            channel.receive(receiveBuffer);
            receiveBuffer.flip();

            String resposta = StandardCharsets.UTF_8.decode(receiveBuffer).toString();
            System.out.println("[CLIENTE UDP - SEM CONEXÃO] Resposta do servidor: '" + resposta + "'");
        } catch (IOException e) {
            System.err.println("[CLIENTE UDP - SEM CONEXÃO] Erro: " + e.getMessage());
        }
    }

    /**
     * Cliente UDP com conexão: connect() fixa o endereço remoto e libera write()/read().
     */
    private static void iniciarClienteUdpComConexao() {
        System.out.println("\n[CLIENTE UDP - COM CONEXÃO] Conectando socket UDP ao endereço remoto...");

        try (DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET)) {
            InetSocketAddress serverAddress = new InetSocketAddress(SERVER_IP, SERVER_PORT);

            // Não há handshake na rede: connect() só filtra de/para quem o canal envia e recebe
            channel.connect(serverAddress);
            System.out.println("[CLIENTE UDP - COM CONEXÃO] Canal vinculado exclusivamente a " + serverAddress);

            String mensagem = "Olá via UDP Conectado!";
            ByteBuffer sendBuffer = ByteBuffer.wrap(mensagem.getBytes(StandardCharsets.UTF_8));

            int bytesEscritos = channel.write(sendBuffer);
            System.out.println("[CLIENTE UDP - COM CONEXÃO] Escritos " + bytesEscritos + " bytes via write().");

            ByteBuffer receiveBuffer = ByteBuffer.allocateDirect(MAX_UDP_PACKET_SIZE);
            channel.read(receiveBuffer);
            receiveBuffer.flip();

            String resposta = StandardCharsets.UTF_8.decode(receiveBuffer).toString();
            System.out.println("[CLIENTE UDP - COM CONEXÃO] Resposta recebida via read(): '" + resposta + "'");
        } catch (IOException e) {
            System.err.println("[CLIENTE UDP - COM CONEXÃO] Erro: " + e.getMessage());
        }
    }
}