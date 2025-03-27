# WeatherApp

**Weather Forecast System – Java Client-Server Application**

This project is a Java-based client-server application designed to manage and display weather forecasts for various locations. The system provides two main modes: regular user mode and administrator mode.

## Features

- Client-server architecture using sockets  
- User management via a simple client login system  
- Real-time weather query based on the user's predefined location (loaded from JSON)  

### Admin capabilities:
- Add or modify weather data for existing or new locations  
- Input future forecasts and coordinates  

### Automatic location handling:
- If a user's location is not found, the system asks for coordinates and suggests the nearest location  

- Persistent storage using SQLite (`weather.db`)  
- Data input through structured JSON files  

## Technologies used
- Java (Standard Edition)  
- Java Sockets  
- SQLite  
- JSON handling (manual parsing)  
- IntelliJ IDEA (project structure includes `.idea/`)  

## Structure

- `ClientHandler.java`: Manages interaction with connected clients  
- `WeatherDataHandler.java`: Responsible for loading/saving weather data and location matching  
- `client.json`: Stores user names and associated locations  
- `weather.db`: Stores weather information in a persistent format  
