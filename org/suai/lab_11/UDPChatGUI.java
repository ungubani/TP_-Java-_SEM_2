package org.suai.lab_11;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.*;
import java.util.function.Consumer;


class UDPChatServer {
    private DatagramSocket socket;
    private InetAddress clientAddress;
    private int clientPort = -1;
    private volatile boolean running = true;
    private Consumer<String> messageCallback;

    public UDPChatServer(int port) throws SocketException, UnknownHostException {
        socket = new DatagramSocket(port);
    }

    public void setOnMessageReceived(Consumer<String> callback) {
        this.messageCallback = callback;
    }

    public void start() {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            while (running) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String msg = new String(packet.getData(), 0, packet.getLength());
                    if (messageCallback != null) messageCallback.accept("[Client]: " + msg);

                    clientAddress = packet.getAddress();
                    clientPort = packet.getPort();

                    if (msg.trim().equals("@quit")) {
                        stop();
                    }
                } catch (IOException e) {
                    if (running) e.printStackTrace();
                }
            }
        }).start();
    }

    public void sendMessage(String msg) {
        if (clientAddress == null || clientPort == -1) return;
        try {
            byte[] data = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, clientAddress, clientPort);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}


class UDPChatClient {
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;
    private volatile boolean running = true;
    private Consumer<String> messageCallback;

    public UDPChatClient(String ip, int port) throws Exception {
        socket = new DatagramSocket();
        serverAddress = InetAddress.getByName(ip);
        serverPort = port;
    }

    public void setOnMessageReceived(Consumer<String> callback) {
        this.messageCallback = callback;
    }

    public void start() {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            while (running) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String msg = new String(packet.getData(), 0, packet.getLength());
                    if (messageCallback != null) messageCallback.accept("[Server]: " + msg);
                } catch (IOException e) {
                    if (running) e.printStackTrace();
                }
            }
        }).start();
    }

    public void sendMessage(String msg) {
        try {
            byte[] data = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, serverPort);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
        socket.close();
    }
}


class ServerWindow extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private UDPChatServer server;

    public ServerWindow(int port) throws Exception {
        setTitle("UDP Server Chat");
        setSize(400, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        inputField = new JTextField();
        inputField.addActionListener((ActionEvent e) -> {
            String text = inputField.getText();
            if (!text.isEmpty()) {
                server.sendMessage(text);
                chatArea.append("[You]: " + text + "\n");
                if (text.trim().equals("@quit")) server.stop();
                inputField.setText("");
            }
        });
        add(inputField, BorderLayout.SOUTH);

        server = new UDPChatServer(port);
        chatArea.append("Server started on: " + InetAddress.getLocalHost().getHostAddress() + ":" + port + "\n");

        server.setOnMessageReceived(msg -> SwingUtilities.invokeLater(() -> chatArea.append(msg + "\n")));
        server.start();
        setVisible(true);
    }
}


class ClientWindow extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private UDPChatClient client;

    public ClientWindow(String ip, int port) throws Exception {
        setTitle("UDP Client Chat");
        setSize(400, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        inputField = new JTextField();
        inputField.addActionListener((ActionEvent e) -> {
            String text = inputField.getText();
            if (!text.isEmpty()) {
                client.sendMessage(text);
                chatArea.append("[You]: " + text + "\n");
                if (text.trim().equals("@quit")) client.stop();
                inputField.setText("");
            }
        });
        add(inputField, BorderLayout.SOUTH);

        client = new UDPChatClient(ip, port);
        client.setOnMessageReceived(msg -> SwingUtilities.invokeLater(() -> chatArea.append(msg + "\n")));
        client.start();
        setVisible(true);
    }
}


class MainServer {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: java org.suai.lab_11.MainServer <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        new ServerWindow(port);
    }
}


class MainClient {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: java org.suai.lab_11.MainClient <server_ip> <server_port>");
            return;
        }
        String ip = args[0];
        int port = Integer.parseInt(args[1]);
        new ClientWindow(ip, port);
    }
}
