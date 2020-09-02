package org.touchhome.bundle.drive.setting.advanced;

import org.touchhome.bundle.api.setting.BundleSettingPlugin;

public class GoogleDriveCodeSetting implements BundleSettingPlugin<String> {

    @Override
    public SettingType getSettingType() {
        return SettingType.Text;
    }

    @Override
    public int order() {
        return 10;
    }

    @Override
    public boolean isAdvanced() {
        return true;
    }

    @Override
    public boolean isSecuredValue() {
        return true;
    }
}
