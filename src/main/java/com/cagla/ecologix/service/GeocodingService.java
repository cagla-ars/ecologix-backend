package com.cagla.ecologix.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

@Service
public class GeocodingService {

    public double[] getCoordinates(String address, String city) {

        try {

            String query =
                    URLEncoder.encode(address + " " + city + " Turkey", "UTF-8");

            String urlString =
                    "https://nominatim.openstreetmap.org/search?q="
                            + query
                            + "&format=json&limit=1";

            URL url = new URL(urlString);

            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            connection.setRequestProperty(
                    "User-Agent",
                    "EcoLogix"
            );

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(connection.getInputStream())
                    );

            StringBuilder response = new StringBuilder();

            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();

            JSONArray array =
                    new JSONArray(response.toString());

            if (array.isEmpty()) {

                return new double[]{0.0, 0.0};
            }

            JSONObject location = array.getJSONObject(0);

            double lat =
                    Double.parseDouble(location.getString("lat"));

            double lon =
                    Double.parseDouble(location.getString("lon"));

            return new double[]{lat, lon};

        } catch (Exception e) {

            e.printStackTrace();

            return new double[]{0.0, 0.0};
        }
    }
}