package caramelo.dev;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Explorando interfaces de rede com a NetworkInterface API.
 */
public class ExploringNetworkInterfaces {

    public static void main(String[] args) {
        System.out.println("--- Listando e Inspecionando Interfaces de Rede ---");

        try {
            // 1. Obtém todas as interfaces de rede disponíveis no sistema
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            if (interfaces == null) {
                System.out.println("Nenhuma interface de rede encontrada.");
                return;
            }

            // 2. Itera sobre cada interface e exibe suas propriedades
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();

                System.out.println("\nNome: " + ni.getName());
                System.out.println("Nome de exibição: " + ni.getDisplayName());
                System.out.println("Índice: " + ni.getIndex());
                System.out.println("Ativa (up): " + ni.isUp());
                System.out.println("Loopback: " + ni.isLoopback());
                System.out.println("Virtual: " + ni.isVirtual());
                System.out.println("Ponto a ponto: " + ni.isPointToPoint());
                System.out.println("Suporta multicast: " + ni.supportsMulticast());
                System.out.println("MTU: " + ni.getMTU());
                System.out.println("MAC: " + formatMac(ni.getHardwareAddress()));

                // 3. Lista os endereços IP associados à interface
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    System.out.println("  Endereço: " + address.getHostAddress());
                }
            }
        } catch (SocketException e) {
            System.err.println("Erro ao acessar as interfaces de rede: " + e.getMessage());
        }
    }

    private static String formatMac(byte[] mac) {
        if (mac == null) {
            return "N/A";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            sb.append(String.format("%02X", mac[i]));
            if (i < mac.length - 1) {
                sb.append("-");
            }
        }
        return sb.toString();
    }
}