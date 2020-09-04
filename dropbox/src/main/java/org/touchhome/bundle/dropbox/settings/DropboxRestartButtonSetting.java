package org.touchhome.bundle.dropbox.settings;

import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.setting.BundleSettingPluginButton;

public class DropboxRestartButtonSetting implements BundleSettingPluginButton {

    @Override
    public int order() {
        return 200;
    }

    @Override
    public boolean isVisible(EntityContext entityContext) {
        return entityContext.getUser().isAdmin();
    }
}
