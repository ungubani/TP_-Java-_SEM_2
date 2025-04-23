package org.suai.lab_11;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class UDPChatClient {
    private static String userName = "Client";
    private static volatile boolean running = true;

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Использование: java UDPChatClient <ip> <порт>");
            return;
        }

        InetAddress serverIP = InetAddress.getByName(args[0]);
        int serverPort = Integer.parseInt(args[1]);

        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(1000);

        System.out.println("Клиент подключается к " + serverIP.getHostAddress() + ":" + serverPort);
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
                }

                String fullMessage = "[" + userName + "]: " + message;
                send(socket, fullMessage, serverIP, serverPort);
            }
            scanner.close();
        });

        Thread receiver = new Thread(() -> {
            byte[] buffer = new byte[1024];
            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(packet);
                    String msg = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("[Сервер]: " + msg);

                    if (msg.trim().endsWith("@quit")) {
                        running = false;
                        System.out.println("Сервер завершил работу.");
                    }
                } catch (SocketTimeoutException e) {
                } catch (IOException e) {
                    if (running) System.err.println("Ошибка: " + e.getMessage());
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
        System.out.println("Клиент завершил работу.");
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
