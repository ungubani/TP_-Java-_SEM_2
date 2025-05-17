package org.suai.lab_12;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private static ConcurrentHashMap<String, PrintWriter> clientWriters = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        int portNumber = Integer.parseInt(args[0]);
        ServerSocket serverSocket = new ServerSocket(portNumber);
        System.out.println("Server vzletel, port: " + portNumber);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            ClientHandler handler = new ClientHandler(clientSocket);
            handler.start();
        }
    }

    static class ClientHandler extends Thread {
        private Socket socket;
        private String clientName = null;
        private PrintWriter out;
        private BufferedReader in;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.startsWith("@name ")) {
                        clientName = inputLine.substring(6).trim();
                        if (clientName.isEmpty() || clientWriters.containsKey(clientName) || clientName.equals("Server")) {
                            out.println("Takoe imya zaprescheno ili ono uzhe zanyato.");
                        } else {
                            clientWriters.put(clientName, out);
                            broadcast("[Server]: '" + clientName + "' zaletel v chat!");
                        }
                    } else if (inputLine.equals("@quit")) {
                        if (clientName != null) {
                            clientWriters.remove(clientName);
                            broadcast("[Server]: '" + clientName + "' uletel ot nas :(");
                        }
                        break;
                    } else if (inputLine.startsWith("@senduser ")) {
                        int firstSpace = inputLine.indexOf(' ', 10);
                        if (firstSpace != -1) {
                            String targetName = inputLine.substring(10, firstSpace);
                            String privateMessage = inputLine.substring(firstSpace + 1);
                            sendPrivateMessage(targetName, privateMessage);
                            out.println("/otpravleno/");
                        } else {
                            out.println("Ispolsovanie: @senduser <username> <message>");
                        }
                    } else {
                        if (clientName != null) {
                            broadcastExcluded("[" + clientName + "]: " + inputLine);
                            out.println("/otpravleno/");
                        } else {
                            out.println("Nazovis! @name <yourname>");
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Oshibka otpravki clientu: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Ne poluchilos zakrit socket: " + e.getMessage());
                }
            }
        }

        private void broadcast(String message) {
            for (PrintWriter writer : clientWriters.values()) {
                writer.println(message);
            }
        }

        private void broadcastExcluded(String message) {
            for (String client : clientWriters.keySet()) {
                if (clientName.equals(client)) {
                    continue;
                }
                clientWriters.get(client).println(message);
            }
        }

        private void sendPrivateMessage(String targetName, String message) {
            PrintWriter targetWriter = clientWriters.get(targetName);
            if (targetWriter != null) {
                targetWriter.println("(Private) [" + clientName + "]: " + message);
            } else {
                out.println("Polsovatel '" + targetName + "' ne naiden.");
            }
        }
    }
}
