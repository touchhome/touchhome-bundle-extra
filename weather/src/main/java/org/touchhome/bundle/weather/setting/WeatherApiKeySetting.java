package org.touchhome.bundle.weather.setting;

import org.touchhome.bundle.api.setting.BundleSettingPlugin;

public class WeatherApiKeySetting implements BundleSettingPlugin<String> {

    @Override
    public SettingType getSettingType() {
        return SettingType.Text;
    }

    @Override
    public int order() {
        return 10;
    }
}
