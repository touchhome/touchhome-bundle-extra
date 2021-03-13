package org.touchhome.bundle.mqtt_client.setting;

import org.touchhome.bundle.api.setting.SettingPluginText;

public class MQTTUsernameSetting implements SettingPluginText {

    @Override
    public int order() {
        return 300;
    }
}
