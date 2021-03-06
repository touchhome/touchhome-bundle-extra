package org.touchhome.bundle.mqtt_client.setting.advanced;

import org.touchhome.bundle.api.setting.SettingPluginInteger;

public class MQTTReconnectTimeSetting implements SettingPluginInteger {

    @Override
    public int defaultValue() {
        return 60;
    }

    @Override
    public int order() {
        return 200;
    }

    @Override
    public Integer getMin() {
        return 10;
    }

    @Override
    public Integer getMax() {
        return 600;
    }

    @Override
    public boolean isAdvanced() {
        return true;
    }

    @Override
    public boolean isReverted() {
        return true;
    }
}
