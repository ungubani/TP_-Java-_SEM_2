package org.suai.lab_11;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class UDPChatServer {
    private static volatile boolean running = true;
    private static String username = "Server";

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Использование: java UDPChatServer <порт>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        DatagramSocket socket = new DatagramSocket(port);

        Thread receiver = new Thread(() -> {
            byte[] receiveBuffer = new byte[1024];
            while (running) {
                try {
                    DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    socket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("\n[Клиент]: " + message);
                    if (message.trim().equals("@quit")) {
                        System.out.println("Клиент вышел. Завершение чата.");
                        running = false;
                    }
                } catch (IOException e) {
                    if (running) e.printStackTrace();
                }
            }
        });

        Thread sender = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            InetAddress clientAddress = null;
            int clientPort = -1;

            System.out.println("Введите @name <имя>, чтобы задать имя. Введите сообщение для отправки. @quit - выход.");

            while (running) {
                String input = scanner.nextLine().trim();
                if (input.startsWith("@name ")) {
                    username = input.substring(6).trim();
                    System.out.println("Имя пользователя установлено: " + username);
                    continue;
                } else if (input.equals("@quit")) {
                    running = false;
                    input = "@quit";
                }

                try {
                    if (clientAddress == null || clientPort == -1) {
                        System.out.println("Введите IP и порт клиента (через пробел):");
                        String[] parts = scanner.nextLine().split(" ");
                        clientAddress = InetAddress.getByName(parts[0]);
                        clientPort = Integer.parseInt(parts[1]);
                    }

                    String fullMessage = username + ": " + input;
                    byte[] sendBuffer = fullMessage.getBytes();
                    DatagramPacket packet = new DatagramPacket(sendBuffer, sendBuffer.length, clientAddress, clientPort);
                    socket.send(packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            socket.close();
        });

        receiver.start();
        sender.start();

        receiver.join();
        sender.join();
    }
}
