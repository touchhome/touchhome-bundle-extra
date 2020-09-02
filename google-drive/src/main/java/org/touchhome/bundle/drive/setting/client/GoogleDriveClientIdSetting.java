package org.touchhome.bundle.drive.setting.client;

import org.touchhome.bundle.api.setting.BundleSettingPlugin;

public class GoogleDriveClientIdSetting implements BundleSettingPlugin<String> {

    @Override
    public SettingType getSettingType() {
        return SettingType.Text;
    }

    @Override
    public int order() {
        return 20;
    }

    @Override
    public boolean isSecuredValue() {
        return true;
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public String group() {
        return "client-security";
    }
}
