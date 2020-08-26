package org.touchhome.bundle.nrf24i01.setting;

import org.touchhome.bundle.api.setting.BundleSettingPlugin;

public class Nrf24i01EnableButtonsSetting implements BundleSettingPlugin<Boolean> {

    @Override
    public SettingType getSettingType() {
        return SettingType.Boolean;
    }

    @Override
    public int order() {
        return 1;
    }
}
