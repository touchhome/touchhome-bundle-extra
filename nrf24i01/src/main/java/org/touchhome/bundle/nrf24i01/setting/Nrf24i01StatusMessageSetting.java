package org.touchhome.bundle.nrf24i01.setting;

import org.touchhome.bundle.api.setting.BundleSettingPlugin;

public class Nrf24i01StatusMessageSetting implements BundleSettingPlugin<String> {
    @Override
    public SettingType getSettingType() {
        return SettingType.Info;
    }

    @Override
    public int order() {
        return 3;
    }
}
