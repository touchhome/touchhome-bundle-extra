package org.touchhome.bundle.nrf24i01.setting;

import org.touchhome.bundle.api.setting.BundleSettingPluginStatus;

public class Nrf24i01StatusSetting implements BundleSettingPluginStatus {
    @Override
    public int order() {
        return 2;
    }
}
