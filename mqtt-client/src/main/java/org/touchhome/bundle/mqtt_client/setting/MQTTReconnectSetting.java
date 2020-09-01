package org.touchhome.bundle.mqtt_client.setting;

import org.touchhome.bundle.api.setting.BundleSettingPlugin;

public class MQTTReconnectSetting implements BundleSettingPlugin<Void> {

    @Override
    public SettingType getSettingType() {
        return SettingType.Button;
    }

    @Override
    public int order() {
        return 500;
    }
}
