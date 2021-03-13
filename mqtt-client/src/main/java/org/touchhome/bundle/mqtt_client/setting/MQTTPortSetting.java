package org.touchhome.bundle.mqtt_client.setting;

import org.touchhome.bundle.api.setting.SettingPluginInteger;

public class MQTTPortSetting implements SettingPluginInteger {

    @Override
    public Integer getMin() {
        return 1;
    }

    @Override
    public Integer getMax() {
        return 65535;
    }

    @Override
    public int defaultValue() {
        return 1883;
    }

    @Override
    public int order() {
        return 200;
    }

    @Override
    public boolean isReverted() {
        return true;
    }
}
