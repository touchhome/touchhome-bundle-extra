package org.touchhome.bundle.mqtt_client.setting.advanced;

import org.touchhome.bundle.api.setting.SettingPluginBoolean;

public class MQTTAutomaticReconnectSetting implements SettingPluginBoolean {

    @Override
    public boolean defaultValue() {
        return true;
    }

    @Override
    public int order() {
        return 100;
    }

    @Override
    public boolean isAdvanced() {
        return true;
    }
}
