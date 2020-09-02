package org.touchhome.bundle.weather.workspace;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.scratch.*;
import org.touchhome.bundle.weather.WeatherBundleEntrypoint;
import org.touchhome.bundle.weather.setting.WeatherProviderSetting;

@Getter
@Component
public class Scratch3WeatherBlocks extends Scratch3ExtensionBlocks {

    public static final String CITY = "CITY";

    @JsonIgnore
    private final Scratch3Block weatherApi;

    @JsonIgnore
    private final Scratch3Block temperatureWeatherApi;
    private final Scratch3Block humidityWeatherApi;
    private final Scratch3Block pressureWeatherApi;

    public Scratch3WeatherBlocks(EntityContext entityContext, WeatherBundleEntrypoint weatherBundleEntrypoint) {
        super("#3B798C", entityContext, weatherBundleEntrypoint);

        this.weatherApi = Scratch3Block.ofEvaluate(4, "weather", BlockType.reporter, "Weather of city [CITY] (JSON)", this::readWeather);
        this.weatherApi.addArgument(CITY, ArgumentType.string);

        this.temperatureWeatherApi = Scratch3Block.ofEvaluate(4, "weather_temp", BlockType.reporter, "Weather temp of city [CITY]", this::readWeatherTemperature);
        this.temperatureWeatherApi.addArgument(CITY, ArgumentType.string);

        this.humidityWeatherApi = Scratch3Block.ofEvaluate(4, "weather_humidity", BlockType.reporter, "Weather humidity of city [CITY]", this::readWeatherHumidity);
        this.humidityWeatherApi.addArgument(CITY, ArgumentType.string);

        this.pressureWeatherApi = Scratch3Block.ofEvaluate(4, "weather_pressure", BlockType.reporter, "Weather pressure of city [CITY]", this::readWeatherPressure);
        this.pressureWeatherApi.addArgument(CITY, ArgumentType.string);

        this.postConstruct();
    }

    private Double readWeatherTemperature(WorkspaceBlock workspaceBlock) {
        return entityContext.getSettingValue(WeatherProviderSetting.class).readWeatherTemperature(workspaceBlock.getInputString(CITY));
    }

    private Double readWeatherPressure(WorkspaceBlock workspaceBlock) {
        return entityContext.getSettingValue(WeatherProviderSetting.class).readWeatherPressure(workspaceBlock.getInputString(CITY));
    }

    private Double readWeatherHumidity(WorkspaceBlock workspaceBlock) {
        return entityContext.getSettingValue(WeatherProviderSetting.class).readWeatherHumidity(workspaceBlock.getInputString(CITY));
    }

    private JSONObject readWeather(WorkspaceBlock workspaceBlock) {
        return entityContext.getSettingValue(WeatherProviderSetting.class).readWeatherJSON(workspaceBlock.getInputString(CITY));
    }
}
