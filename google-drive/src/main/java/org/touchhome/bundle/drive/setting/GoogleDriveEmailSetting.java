package org.touchhome.bundle.drive.setting;

import org.touchhome.bundle.api.setting.BundleSettingPlugin;

public class GoogleDriveEmailSetting implements BundleSettingPlugin<String> {

    @Override
    public SettingType getSettingType() {
        return SettingType.Text;
    }

    @Override
    public int order() {
        return 10;
    }

    @Override
    public boolean isSecuredValue() {
        return true;
    }
}
