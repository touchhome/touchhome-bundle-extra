package org.touchhome.bundle.dropbox.settings;

import org.touchhome.bundle.api.setting.BundleSettingPluginStatus;

public class DropboxStatusSetting implements BundleSettingPluginStatus {

    @Override
    public int order() {
        return 100;
    }
}
