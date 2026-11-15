# redes-jdk

Estudos de programação de redes em Java usando só as APIs que vêm no JDK: sockets, NIO, TCP, UDP, multicast e o Simple Web Server.

Cada classe é um exemplo autocontido, com seu próprio `main`. O projeto não tem dependências externas: tudo vem de `java.net`, `java.nio.channels` e `com.sun.net.httpserver`.

## Requisitos

- JDK 18 ou superior. O Simple Web Server (JEP 408) só existe a partir do 18; os outros exemplos rodam em versões anteriores.
- Testado com OpenJDK 21.

## Exemplos

### Fundamentos

| Classe | O que mostra |
|---|---|
| [`SocketBasicsIntro`](src/caramelo/dev/SocketBasicsIntro.java) | `InetAddress` para o localhost e o loopback `127.0.0.1`, e um endpoint com `InetSocketAddress` (IP + porta). |
| [`ExploringNetworkInterfaces`](src/caramelo/dev/ExploringNetworkInterfaces.java) | Lista as interfaces de rede da máquina com `NetworkInterface`: propriedades (up, loopback, MTU) e os IPs de cada uma. |
| [`SocketApiOptionsIntro`](src/caramelo/dev/SocketApiOptionsIntro.java) | Lê e configura `StandardSocketOptions` em `ServerSocketChannel` e `SocketChannel`. O tamanho de buffer é só uma dica: o sistema operacional pode ajustar. |

### TCP

| Classe | O que mostra |
|---|---|
| [`WritingBlockingTcpServerClient`](src/caramelo/dev/WritingBlockingTcpServerClient.java) | Servidor e cliente TCP bloqueantes com `SocketChannel`. Uma requisição por conexão: o cliente chama `shutdownOutput()` e o servidor lê até o EOF. |
| [`NonBlockingTcpServerClient`](src/caramelo/dev/NonBlockingTcpServerClient.java) | Servidor eco não bloqueante com `Selector`, tratando `OP_ACCEPT`, `OP_READ` e `OP_CONNECT` numa única thread. |
| [`TcpServers`](src/caramelo/dev/TcpServers.java) | Os dois modelos lado a lado: bloqueante (uma thread por cliente) e não bloqueante (uma thread, um `Selector`, muitas conexões). |

### UDP e multicast

| Classe | O que mostra |
|---|---|
| [`WritingUdpServerClient`](src/caramelo/dev/udp/WritingUdpServerClient.java) | Servidor UDP eco com `DatagramChannel` e dois clientes: sem conexão (`send`/`receive`) e com conexão (`connect` + `write`/`read`). |
| [`IntroducingMulticasting`](src/caramelo/dev/udp/IntroducingMulticasting.java) | Entra no grupo `225.4.5.6` com `join()`, inspeciona o `MembershipKey` e sai com `drop()`. |

### HTTP

| Classe | O que mostra |
|---|---|
| [`SimpleWebServerIntro`](src/caramelo/dev/sws/SimpleWebServerIntro.java) | Servidor de arquivos estáticos com `SimpleFileServer` (JEP 408), servindo a pasta [`docs/`](docs/). |

## Portas usadas

Dois exemplos na mesma porta e no mesmo protocolo não rodam ao mesmo tempo.

| Porta | Protocolo | Classe |
|---|---|---|
| 4444 | TCP | `NonBlockingTcpServerClient` |
| 4444 | UDP | `WritingUdpServerClient` |
| 4444 | UDP multicast | `IntroducingMulticasting` |
| 8080 | TCP | `TcpServers` (bloqueante) |
| 8081 | TCP | `TcpServers` (não bloqueante) |
| 8080 | HTTP | `SimpleWebServerIntro` |

## Como executar

Pela IDE, rode o `main` da classe desejada.

Pelo terminal, na raiz do projeto, compile tudo para `out/`:

```bash
javac -d out $(find src -name "*.java")
```

E rode a classe pelo nome completo, por exemplo:

```bash
java -cp out caramelo.dev.udp.WritingUdpServerClient
```

## Documentação com o Simple Web Server

A pasta [`docs/`](docs/) tem uma página HTML com a documentação do projeto, feita para ser servida pelo `SimpleWebServerIntro`:

```bash
java -cp out caramelo.dev.sws.SimpleWebServerIntro
```

Depois, abra http://localhost:8080/.

O caminho `./docs` é resolvido a partir do diretório de trabalho, então rode da raiz do projeto (na IDE, confira o *working directory* da configuração de execução).

Sem escrever código, o JDK também traz a ferramenta de linha de comando:

```bash
jwebserver -d "$(pwd)/docs" -p 8080
```

O `jwebserver` exige caminho absoluto no `-d`. O Simple Web Server só atende `GET` e `HEAD` de arquivos estáticos: serve para estudo, testes e protótipos, não para produção.

## Estrutura

```
redes-jdk/
├── docs/                      # página servida pelo Simple Web Server
│   ├── index.html
│   └── style.css
└── src/caramelo/dev/
    ├── SocketBasicsIntro.java
    ├── ExploringNetworkInterfaces.java
    ├── SocketApiOptionsIntro.java
    ├── WritingBlockingTcpServerClient.java
    ├── NonBlockingTcpServerClient.java
    ├── TcpServers.java
    ├── udp/
    │   ├── WritingUdpServerClient.java
    │   └── IntroducingMulticasting.java
    └── sws/
        └── SimpleWebServerIntro.java
```

---

Conteúdo de estudo do [getcaramelo.dev](https://getcaramelo.dev).
