package caramelo.dev.udp;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.util.Collections;
import java.util.Optional;

/**
 * Introdução ao IP Multicasting e exploração do MembershipKey.
 */
public class IntroducingMulticasting {

    // Faixa de multicast IPv4 (Classe D): 224.0.0.0 a 239.255.255.255
    private static final String MULTICAST_GROUP_IP = "225.4.5.6";
    private static final int PORT = 4444;

    public static void main(String[] args) {
        System.out.println("--- Demonstrando IP Multicasting no Java NIO.2 ---");

        try {
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP_IP);

            // Valida se o IP pertence à faixa de multicast
            if (!group.isMulticastAddress()) {
                System.err.println("O endereço " + MULTICAST_GROUP_IP + " não é um IP de multicast válido!");
                return;
            }
            System.out.println("[OK] " + MULTICAST_GROUP_IP + " é um IP de multicast válido.");

            // Procura uma interface ativa, com suporte a multicast e endereço IPv4
            Optional<NetworkInterface> multicastInterface = findMulticastInterface();
            if (multicastInterface.isEmpty()) {
                System.err.println("Nenhuma interface de rede ativa com suporte a multicast foi encontrada.");
                return;
            }
            NetworkInterface networkInterface = multicastInterface.get();
            System.out.println("[OK] Interface escolhida: " + networkInterface.getDisplayName());

            // Abre o DatagramChannel, que implementa MulticastChannel
            // A família do canal (INET) precisa ser a mesma do endereço do grupo (IPv4).
            try (DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET)) {

                // SO_REUSEADDR antes do bind, para vários sockets escutarem o mesmo grupo/porta
                channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
                channel.bind(new InetSocketAddress(PORT));
                // Interface usada para enviar datagramas ao grupo
                channel.setOption(StandardSocketOptions.IP_MULTICAST_IF, networkInterface);
                System.out.println("[OK] DatagramChannel vinculado à porta " + PORT);

                // Entra no grupo de multicast via join()
                MembershipKey key = channel.join(group, networkInterface);
                System.out.println("[OK] Inscrição realizada com sucesso no grupo!");

                // Inspeciona os métodos do MembershipKey
                System.out.println("\n--- Propriedades da MembershipKey ---");
                System.out.println(" • É uma chave válida? " + key.isValid());
                System.out.println(" • IP do grupo: " + key.group().getHostAddress());
                System.out.println(" • Interface de rede: " + key.networkInterface().getDisplayName());
                System.out.println(" • Canal associado: " + key.channel().getClass().getSimpleName());
                // null quando o join não é filtrado por origem (source-specific)
                System.out.println(" • Endereço de origem: " + key.sourceAddress());

                // Sai do grupo com drop()
                key.drop();
                System.out.println("\n[OK] Chave cancelada (drop). Válida após o drop? " + key.isValid());
            }
        } catch (IOException e) {
            System.err.println("Erro na execução do exemplo de multicast: " + e.getMessage());
        }
    }

    private static Optional<NetworkInterface> findMulticastInterface() throws IOException {
        NetworkInterface fallback = null;

        for (NetworkInterface candidate : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (!candidate.isUp() || !candidate.supportsMulticast() || !hasIPv4Address(candidate)) {
                continue;
            }
            if (!candidate.isLoopback()) {
                return Optional.of(candidate);
            }
            fallback = candidate; // loopback só se não houver outra opção
        }
        return Optional.ofNullable(fallback);
    }

    private static boolean hasIPv4Address(NetworkInterface networkInterface) {
        return networkInterface.inetAddresses().anyMatch(Inet4Address.class::isInstance);
    }
}