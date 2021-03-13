package org.touchhome.bundle.mqtt_client.setting.advanced;

import org.touchhome.bundle.api.setting.SettingPluginText;

public class MQTTClientIDSetting implements SettingPluginText {

    @Override
    public int order() {
        return 500;
    }

    @Override
    public boolean isAdvanced() {
        return true;
    }
}
