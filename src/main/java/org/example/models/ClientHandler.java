package org.example.models;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            // Pasul 1: Identificare client
           // out.println("Introduceți numele dvs:");
            String clientName = in.readLine();

            boolean isAdmin = WeatherDataHandler.isAdmin(clientName);

            if (isAdmin) {
                out.println("Bun venit, admin " + clientName + "!");
                out.println("Aveți opțiunea de a adăuga/modifica locații.");
                String adminResponse = in.readLine().trim();

                if (adminResponse.equalsIgnoreCase("da")) {
                    handleAdminOptions(out, in);
                }
            } else {
                out.println("Bun venit, " + clientName + "!");
            }

            String location = WeatherDataHandler.getClientLocation(clientName).orElse(null);

            if (location == null) {
                out.println("Nu există informații pentru clientul specificat.");
                return;
            }

            // Pasul 2: Afișare prognoză pentru locația curentă
            out.println("Locația dvs. curentă este: " + location);
            String weather = WeatherDataHandler.getLocation(location);
            out.println(weather);

            // Pasul 3: Întrebăm dacă dorește alte locații
            while (true) {
                out.println("Doriți să aflați informații despre alte locații? (da/nu)");
                String response = in.readLine().trim();

                if (response.equalsIgnoreCase("nu")) {
                    out.println("Vă mulțumim că ați utilizat aplicația noastră!");
                    break;
                }

                if (response.equalsIgnoreCase("da")) {
                    out.println("Introduceți numele locației:");
                    String locationName = in.readLine();

                    String weatherLoc = WeatherDataHandler.getLocation(locationName);
                    if (weatherLoc.equals("Location not found.")) {
                        out.println("Locația nu a fost găsită. Introduceți coordonatele:");

                        out.println("Latitudine:");
                        double latitude = Double.parseDouble(in.readLine());

                        out.println("Longitudine:");
                        double longitude = Double.parseDouble(in.readLine());

                        double maxDistance = 500;
                        String nearest = WeatherDataHandler.findNearestLocation(latitude, longitude, maxDistance);
                        out.println(nearest);
                    } else {
                        out.println(weatherLoc);
                    }
                } else {
                    out.println("Răspuns invalid. Vă rugăm să introduceți 'da' sau 'nu'.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleAdminOptions(PrintWriter out, BufferedReader in) throws IOException {
        out.println("Alegeți o opțiune: \n1. Adăugați o locație\n2. Modificați o locație existentă\n3. Ieșiți");
        String choice = in.readLine();

        switch (choice) {
            case "1":
                out.println("Introduceți numele locației:");
                String newName = in.readLine();

                out.println("Introduceți latitudinea:");
                double newLatitude = Double.parseDouble(in.readLine());

                out.println("Introduceți longitudinea:");
                double newLongitude = Double.parseDouble(in.readLine());

                out.println("Introduceți prognoza curentă:");
                String newForecast = in.readLine();

                out.println("Introduceți prognoza pentru următoarele zile (separate prin virgulă):");
                String[] newForecastNextDays = in.readLine().split(",");

                WeatherDataHandler.addLocation(newName, newLatitude, newLongitude, newForecast, newForecastNextDays);
                out.println("Locația a fost adăugată cu succes!");
                break;

            case "2":
                out.println("Introduceți numele locației pe care doriți să o modificați:");
                String existingName = in.readLine();

                out.println("Introduceți noua prognoză curentă:");
                String updatedForecast = in.readLine();

                out.println("Introduceți noua prognoză pentru următoarele zile (separate prin virgulă):");
                String[] updatedForecastNextDays = in.readLine().split(",");

                WeatherDataHandler.updateLocation(existingName, updatedForecast, updatedForecastNextDays);
                out.println("Locația a fost actualizată cu succes!");
                break;

            case "3":
                out.println("Ieșire din modul admin.");
                break;

            default:
                out.println("Opțiune invalidă.");
        }
    }


}

