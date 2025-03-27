package org.example.models;

import java.io.*;
import java.net.Socket;

public class WeatherClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // Autoflush activat
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {

            String response;

            // Pasul 1: Introduceți numele utilizatorului
            System.out.print("Introduceți numele dvs.: ");
            String clientName = userInput.readLine();
            out.println(clientName);

            // Pasul 2: Afișare mesaj de bun venit și verificare dacă utilizatorul este admin
            while ((response = in.readLine()) != null) {
                System.out.println(response);

                if (response.contains("Aveți opțiunea de a adăuga/modifica locații")) {
                    handleAdminOptions(out, in, userInput);
                }

                // Oferim opțiunea de a cere informații despre alte locații
                if (response.contains("Doriți să aflați informații despre alte locații? (da/nu)")) {
                    String userResponse = userInput.readLine();
                    out.println(userResponse);

                    if (userResponse.equalsIgnoreCase("nu")) {
                        System.out.println("Vă mulțumim că ați utilizat aplicația noastră!");
                        break;
                    }

                    if (userResponse.equalsIgnoreCase("da")) {
                        System.out.print("Introduceți numele locației: ");
                        String locationName = userInput.readLine();
                        out.println(locationName);
                    }
                }

                // Dacă serverul cere coordonate pentru o locație necunoscută
                if (response.contains("Introduceți coordonatele:")) {
                    System.out.print("Latitudine: ");
                    String latitude = userInput.readLine();
                    out.println(latitude);

                    System.out.print("Longitudine: ");
                    String longitude = userInput.readLine();
                    out.println(longitude);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleAdminOptions(PrintWriter out, BufferedReader in, BufferedReader userInput) throws IOException {
        System.out.println("Sunteți în modul admin. Aveți următoarele opțiuni:");
        System.out.println("1. Adăugați o locație nouă");
        System.out.println("2. Modificați o locație existentă");
        System.out.println("3. Ieșiți din modul admin");

        String choice = userInput.readLine();
        out.println(choice);

        switch (choice) {
            case "1":
                // Adăugare locație nouă
                System.out.print("Introduceți numele locației: ");
                String newName = userInput.readLine();

                System.out.print("Introduceți latitudinea: ");
                double newLatitude = Double.parseDouble(userInput.readLine());

                System.out.print("Introduceți longitudinea: ");
                double newLongitude = Double.parseDouble(userInput.readLine());

                System.out.print("Introduceți prognoza curentă: ");
                String newForecast = userInput.readLine();

                System.out.print("Introduceți prognoza pentru următoarele zile (separate prin virgulă): ");
                String[] newNextDaysForecast = userInput.readLine().split(",");

                // Apelează funcția pentru a adăuga locația
                WeatherDataHandler.addLocation(newName, newLatitude, newLongitude, newForecast, newNextDaysForecast);
                System.out.println("Locația a fost adăugată cu succes!");
                break;

            case "2":
                // Modificare locație existentă
                System.out.print("Introduceți numele locației pe care doriți să o modificați: ");
                String existingName = userInput.readLine();

                System.out.print("Introduceți noua prognoză curentă: ");
                String updatedForecast = userInput.readLine();

                System.out.print("Introduceți noua prognoză pentru următoarele zile (separate prin virgulă): ");
                String[] updatedNextDaysForecast = userInput.readLine().split(",");

                // Apelează funcția pentru a actualiza locația
                WeatherDataHandler.updateLocation(existingName, updatedForecast, updatedNextDaysForecast);
                System.out.println("Locația a fost actualizată cu succes!");
                break;

            case "3":
                System.out.println("Ieșire din modul admin.");
                break;

            default:
                System.out.println("Opțiune invalidă.");
        }
    }}


/*
Functii folosite din tabel
Collections.sort
Optional
equals
hashCode
 */