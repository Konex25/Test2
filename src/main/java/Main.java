import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Main {
    private static final double WINDY_THRESHOLD = 5.0;

    private Main() {
    }

    public static void main(String[] args) {
        OpenMeteoClient client = new OpenMeteoClient();
        WeatherAnalysisService analysisService = new WeatherAnalysisService();
        Repository<String, Location> locationsRepository = new Repository<>();
        List<WeatherReading> readings = new ArrayList<>();

        // Hardcoded cities
        locationsRepository.add(new Location("Wroclaw", "PL", 51.1079, 17.0385, "Europe/Warsaw"));
        locationsRepository.add(new Location("Warsaw", "PL", 52.2297, 21.0122, "Europe/Warsaw"));
        locationsRepository.add(new Location("Berlin", "DE", 52.5200, 13.4050, "Europe/Berlin"));

        try {
            // Download weather for each city
            for (Location location : locationsRepository.findAll()) {
                String url = client.buildWeatherUrl(location.getLatitude(), location.getLongitude());
                String json = client.fetchJson(url);
                readings.add(client.parseCurrentWeather(location.getId(), json));
            }
            printReport(analysisService, locationsRepository.findAll(), readings);
        } catch (IOException exception) {
            System.out.println("Weather service error: " + exception.getMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            System.out.println("Weather request was interrupted.");
        } catch (RuntimeException exception) {
            System.out.println("Application error: " + exception.getMessage());
        }
    }

    private static void printReport(WeatherAnalysisService service, List<Location> locations, List<WeatherReading> readings) {
        System.out.println("City Weather Monitoring System");
        System.out.println();

        // Print readings
        for (WeatherReading reading : readings) {
            Location location = findLocation(locations, reading.getLocationId());
            System.out.printf("%s (%s), %s: %.1f C, %d%% humidity, %.1f km/h wind%n",
                    location.getCityName(),
                    location.getCountryCode(),
                    reading.getObservationTime(),
                    reading.getTemperatureCelsius(),
                    reading.getRelativeHumidity(),
                    reading.getWindSpeedKmh());
        }

        System.out.println();
        // Print simple analysis
        Optional<String> warmestLocation = service.findWarmestLocation(readings);
        if (warmestLocation.isPresent()) {
            System.out.println("Warmest city: " + findLocation(locations, warmestLocation.get()).getCityName());
        }

        System.out.println();
        Optional<WeatherReading> warmestReading = service.findWarmestReading(readings);
        if (warmestReading.isPresent()) {
            WeatherReading reading = warmestReading.get();
            System.out.println("Warmest reading: " + reading.getLocationId() + " " + reading.getTemperatureCelsius() + " C");
        }

        System.out.println();
        Optional<WeatherReading> coldestReading = service.findColdestReading(readings);
        if (coldestReading.isPresent()) {
            WeatherReading reading = coldestReading.get();
            System.out.println("Coldest reading: " + reading.getLocationId() + " " + reading.getTemperatureCelsius() + " C");
        }

        System.out.println();
        System.out.println("Average temperature by location:");
        for (Map.Entry<String, Double> entry : service.calculateAverageTemperatureByLocation(readings).entrySet()) {
            System.out.printf("%s: %.1f C%n", entry.getKey(), entry.getValue());
        }

        System.out.println();
        System.out.println("Readings between 20.0 and 30.0 C:");
        for (WeatherReading reading : service.filterByTemperatureRange(readings, 20.0, 30.0)) {
            System.out.println(reading.getLocationId() + ": " + reading.getTemperatureCelsius() + " C");
        }

        System.out.println();
        System.out.println("Readings sorted by temperature:");
        for (WeatherReading reading : service.sortByTemperatureDescending(readings)) {
            System.out.println(reading.getLocationId() + ": " + reading.getTemperatureCelsius() + " C");
        }

        System.out.println();
        System.out.println("Windy readings above " + WINDY_THRESHOLD + " km/h:");
        List<WeatherReading> windyReadings = service.filterByMinimumWindSpeed(readings, WINDY_THRESHOLD);
        if (windyReadings.isEmpty()) {
            System.out.println("No windy readings.");
        }
        for (WeatherReading reading : windyReadings) {
            System.out.println(reading.getLocationId() + ": " + reading.getWindSpeedKmh() + " km/h");
        }
    }

    private static Location findLocation(List<Location> locations, String id) {
        // Match reading with city
        for (Location location : locations) {
            if (location.getId().equals(id)) {
                return location;
            }
        }
        throw new IllegalArgumentException("Location not found: " + id);
    }
}
