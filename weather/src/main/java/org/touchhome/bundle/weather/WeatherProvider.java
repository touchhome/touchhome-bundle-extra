package org.touchhome.bundle.weather;

import org.json.JSONObject;

public interface WeatherProvider<T> {
    T readWeather(String city);

    default JSONObject readWeatherJSON(String city) {
        return new JSONObject(readWeather(city));
    }

    Double readWeatherTemperature(String city);

    Double readWeatherHumidity(String city);

    Double readWeatherPressure(String city);
}
