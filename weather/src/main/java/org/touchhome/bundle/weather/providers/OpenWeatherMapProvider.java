package org.touchhome.bundle.weather.providers;

import lombok.Setter;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Component;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.weather.setting.WeatherApiKeySetting;
import org.touchhome.bundle.weather.setting.WeatherLangSetting;
import org.touchhome.bundle.weather.setting.WeatherUnitSetting;

import java.util.HashMap;
import java.util.Map;

@Component
public class OpenWeatherMapProvider extends BaseWeatherProvider<OpenWeatherMapProvider.WeatherJSON> {
    private static final String URL = "https://api.openweathermap.org/data/2.5/onecall?lat=${lat}&lon=${lon}&appid=${key}&units=${unit}&lang=${lang}";
    private final EntityContext entityContext;

    public OpenWeatherMapProvider(EntityContext entityContext) {
        super(WeatherJSON.class, URL);
        this.entityContext = entityContext;
    }

    @Override
    public Double readWeatherTemperature(String city) {
        return this.readWeather(city).current.temp;
    }

    @Override
    public Double readWeatherHumidity(String city) {
        return this.readWeather(city).current.humidity;
    }

    @Override
    public Double readWeatherPressure(String city) {
        return this.readWeather(city).current.pressure;
    }

    @Override
    public String getDescription() {
        return "You has to acquire api key for provider<\br><a href='https://openweathermap.org/'>OpenWeather</a>";
    }

    @Setter
    public static class WeatherJSON {
        private Current current;

        @Setter
        private static class Current {
            private Double temp;
            private Double pressure;
            private Double humidity;
        }
    }

    @Override
    protected StringSubstitutor buildWeatherRequest(String city, String latt, String longt) {
        Map<String, String> valuesMap = new HashMap<>();

        valuesMap.put("lat", cityToGeoMap.get(city).getLatt());
        valuesMap.put("lon", cityToGeoMap.get(city).getLongt());
        valuesMap.put("unit", entityContext.getSettingValue(WeatherUnitSetting.class).name());
        valuesMap.put("key", entityContext.getSettingValue(WeatherApiKeySetting.class));
        valuesMap.put("lang", entityContext.getSettingValue(WeatherLangSetting.class));
        return new StringSubstitutor(valuesMap);
    }
}
