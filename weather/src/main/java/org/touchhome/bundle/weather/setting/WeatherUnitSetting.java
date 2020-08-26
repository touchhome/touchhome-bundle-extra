package org.touchhome.bundle.weather.setting;

import org.apache.commons.lang3.StringUtils;
import org.touchhome.bundle.api.setting.BundleSettingPlugin;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.json.Option;

import java.util.List;

public class WeatherUnitSetting implements BundleSettingPlugin<WeatherUnitSetting.WeatherUnit> {

    @Override
    public SettingType getSettingType() {
        return SettingType.SelectBox;
    }

    @Override
    public int order() {
        return 80;
    }

    @Override
    public WeatherUnit parseValue(EntityContext entityContext, String value) {
        return StringUtils.isEmpty(value) ? null : WeatherUnit.valueOf(value);
    }

    @Override
    public String getDefaultValue() {
        return WeatherUnit.metric.name();
    }

    @Override
    public List<Option> loadAvailableValues(EntityContext entityContext) {
        return Option.list(WeatherUnit.class);
    }

    public enum WeatherUnit {
        metric, imperial
    }
}
