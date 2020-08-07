package org.touchhome.bundle.weather.setting;

import org.touchhome.bundle.api.BundleSettingPlugin;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.exception.NotFoundException;
import org.touchhome.bundle.api.json.Option;
import org.touchhome.bundle.weather.providers.OpenWeatherMapProvider;
import org.touchhome.bundle.weather.WeatherProvider;

import java.util.List;

public class WeatherProviderSetting implements BundleSettingPlugin<WeatherProvider> {

    @Override
    public String getDefaultValue() {
        return OpenWeatherMapProvider.class.getSimpleName();
    }

    @Override
    public SettingType getSettingType() {
        return SettingType.SelectBoxDynamic;
    }

    @Override
    public int order() {
        return 1;
    }

    @Override
    public List<Option> loadAvailableValues(EntityContext entityContext) {
        return Option.simpleNamelist(entityContext.getBeansOfType(WeatherProvider.class));
    }

    @Override
    public WeatherProvider parseValue(EntityContext entityContext, String value) {
        return entityContext.getBeansOfType(WeatherProvider.class).stream().filter(p -> p.getClass().getSimpleName().equals(value)).findAny()
                .orElseThrow(() -> new NotFoundException("Unable to find ssh provider with name: " + value));
    }
}
