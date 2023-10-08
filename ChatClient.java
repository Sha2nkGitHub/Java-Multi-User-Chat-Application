import javax.swing.*;
import java.net.*;
import java.nio.channels.*;
import java.io.*;
import java.nio.charset.*;
import java.util.concurrent.*;
import java.awt.*;
import java.util.*;

public class ChatClient {
    private BufferedReader reader;
    private PrintWriter writer;
    private JTextField msgField;
    private JTextArea area;
    private String name;

    public static void main(String... args) {
        ChatClient client = new ChatClient();
        try {
            client.setName(args[0]);
            client.go();
        } catch (Exception e) {
            System.out.println("Format to run client code : java ChatClient <clientName>");
            e.printStackTrace();
        }
    }

    private void setName(String newName) {
        name = newName;
    }

    public void go() {
        setupNetworking();
        JFrame frame = new JFrame("JavaChat");

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Raleway", Font.ITALIC, 17));

        area = new JTextArea(10, 30);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setFont(new Font("Raleway", Font.PLAIN, 18));
        JScrollPane scroller = new JScrollPane(area);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        msgField = new JTextField(30);
        msgField.setFont(new Font("Raleway", Font.BOLD, 16));
        msgField.requestFocus();

        JButton send = new JButton("Send");
        send.addActionListener(event -> sendMessage());
        send.setBackground(new Color(7, 69, 168));
        send.setForeground(Color.WHITE);
        send.setFont(new Font("Raleway", Font.BOLD, 18));

        Box boxInner = new Box(BoxLayout.X_AXIS);
        boxInner.add(msgField);
        boxInner.add(Box.createHorizontalStrut(10));
        boxInner.add(send);

        Box boxOuter = new Box(BoxLayout.Y_AXIS);
        boxOuter.add(Box.createVerticalStrut(10));
        boxOuter.add(nameLabel);
        boxOuter.add(Box.createVerticalStrut(5));
        boxOuter.add(scroller);
        boxOuter.add(Box.createVerticalStrut(10));
        boxOuter.add(boxInner);
        JPanel panel = new JPanel();
        panel.add(boxOuter);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new KeepReading());

        frame.getContentPane().add(BorderLayout.CENTER, panel);
        Random rand = new Random();
        panel.setBackground(new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256), 100));

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(580, 390);
        frame.setLocation(200, 100);
        frame.setVisible(true);
    }

    public void setupNetworking() {
        try {
            InetSocketAddress server = new InetSocketAddress("127.0.0.1", 5000);
            SocketChannel socketChannel = SocketChannel.open(server);

            reader = new BufferedReader(Channels.newReader(socketChannel, StandardCharsets.UTF_8));
            writer = new PrintWriter(Channels.newWriter(socketChannel, StandardCharsets.UTF_8));

            System.out.println("Connection established with server");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        if (msgField.getText().equals(""))
            return;
        writer.println(name + " : " + msgField.getText());
        writer.flush();
        msgField.setText("");
        msgField.requestFocus();
    }

    class KeepReading implements Runnable {
        public void run() {
            String msg = "";
            try {
                while ((msg = reader.readLine()) != "") {
                    area.append(msg + "\n");
                    System.out.println(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}