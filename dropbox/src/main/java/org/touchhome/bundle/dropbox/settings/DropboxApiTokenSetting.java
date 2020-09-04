package org.touchhome.bundle.dropbox.settings;

import org.touchhome.bundle.api.setting.BundleSettingPlugin;

public class DropboxApiTokenSetting implements BundleSettingPlugin<String> {

    @Override
    public int order() {
        return 0;
    }

    @Override
    public SettingType getSettingType() {
        return SettingType.Text;
    }

    @Override
    public boolean isSecuredValue() {
        return true;
    }

    @Override
    public boolean isRequired() {
        return true;
    }
}
