public class Location implements Identifiable<String> {
    private final String id;
    private final String cityName;
    private final String countryCode;
    private final double latitude;
    private final double longitude;
    private final String timezone;

    public Location(String cityName, String countryCode, double latitude, double longitude, String timezone) {
        // Validate and save data
        this.cityName = requireNotBlank(cityName, "City name").trim();
        this.countryCode = normalizeCountryCode(countryCode);
        validateLatitude(latitude);
        validateLongitude(longitude);
        this.latitude = latitude;
        this.longitude = longitude;
        this.timezone = requireNotBlank(timezone, "Timezone").trim();
        this.id = buildId(this.cityName, this.countryCode);
    }

    // Getters
    @Override
    public String getId() {
        return id;
    }

    public String getCityName() {
        return cityName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getTimezone() {
        return timezone;
    }

    // Validation functions
    public static void validateLatitude(double latitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90.");
        }
    }

    public static void validateLongitude(double longitude) {
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180.");
        }
    }

    public static String normalizeCountryCode(String countryCode) {
        String value = requireNotBlank(countryCode, "Country code").trim().toUpperCase();
        if (!value.matches("[A-Z]{2}")) {
            throw new IllegalArgumentException("Country code must contain exactly two letters.");
        }
        return value;
    }

    private static String buildId(String cityName, String countryCode) {
        return cityName.trim().toUpperCase().replaceAll("\\s+", "_") + "-" + countryCode;
    }

    private static String requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank.");
        }
        return value;
    }
}
