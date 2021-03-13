package org.touchhome.bundle.mqtt_client.setting;

import org.touchhome.bundle.api.setting.SettingPluginButton;

public class MQTTReconnectSetting implements SettingPluginButton {

    @Override
    public int order() {
        return 500;
    }

    @Override
    public String getIcon() {
        return "fas fa-plug";
    }
}
