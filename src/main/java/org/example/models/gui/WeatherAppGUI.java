package org.example.models.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Socket;
import java.io.*;

public class WeatherAppGUI extends JFrame {

    private JTextField nameField;
    private JTextArea weatherInfoArea;
    private JLabel weatherImageLabel;

    public WeatherAppGUI() {
        setTitle("Weather Client Application");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel pentru introducerea numelui clientului
        JPanel namePanel = new JPanel();
        namePanel.setLayout(new FlowLayout());

        JLabel nameLabel = new JLabel("Introduceți numele dvs:");
        nameField = new JTextField(20);
        JButton submitNameButton = new JButton("Submit Name");

        namePanel.add(nameLabel);
        namePanel.add(nameField);
        namePanel.add(submitNameButton);

        // Panel central pentru informațiile despre vreme
        JPanel weatherPanel = new JPanel();
        weatherPanel.setLayout(new BorderLayout());

        weatherInfoArea = new JTextArea();
        weatherInfoArea.setEditable(false);

        weatherImageLabel = new JLabel("", SwingConstants.CENTER);

        weatherPanel.add(new JScrollPane(weatherInfoArea), BorderLayout.CENTER);
        weatherPanel.add(weatherImageLabel, BorderLayout.EAST);

        // Adaugă panouri la fereastră
        add(namePanel, BorderLayout.NORTH);
        add(weatherPanel, BorderLayout.CENTER);

        // Listener pentru numele clientului
        submitNameButton.addActionListener(e -> {
            String clientName = nameField.getText().trim();
            if (!clientName.isEmpty()) {
                handleClient(clientName);
            } else {
                JOptionPane.showMessageDialog(this, "Introduceți un nume valid.");
            }
        });
    }

    private void handleClient(String clientName) {
        try (Socket socket = new Socket("localhost", 12345);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

             new Thread(() -> {
                 // Trimite numele clientului
                 out.println(clientName);
             }
             ).start();

            new Thread(()->{
                // Primește locația curentă și prognoza
                String locationResponse = null;
                try {
                    while (true) {
                        locationResponse = in.readLine();
                        String weatherResponse = in.readLine();
                        // Afișează informațiile complete
                        weatherInfoArea.setText(String.format("Bun venit, %s!\n", clientName));
                        weatherInfoArea.append("Locația curentă: " + locationResponse + "\n");
                        weatherInfoArea.append("Prognoza meteo: " + weatherResponse + "\n");
                        refreshUI();

                        // Afișează imaginea pentru condițiile meteo
                        displayWeatherImage(weatherResponse);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();


            // Întrebăm despre alte locații
            //handleOtherLocations(socket, out, in);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Eroare la conectarea cu serverul: " + ex.getMessage());
        }
    }

    private void handleOtherLocations(Socket socket, PrintWriter out, BufferedReader in) throws IOException {
        while (true) {
            int response = JOptionPane.showConfirmDialog(this, "Doriți să aflați informații despre alte locații?", "Other Locations", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.NO_OPTION) {
                JOptionPane.showMessageDialog(this, "Vă mulțumim că ați utilizat aplicația noastră!");
                break;
            }

            // Dacă utilizatorul dorește alte locații
            String location = JOptionPane.showInputDialog(this, "Introduceți numele locației:");
            if (location != null && !location.isEmpty()) {
                out.println(location); // Trimite locația la server

                String weatherResponse = in.readLine();
                if (weatherResponse.equals("Location not found.")) {
                    JOptionPane.showMessageDialog(this, "Locația nu a fost găsită. Introduceți coordonatele.");
                    String latitude = JOptionPane.showInputDialog(this, "Latitudine:");
                    String longitude = JOptionPane.showInputDialog(this, "Longitudine:");

                    out.println(latitude);
                    out.println(longitude);

                    weatherResponse = in.readLine(); // Primește locația cea mai apropiată
                }

                // Afișează informațiile despre vreme
                weatherInfoArea.append("\nInformații pentru noua locație:\n");
                weatherInfoArea.append(weatherResponse + "\n");
                refreshUI();

                // Afișează imaginea pentru condițiile meteo
                displayWeatherImage(weatherResponse);
            } else {
                JOptionPane.showMessageDialog(this, "Locație invalidă.");
            }
        }
    }

    private void displayWeatherImage(String weatherResponse) {
        if (weatherResponse.contains("Sunny")) {
            weatherImageLabel.setIcon(new ImageIcon("images/sunny.png"));
        } else if (weatherResponse.contains("Rainy")) {
            weatherImageLabel.setIcon(new ImageIcon("images/rainy.png"));
        } else if (weatherResponse.contains("Cloudy")) {
            weatherImageLabel.setIcon(new ImageIcon("images/cloudy.png"));
        } else {
            weatherImageLabel.setIcon(null); // Fără imagine
        }
    }

    private void refreshUI() {
        SwingUtilities.invokeLater(() -> {
            weatherInfoArea.repaint();
            weatherInfoArea.revalidate();
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WeatherAppGUI app = new WeatherAppGUI();
            app.setVisible(true);
        });
    }
}
