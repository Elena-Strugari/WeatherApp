package org.example.models;

import com.google.gson.*;
import java.io.FileReader;
import java.io.Reader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Optional;
import java.util.Objects;
import java.util.Comparator;
import java.util.Scanner;

public class WeatherDataHandler {
    private static final String DB_URL = "jdbc:sqlite:weather.db";

    // Inițializează baza de date
    public static void initDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // Creează tabelul Locations dacă nu există
            String createLocationsTable = "CREATE TABLE IF NOT EXISTS Locations (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "latitude REAL NOT NULL, " +
                    "longitude REAL NOT NULL, " +
                    "current_forecast TEXT NOT NULL," +
                    "forecast_next_days TEXT NOT NULL)";
            stmt.execute(createLocationsTable);

            // Creează tabelul ClientLocations dacă nu exista
            String createClientLocationsTable = "CREATE TABLE IF NOT EXISTS ClientLocations (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "client_name TEXT NOT NULL, " +
                    "location_name TEXT NOT NULL, " +
                    "is_admin INTEGER NOT NULL DEFAULT 0)"; // 0 = utilizator normal, 1 = admin
            stmt.execute(createClientLocationsTable);

            System.out.println("Database initialized.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    // Încarcă datele din fișierul JSON în baza de date
    public static void loadJsonData(String filePath) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            Gson gson = new Gson();
            Reader reader = new FileReader(filePath);
            Location[] locations = gson.fromJson(reader, Location[].class);

            String insertSQL = "INSERT INTO Locations (name, latitude, longitude, current_forecast, forecast_next_days) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertSQL);

            for (Location location : locations) {
                pstmt.setString(1, location.getName());
                pstmt.setDouble(2, location.getLatitude());
                pstmt.setDouble(3, location.getLongitude());
                pstmt.setString(4, location.getCurrent());
                pstmt.setString(5, gson.toJson(location.getForecast()));
                pstmt.executeUpdate();
            }
            System.out.println("Data loaded successfully from JSON.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class Location {
        private String name;
        private double latitude;
        private double longitude;
        private String current;
        private String[] forecast;

        public String getName() {
            return name;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public String getCurrent() {
            return current;
        }

        public String[] getForecast() {
            return forecast;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Location location = (Location) o;
            return Double.compare(location.latitude, latitude) == 0 &&
                    Double.compare(location.longitude, longitude) == 0 &&
                    name.equals(location.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, latitude, longitude);
        }
    }

    public static String getLocation(String locationName) {
        String sql = "SELECT current_forecast, forecast_next_days FROM Locations WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, locationName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String currentForecast = rs.getString("current_forecast");
                String forecastNextDaysJson = rs.getString("forecast_next_days");

                // Convertim forecast_next_days din JSON string în array
                Gson gson = new Gson();
                String[] forecastNextDays = gson.fromJson(forecastNextDaysJson, String[].class);

                StringBuilder forecast = new StringBuilder();
                forecast.append("Current weather: ").append(currentForecast).append("\n");
                forecast.append("Forecast for the next days:\n");

                for (int i = 0; i < forecastNextDays.length; i++) {
                    forecast.append("Day ").append(i + 1).append(": ").append(forecastNextDays[i]).append("\n");
                }

                return forecast.toString();
            } else {
                return "Location not found.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error retrieving location.";
        }
    }


    public static String findNearestLocation(double latitude, double longitude, double maxDistance) {
        String sql = "SELECT name, latitude, longitude, current_forecast FROM Locations";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            List<LocationDistance> locations = new ArrayList<>();
            while (rs.next()) {
                String name = rs.getString("name");
                double locLat = rs.getDouble("latitude");
                double locLon = rs.getDouble("longitude");
                String currentForecast = rs.getString("current_forecast");
                double distance = haversine(latitude, longitude, locLat, locLon);

                if (distance <= maxDistance) {
                    locations.add(new LocationDistance(name, locLat, locLon, currentForecast, distance));
                }
            }

            // Sortăm locațiile după distanță
            Collections.sort(locations, (a, b) -> Double.compare(a.distance, b.distance));

            // Returnăm cea mai apropiată locație sau un mesaj dacă lista este goală
            if (!locations.isEmpty()) {
                LocationDistance nearest = locations.get(0);
                return String.format("Cea mai apropiată locație: %s (%.2f km)\nCurrent weather: %s",
                        nearest.name, nearest.distance, nearest.currentForecast);
            } else {
                return "Nu există informații pentru această locație.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Eroare la căutarea locației.";
        }
    }

    private static class LocationDistance {
        String name;
        double latitude;
        double longitude;
        String currentForecast;
        double distance;

        public LocationDistance(String name, double latitude, double longitude, String currentForecast, double distance) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
            this.currentForecast = currentForecast;
            this.distance = distance;
        }
    }


    // Formula haversine
    private static double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Raza Pământului în km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public static void deleteLocation(String locationName) {
        String sql = "DELETE FROM Locations WHERE name = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, locationName);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Locația a fost ștearsă cu succes.");
            } else {
                System.out.println("Locația nu a fost găsită.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void loadClientData(String filePath) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            Gson gson = new Gson();
            Reader reader = new FileReader("C:\\Users\\elena\\OneDrive\\Documente\\Facultate-INFORMATICA\\An_2\\sem1\\MIP\\vreme\\src\\main\\java\\org\\example\\models\\client.json");
            Client[] clients = gson.fromJson(reader, Client[].class);

            String insertSQL = "INSERT INTO ClientLocations (client_name, location_name) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertSQL);

            for (Client client : clients) {
                pstmt.setString(1, client.getName());
                pstmt.setString(2, client.getLocation());
                pstmt.executeUpdate();
            }
            System.out.println("Client data loaded successfully from JSON.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Clasa pentru clienți
    static class Client {
        private String name;
        private String location;

        public String getName() {
            return name;
        }

        public String getLocation() {
            return location;
        }
    }
    public static Optional<String> getClientLocation(String clientName) {
        String sql = "SELECT location_name FROM ClientLocations WHERE client_name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, clientName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(rs.getString("location_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
    public static boolean isAdmin(String clientName) {
        String sql = "SELECT is_admin FROM ClientLocations WHERE client_name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, clientName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("is_admin") == 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void addLocation(String name, double latitude, double longitude, String currentForecast, String[] forecastNextDays) {
        String insertSQL = "INSERT INTO Locations (name, latitude, longitude, current_forecast, forecast_next_days) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setString(1, name);
            pstmt.setDouble(2, latitude);
            pstmt.setDouble(3, longitude);
            pstmt.setString(4, currentForecast);
            pstmt.setString(5, new Gson().toJson(forecastNextDays));

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Locația a fost adăugată cu succes.");
            } else {
                System.out.println("Nu s-a putut adăuga locația.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateLocation(String name, String updatedForecast, String[] updatedNextDaysForecast) {
        String sql = "UPDATE Locations SET current_forecast = ?, forecast_next_days = ? WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            Gson gson = new Gson();
            pstmt.setString(1, updatedForecast);
            pstmt.setString(2, gson.toJson(updatedNextDaysForecast));
            pstmt.setString(3, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




}
