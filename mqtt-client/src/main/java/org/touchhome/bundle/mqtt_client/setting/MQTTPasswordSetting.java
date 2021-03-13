package org.touchhome.bundle.mqtt_client.setting;

import org.touchhome.bundle.api.setting.SettingPluginText;

public class MQTTPasswordSetting implements SettingPluginText {

    @Override
    public int order() {
        return 400;
    }

    @Override
    public boolean isSecuredValue() {
        return true;
    }
}
