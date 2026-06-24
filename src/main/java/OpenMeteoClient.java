import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenMeteoClient {
    private static final String WEATHER_URL = "https://api.open-meteo.com/v1/forecast";

    /**
     * Builds a URL for the Open-Meteo forecast API.
     *
     * @param latitude latitude from {@code -90} to {@code 90}
     * @param longitude longitude from {@code -180} to {@code 180}
     * @return URL containing {@code temperature_2m}, {@code relative_humidity_2m} and {@code wind_speed_10m}
     * @throws IllegalArgumentException when coordinates are outside the accepted range
     */
    public String buildWeatherUrl(double latitude, double longitude) {
        Location.validateLatitude(latitude);
        Location.validateLongitude(longitude);
        return WEATHER_URL
                + "?latitude=" + latitude
                + "&longitude=" + longitude
                + "&current=temperature_2m,relative_humidity_2m,wind_speed_10m"
                + "&timezone=auto";
    }

    public String fetchJson(String url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        // Send GET request
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Open-Meteo returned status " + response.statusCode());
        }
        return response.body();
    }

    public WeatherReading parseCurrentWeather(String locationId, String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("JSON cannot be blank.");
        }
        String currentJson = findCurrentObject(json);
        // Read fields from JSON
        Optional<String> timeResult = findString(currentJson, "time");
        if (timeResult.isEmpty()) {
            throw new IllegalArgumentException("Missing time.");
        }

        Optional<Double> temperatureResult = findDouble(currentJson, "temperature_2m");
        if (temperatureResult.isEmpty()) {
            throw new IllegalArgumentException("Missing temperature_2m.");
        }

        Optional<Integer> humidityResult = findInt(currentJson, "relative_humidity_2m");
        if (humidityResult.isEmpty()) {
            throw new IllegalArgumentException("Missing relative_humidity_2m.");
        }

        Optional<Double> windSpeedResult = findDouble(currentJson, "wind_speed_10m");
        if (windSpeedResult.isEmpty()) {
            throw new IllegalArgumentException("Missing wind_speed_10m.");
        }

        try {
            return new WeatherReading(locationId, LocalDateTime.parse(timeResult.get()), temperatureResult.get(),
                    humidityResult.get(), windSpeedResult.get());
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Weather JSON contains invalid values.", exception);
        }
    }

    private String findCurrentObject(String json) {
        // Use only current object
        // "current" = field name
        // \\s* = spaces
        // ([^}]*) = text until }
        Pattern pattern = Pattern.compile("\"current\"\\s*:\\s*\\{([^}]*)}");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Missing current object.");
    }

    private Optional<String> findString(String json, String fieldName) {
        // Find text value
        // \" = quote
        // \\s* = spaces
        // ([^\"]+) = text in quotes
        Pattern pattern = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }
        return Optional.empty();
    }

    private Optional<Double> findDouble(String json, String fieldName) {
        // Find decimal value
        // -? = optional minus
        // \\d+ = digits
        // (\\.\\d+)? = optional decimal part
        Pattern pattern = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*(-?\\d+(\\.\\d+)?)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return Optional.of(Double.parseDouble(matcher.group(1)));
        }
        return Optional.empty();
    }

    private Optional<Integer> findInt(String json, String fieldName) {
        // Find integer value
        // \\d+ = digits
        Pattern pattern = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return Optional.of(Integer.parseInt(matcher.group(1)));
        }
        return Optional.empty();
    }
}
