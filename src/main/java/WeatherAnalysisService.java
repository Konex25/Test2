import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WeatherAnalysisService {
    public WeatherAnalysisService() {
    }

    // 1. Group readings by location id
    public Map<String, List<WeatherReading>> groupByLocation(List<WeatherReading> readings) {
        requireList(readings);
        Map<String, List<WeatherReading>> result = new LinkedHashMap<>();
        // Group by location id
        for (WeatherReading reading : readings) {
            String locationId = reading.getLocationId();
            if (!result.containsKey(locationId)) {
                result.put(locationId, new ArrayList<>());
            }
            result.get(locationId).add(reading);
        }
        return result;
    }

    // 2. Average temp for non-empty
    public double calculateAverageTemperature(List<WeatherReading> readings) {
        requireNonEmpty(readings);
        double sum = 0;
        // Sum temperatures
        for (WeatherReading reading : readings) {
            sum += reading.getTemperatureCelsius();
        }
        return sum / readings.size();
    }

    // 3. Average temp by location
    public Map<String, Double> calculateAverageTemperatureByLocation(List<WeatherReading> readings) {
        Map<String, List<WeatherReading>> grouped = groupByLocation(readings);
        Map<String, Double> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<WeatherReading>> entry : grouped.entrySet()) {
            result.put(entry.getKey(), calculateAverageTemperature(entry.getValue()));
        }
        return result;
    }

    // 4.a Find the warmest reading
    public Optional<WeatherReading> findWarmestReading(List<WeatherReading> readings) {
        requireList(readings);
        if (readings.isEmpty()) {
            return Optional.empty();
        }
        WeatherReading warmest = readings.get(0);
        for (WeatherReading reading : readings) {
            if (reading.getTemperatureCelsius() > warmest.getTemperatureCelsius()) {
                warmest = reading;
            }
        }
        return Optional.of(warmest);
    }

    // 4.b Find the coldest reading
    public Optional<WeatherReading> findColdestReading(List<WeatherReading> readings) {
        requireList(readings);
        if (readings.isEmpty()) {
            return Optional.empty();
        }
        WeatherReading coldest = readings.get(0);
        for (WeatherReading reading : readings) {
            if (reading.getTemperatureCelsius() < coldest.getTemperatureCelsius()) {
                coldest = reading;
            }
        }
        return Optional.of(coldest);
    }

    // 5. Filter by temp. range
    public List<WeatherReading> filterByTemperatureRange(List<WeatherReading> readings, double minimum, double maximum) {
        requireList(readings);
        if (minimum > maximum) {
            throw new IllegalArgumentException("Minimum temperature cannot be greater than maximum.");
        }
        List<WeatherReading> result = new ArrayList<>();
        // Keep temperatures in range
        for (WeatherReading reading : readings) {
            double temperature = reading.getTemperatureCelsius();
            if (temperature >= minimum && temperature <= maximum) {
                result.add(reading);
            }
        }
        return result;
    }

    // 6. Filtering by minimum wind speed
    public List<WeatherReading> filterByMinimumWindSpeed(List<WeatherReading> readings, double minimumWindSpeed) {
        requireList(readings);
        if (minimumWindSpeed < 0) {
            throw new IllegalArgumentException("Minimum wind speed cannot be negative.");
        }
        List<WeatherReading> result = new ArrayList<>();
        // Keep windy readings
        for (WeatherReading reading : readings) {
            if (reading.getWindSpeedKmh() > minimumWindSpeed) {
                result.add(reading);
            }
        }
        return result;
    }

    // 7. Sort readings descending (without modifying orign. list)
    public List<WeatherReading> sortByTemperatureDescending(List<WeatherReading> readings) {
        requireList(readings);
        // Sort copy, not original list
        List<WeatherReading> result = new ArrayList<>(readings);
        for (int i = 0; i < result.size(); i++) {
            for (int j = i + 1; j < result.size(); j++) {
                if (result.get(j).getTemperatureCelsius() > result.get(i).getTemperatureCelsius()) {
                    WeatherReading temp = result.get(i);
                    result.set(i, result.get(j));
                    result.set(j, temp);
                }
            }
        }
        return result;
    }

    // 8. Find the location with the highest average temp
    public Optional<String> findWarmestLocation(List<WeatherReading> readings) {
        Map<String, Double> averages = calculateAverageTemperatureByLocation(readings);
        String bestLocation = null;
        double bestAverage = Double.NEGATIVE_INFINITY;
        // Pick highest average
        for (Map.Entry<String, Double> entry : averages.entrySet()) {
            if (entry.getValue() > bestAverage) {
                bestAverage = entry.getValue();
                bestLocation = entry.getKey();
            }
        }
        return Optional.ofNullable(bestLocation);
    }

    private void requireList(List<WeatherReading> readings) {
        if (readings == null) {
            throw new IllegalArgumentException("Readings cannot be null.");
        }
    }

    private void requireNonEmpty(List<WeatherReading> readings) {
        requireList(readings);
        if (readings.isEmpty()) {
            throw new IllegalArgumentException("Cannot calculate average for an empty list.");
        }
    }
}
