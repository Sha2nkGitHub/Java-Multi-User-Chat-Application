import java.net.*;
import java.nio.channels.*;
import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private List<PrintWriter> clients = new ArrayList<>();
    private final int PORT = 5000;
    public static void main(String... args) {
        new ChatServer().go();
    }

    public void go() {
        ExecutorService threadPool = Executors.newCachedThreadPool();
        try {
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(PORT));
            System.out.println("Server is listeneing for connections : ");
            while (serverChannel.isOpen()) {
                SocketChannel socketChannel = serverChannel.accept();
                PrintWriter writer = new PrintWriter(Channels.newWriter(socketChannel, StandardCharsets.UTF_8));
                clients.add(writer);
                threadPool.submit(new ClientHandler(socketChannel));
                System.out.println("Client connected");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class ClientHandler implements Runnable {
        BufferedReader reader;
        SocketChannel socket;

        public void run() {
            try {
                String line;
                while (reader != null && (line = reader.readLine()) != null) {
                    System.out.println(line);
                    tellEveryone(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public ClientHandler(SocketChannel channel) {
            socket = channel;
            reader = new BufferedReader(Channels.newReader(socket, StandardCharsets.UTF_8));
        }
    }

    private void tellEveryone(String msg) {
        for (PrintWriter writer : clients) {
            writer.println(msg);
            writer.flush();
        }
    }
}