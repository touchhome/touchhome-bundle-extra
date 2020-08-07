package org.touchhome.bundle.weather.providers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.text.StringSubstitutor;
import org.touchhome.bundle.api.model.HasDescription;
import org.touchhome.bundle.api.util.Curl;
import org.touchhome.bundle.weather.WeatherProvider;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public abstract class BaseWeatherProvider<T> implements WeatherProvider<T>, HasDescription {
    protected final Map<String, OpenWeatherMapProvider.CityToGeoLocation> cityToGeoMap = new HashMap<>();

    private T data;
    private long lastRequestTimeout;
    private final Class<T> weatherJSONType;
    private final String url;

    /**
     * Read from weather provider not ofter than one minute
     */
    private synchronized T readJson(String city) {
        if (data == null || System.currentTimeMillis() - lastRequestTimeout > 60000) {
            CityToGeoLocation cityGeolocation = findCityGeolocation(city);
            data = Curl.get(buildWeatherRequest(city, cityGeolocation.latt, cityGeolocation.longt).replace(url), weatherJSONType);
            lastRequestTimeout = System.currentTimeMillis();
        }
        return data;
    }

    public T readWeather(String city) {
        return readJson(city);
    }

    // sync to avoid too many requests
    private CityToGeoLocation findCityGeolocation(String city) {
        if (!cityToGeoMap.containsKey(city)) {
            OpenWeatherMapProvider.CityToGeoLocation cityToGeoLocation = Curl.get("https://geocode.xyz/" + city + "?json=1", OpenWeatherMapProvider.CityToGeoLocation.class);
            if (cityToGeoLocation.error != null) {
                String error = cityToGeoLocation.error.description;
                if ("15. Your request did not produce any results.".equals(error)) {
                    error = "Unable to find city: " + city + ". Please, check city from site: https://geocode.xyz";
                }
                throw new IllegalArgumentException(error);
            }
            cityToGeoMap.put(city, cityToGeoLocation);
        }
        return cityToGeoMap.get(city);
    }

    protected abstract StringSubstitutor buildWeatherRequest(String city, String latt, String longt);

    @Setter
    @Getter
    public static class CityToGeoLocation {
        private String longt;
        private String latt;
        private Error error;

        @Setter
        private static class Error {
            private String description;
        }
    }
}
