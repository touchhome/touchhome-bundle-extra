package org.touchhome.bundle.drive.setting;

import org.touchhome.bundle.api.setting.BundleSettingPluginStatus;

public class GoogleDriveStatusSetting implements BundleSettingPluginStatus {

    @Override
    public int order() {
        return 120;
    }
}
