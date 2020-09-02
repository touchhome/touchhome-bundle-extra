package org.touchhome.bundle.drive.setting.advanced;

import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.setting.BundleSettingPlugin;

public class GoogleDriveAccessTokenSetting implements BundleSettingPlugin<String> {

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
    public boolean isDisabled(EntityContext entityContext) {
        return true;
    }

    @Override
    public boolean isAdvanced() {
        return true;
    }
}
