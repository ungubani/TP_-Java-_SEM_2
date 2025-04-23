package org.suai.lab_11;


import java.io.*;
import java.net.*;
import java.util.Scanner;

public class UDPChatClient {
    private static volatile boolean running = true;
    private static String username = "Client";

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Использование: java UDPChatClient <IP-сервера> <порт>");
            return;
        }

        InetAddress serverAddress = InetAddress.getByName(args[0]);
        int serverPort = Integer.parseInt(args[1]);
        DatagramSocket socket = new DatagramSocket();

        Thread receiver = new Thread(() -> {
            byte[] receiveBuffer = new byte[1024];
            while (running) {
                try {
                    DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    socket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("\n[Сервер]: " + message);
                    if (message.trim().equals("@quit")) {
                        System.out.println("Сервер завершил чат.");
                        running = false;
                    }
                } catch (IOException e) {
                    if (running) e.printStackTrace();
                }
            }
        });

        Thread sender = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);

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
                    String fullMessage = username + ": " + input;
                    byte[] sendBuffer = fullMessage.getBytes();
                    DatagramPacket packet = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, serverPort);
                    socket.send(packet);
                } catch (IOException e) {
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
