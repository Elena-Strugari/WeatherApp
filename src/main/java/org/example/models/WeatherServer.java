package org.example.models;

import java.net.ServerSocket;
import java.net.Socket;

public class WeatherServer {
    public static void main(String[] args) {
        int port = 12345;

        try {
            WeatherDataHandler.initDatabase(); // Inițializează baza de date
            System.out.println("Database initialized.");

            // Pornește serverul
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Weather Server started on port " + port);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(new ClientHandler(clientSocket)).start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

