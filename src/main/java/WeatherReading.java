import java.time.LocalDateTime;

public class WeatherReading {

    private final String locationId;
    private final LocalDateTime observationTime;
    private final double temperatureCelsius;
    private final int relativeHumidity;
    private final double windSpeedKmh;

    public WeatherReading(String locationId, LocalDateTime observationTime, double temperatureCelsius,
                          int relativeHumidity, double windSpeedKmh) {
        // Check wrong values
        if (locationId == null || locationId.isBlank()) {
            throw new IllegalArgumentException("Location id cannot be blank.");
        }
        if (observationTime == null) {
            throw new IllegalArgumentException("Observation time cannot be null.");
        }
        if (relativeHumidity < 0 || relativeHumidity > 100) {
            throw new IllegalArgumentException("Relative humidity must be between 0 and 100.");
        }
        if (windSpeedKmh < 0) {
            throw new IllegalArgumentException("Wind speed cannot be negative.");
        }
        this.locationId = locationId;
        this.observationTime = observationTime;
        this.temperatureCelsius = temperatureCelsius;
        this.relativeHumidity = relativeHumidity;
        this.windSpeedKmh = windSpeedKmh;
    }

    // getters
    public String getLocationId() {
        return locationId;
    }

    public LocalDateTime getObservationTime() {
        return observationTime;
    }

    public double getTemperatureCelsius() {
        return temperatureCelsius;
    }

    public int getRelativeHumidity() {
        return relativeHumidity;
    }

    public double getWindSpeedKmh() {
        return windSpeedKmh;
    }
}
