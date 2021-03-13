package org.touchhome.bundle.nrf24i01.setting;

import org.touchhome.bundle.api.setting.SettingPlugin;

public class Nrf24i01EnableButtonsSetting implements SettingPlugin<Boolean> {

    @Override
    public UIFieldType getSettingType() {
        return UIFieldType.Boolean;
    }

    @Override
    public int order() {
        return 1;
    }
}
