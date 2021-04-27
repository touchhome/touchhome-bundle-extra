package org.touchhome.bundle.ipscanner.setting;

import org.touchhome.bundle.api.setting.SettingPluginButton;

public class IpScannerHeaderStopButtonSetting implements SettingPluginButton {

    @Override
    public int order() {
        return 100;
    }

    @Override
    public String getIcon() {
        return "fas fa-stop-circle";
    }
}
