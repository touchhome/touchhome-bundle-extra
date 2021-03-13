package org.touchhome.bundle.mqtt_client.setting;

import org.touchhome.bundle.api.setting.SettingPluginText;

public class MQTTHostSetting implements SettingPluginText {

    @Override
    public String getDefaultValue() {
        return "127.0.0.1";
    }

    @Override
    public int order() {
        return 100;
    }
}
