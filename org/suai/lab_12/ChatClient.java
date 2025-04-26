package org.suai.lab_12;

import java.io.*;
import java.net.*;

public class ChatClient {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: java ChatClient <host> <port>");
            return;
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        Socket socket = new Socket(hostName, portNumber);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        Thread readerThread = new Thread(() -> {
            try {
                String fromServer;
                while ((fromServer = in.readLine()) != null) {
                    System.out.println(fromServer);
                }
            } catch (IOException e) {
                System.err.println("Connection closed: " + e.getMessage());
            }
        });
        readerThread.start();

        System.out.println("Connected to the chat server.");
        String userInput;
        while ((userInput = stdIn.readLine()) != null) {
            out.println(userInput);
            if (userInput.equals("@quit")) {
                break;
            }
        }

        socket.close();
        System.exit(0);
    }
}
