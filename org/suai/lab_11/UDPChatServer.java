package org.suai.lab_11;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class UDPChatServer {
    private static String userName = "Server";
    private static volatile boolean running = true;
    private static InetAddress clientAddress = null;
    private static int clientPort = -1;

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Использование: java UDPChatServer <порт>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        DatagramSocket socket = new DatagramSocket(port);
        socket.setSoTimeout(1000);

        InetAddress localAddress = InetAddress.getLocalHost();

        System.out.println("Сервер запущен. IP: " + localAddress.getHostAddress() + ", порт: " + port);
        System.out.println("Команды: @name <имя>, @quit");

        Thread sender = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (running) {
                if (!scanner.hasNextLine()) continue;
                String message = scanner.nextLine();

                if (message.startsWith("@name ")) {
                    userName = message.substring(6);
                    System.out.println("Имя изменено: " + userName);
                    continue;
                }

                if (message.equals("@quit")) {
                    running = false;
                    break;
                }

                if (clientAddress == null || clientPort == -1) {
                    System.out.println("Клиент ещё не подключился...");
                    continue;
                }

                send(socket, "[" + userName + "]: " + message, clientAddress, clientPort);
            }
            scanner.close();
        });

        Thread receiver = new Thread(() -> {
            byte[] buffer = new byte[1024];
            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(packet);
                    clientAddress = packet.getAddress();
                    clientPort = packet.getPort();

                    String msg = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("[Клиент]: " + msg);

                    if (msg.trim().endsWith("@quit")) {
                        System.out.println("Клиент завершил соединение.");
                        running = false;
                    }
                } catch (SocketTimeoutException e) {
                } catch (IOException e) {
                    if (running) System.err.println("Ошибка при получении: " + e.getMessage());
                }
            }
        });

        sender.start();
        receiver.start();

        try {
            sender.join();
            receiver.join();
        } catch (InterruptedException ignored) {}

        socket.close(); 
        System.out.println("Сервер завершил работу.");
    }

    private static void send(DatagramSocket socket, String msg, InetAddress ip, int port) {
        byte[] buffer = msg.getBytes();
        try {
            socket.send(new DatagramPacket(buffer, buffer.length, ip, port));
        } catch (IOException e) {
            System.err.println("Ошибка при отправке: " + e.getMessage());
        }
    }
}
