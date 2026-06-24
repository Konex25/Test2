import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeatherSystemTest {
    private OpenMeteoClient client;
    private WeatherAnalysisService service;
    private List<WeatherReading> readings;

    @BeforeEach
    void setUp() {
        client = new OpenMeteoClient();
        service = new WeatherAnalysisService();
        readings = new ArrayList<>();
        readings.add(new WeatherReading("WROCLAW-PL", LocalDateTime.of(2026, 6, 24, 10, 0), 24.0, 55, 10.0));
        readings.add(new WeatherReading("WARSAW-PL", LocalDateTime.of(2026, 6, 24, 10, 0), 28.0, 45, 12.0));
        readings.add(new WeatherReading("BERLIN-DE", LocalDateTime.of(2026, 6, 24, 10, 0), 20.0, 60, 25.0));
    }

    // 1. ULR test
    @Test
    void buildWeatherUrlShouldContainCoordinatesAndVariables() {
        String url = client.buildWeatherUrl(51.1079, 17.0385);

        assertTrue(url.contains("latitude=51.1079"));
        assertTrue(url.contains("longitude=17.0385"));
        assertTrue(url.contains("temperature_2m"));
        assertTrue(url.contains("relative_humidity_2m"));
        assertTrue(url.contains("wind_speed_10m"));
    }

    // 2. JSON missing fields test
    @Test
    void parseCurrentWeatherShouldRejectJsonWithMissingFields() {
        String json = "{\"current\":{\"time\":\"2026-06-24T11:15\",\"temperature_2m\":24.6}}";

        assertThrows(IllegalArgumentException.class, () -> client.parseCurrentWeather("WROCLAW-PL", json));
    }

    // 3. Average temp test
    @Test
    void calculateAverageTemperatureShouldReturnAverage() {
        assertEquals(24.0, service.calculateAverageTemperature(readings), 0.001);
    }

    // 4. Warmest city test
    @Test
    void findWarmestLocationShouldReturnWarsaw() {
        assertEquals("WARSAW-PL", service.findWarmestLocation(readings).orElseThrow());
    }

    // 5. Repository insert location test
    @Test
    void repositoryShouldAddAndFindLocation() {
        Repository<String, Location> repository = new Repository<>();
        Location location = new Location("Wroclaw", "PL", 51.1079, 17.0385, "Europe/Warsaw");

        repository.add(location);

        assertTrue(repository.findById("WROCLAW-PL").isPresent());
    }

    // 6. Minimum wind speed test
    @Test
    void filterByMinimumWindSpeedShouldReturnOnlyWindyReadings() {
        List<WeatherReading> windyReadings = service.filterByMinimumWindSpeed(readings, 20.0);

        assertEquals(1, windyReadings.size());
        assertEquals("BERLIN-DE", windyReadings.get(0).getLocationId());
    }
}
