package org.touchhome.bundle.mqtt_client.setting.advanced;

import org.touchhome.bundle.api.setting.BundleSettingPlugin;

public class MQTTAutomaticReconnectSetting implements BundleSettingPlugin<Boolean> {

    @Override
    public SettingType getSettingType() {
        return SettingType.Boolean;
    }

    @Override
    public String getDefaultValue() {
        return Boolean.TRUE.toString();
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
