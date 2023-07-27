package me.flashyreese.automapper.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Utils {
    // Helper method to fetch JSON data from a URL
    public static String fetchJsonFromUrl(String url) throws IOException {
        StringBuilder jsonData = new StringBuilder();

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonData.append(line);
            }
        }

        return jsonData.toString();
    }
}
