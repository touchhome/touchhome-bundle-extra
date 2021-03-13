package org.touchhome.bundle.nrf24i01.setting;

import org.touchhome.bundle.api.setting.SettingPluginStatus;

public class Nrf24i01StatusSetting implements SettingPluginStatus {
    @Override
    public int order() {
        return 2;
    }
}
