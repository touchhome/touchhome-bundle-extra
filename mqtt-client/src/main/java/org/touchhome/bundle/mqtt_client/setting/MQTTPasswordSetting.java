package org.touchhome.bundle.mqtt_client.setting;

import org.touchhome.bundle.api.setting.BundleSettingPlugin;

public class MQTTPasswordSetting implements BundleSettingPlugin<String> {

    @Override
    public SettingType getSettingType() {
        return SettingType.Text;
    }

    @Override
    public int order() {
        return 400;
    }

    @Override
    public boolean isSecuredValue() {
        return true;
    }
}
