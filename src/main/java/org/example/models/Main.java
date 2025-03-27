package org.example.models;

public class Main {
    public static void main(String[] args) {
        WeatherDataHandler.initDatabase(); // Inițializează baza de date
        //WeatherDataHandler.loadJsonData("C:\\Users\\elena\\OneDrive\\Documente\\Facultate-INFORMATICA\\An_2\\sem1\\MIP\\vreme\\src\\main\\java\\org\\example\\models\\locations.json"); // Încarcă datele din JSON
        WeatherDataHandler.loadClientData("C:\\Users\\elena\\OneDrive\\Documente\\Facultate-INFORMATICA\\An_2\\sem1\\MIP\\vreme\\src\\main\\java\\org\\example\\models\\client.json"); // Încarcă datele din JSON

//        WeatherDataHandler.deleteLocation("Paris");
//        WeatherDataHandler.deleteLocation("London");
//        WeatherDataHandler.deleteLocation("Berlin");
//        WeatherDataHandler.deleteLocation("Rome");
//        WeatherDataHandler.deleteLocation("Vienna");
//        WeatherDataHandler.deleteLocation("Prague");
//        WeatherDataHandler.deleteLocation("Lisbon");
//        WeatherDataHandler.deleteLocation("Stockholm");
//      WeatherDataHandler.deleteLocation("Amsterdam");

        // Exemplu de utilizare a getLocation
        String locationInfo = WeatherDataHandler.getLocation("Paris");
        System.out.println(locationInfo); // Afișează informațiile despre locație
    }

}

//update temperatura