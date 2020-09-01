package org.touchhome.bundle.mqtt_client.setting;

import org.touchhome.bundle.api.setting.BundleSettingPlugin;

public class MQTTStatusSetting implements BundleSettingPlugin<String> {

    @Override
    public SettingType getSettingType() {
        return SettingType.Info;
    }

    @Override
    public int order() {
        return 600;
    }
}
