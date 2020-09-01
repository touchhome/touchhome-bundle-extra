package org.touchhome.bundle.mqtt_client.setting.advanced;

import org.touchhome.bundle.api.setting.BundleSettingPlugin;

public class MQTTCleanSessionOnConnectSetting implements BundleSettingPlugin<Boolean> {

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
        return 300;
    }

    @Override
    public boolean isAdvanced() {
        return true;
    }
}
