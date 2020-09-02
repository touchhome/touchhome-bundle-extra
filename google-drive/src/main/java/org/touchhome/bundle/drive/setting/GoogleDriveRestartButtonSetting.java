package org.touchhome.bundle.drive.setting;

import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.setting.BundleSettingPluginButton;

public class GoogleDriveRestartButtonSetting implements BundleSettingPluginButton {

    @Override
    public int order() {
        return 100;
    }

    @Override
    public boolean isVisible(EntityContext entityContext) {
        return entityContext.getUser().isAdmin();
    }
}
