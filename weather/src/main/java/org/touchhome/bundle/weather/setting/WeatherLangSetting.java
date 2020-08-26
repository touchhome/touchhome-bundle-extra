package org.touchhome.bundle.weather.setting;

import org.touchhome.bundle.api.setting.BundleSettingPlugin;

public class WeatherLangSetting implements BundleSettingPlugin<String> {

    @Override
    public SettingType getSettingType() {
        return SettingType.Text;
    }

    @Override
    public String getDefaultValue() {
        return "en";
    }

    @Override
    public int order() {
        return 60;
    }
}
