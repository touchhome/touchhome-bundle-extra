package org.touchhome.bundle.mqtt_client.setting.advanced;

import org.touchhome.bundle.api.setting.BundleSettingPluginInteger;

public class MQTTConnectionTimeoutSetting implements BundleSettingPluginInteger {

    @Override
    public int defaultValue() {
        return 30;
    }

    @Override
    public int getMin() {
        return 10;
    }

    @Override
    public int getMax() {
        return 600;
    }

    @Override
    public int order() {
        return 200;
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