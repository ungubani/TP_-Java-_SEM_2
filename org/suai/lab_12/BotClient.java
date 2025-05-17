package org.suai.lab_12;

import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;

public class BotClient {
    private static final String BOT_NAME = "BotChata";
    private static PrintWriter out;
    private static BufferedReader in;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Ispolzovanie: java BotClient <host name> <port number>");
            return;
        }

        System.out.println("Bot " + BOT_NAME + " zapuschen");

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(hostName, portNumber)) {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("@name " + BOT_NAME);

            new Thread(() -> {
                try {
                    while (true) {
                        TimeUnit.SECONDS.sleep(30);
                        out.println("Vi nahodites na servere MnogopolzChat");
                    }
                } catch (InterruptedException e) {
                    System.err.println("Bot periodic thread interrupted");
                }
            }).start();

            String fromServer;
            while ((fromServer = in.readLine()) != null) {
                // System.out.println(fromServer);

                handleMessage(fromServer);
            }

        } catch (UnknownHostException e) {
            System.err.println("Chto za host?... " + hostName);
        } catch (IOException e) {
            System.err.println("I/O error " + hostName);
        }
    }

    private static void handleMessage(String message) {
        if (message.startsWith("(Private)")) {
            String reply = extractPrivateMessage(message);
            // System.out.println("REPLY: " + reply);
            if (reply != null) {
                out.println(reply);
            }
        } else if (message.contains("privet")) {
            out.println("privet ot starih stiblet");
        }
    }

    private static String extractPrivateMessage(String fullMessage) {
        int colonIndex = fullMessage.indexOf(":");
        if (colonIndex != -1 && colonIndex + 1 < fullMessage.length()) {
            String content = fullMessage.substring(colonIndex + 1).trim();
            content = "@senduser " + fullMessage.substring(11, colonIndex - 1) + " " + content;
            return content;
        }
        return null;
    }
}
