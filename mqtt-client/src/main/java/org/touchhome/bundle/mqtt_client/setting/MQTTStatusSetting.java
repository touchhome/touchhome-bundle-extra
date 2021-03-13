package org.touchhome.bundle.mqtt_client.setting;

import org.touchhome.bundle.api.setting.SettingPluginInfo;

public class MQTTStatusSetting implements SettingPluginInfo {

    @Override
    public int order() {
        return 600;
    }
}
