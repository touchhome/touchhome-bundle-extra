package org.touchhome.bundle.mqtt_client.setting;

import org.touchhome.bundle.api.setting.BundleSettingPlugin;

public class MQTTHostSetting implements BundleSettingPlugin<String> {

    @Override
    public SettingType getSettingType() {
        return SettingType.Text;
    }

    @Override
    public String getDefaultValue() {
        return "127.0.0.1";
    }

    @Override
    public int order() {
        return 100;
    }
}
