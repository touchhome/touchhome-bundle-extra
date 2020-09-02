package org.touchhome.bundle.drive.setting;

import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.json.NotificationEntityJSON;
import org.touchhome.bundle.api.setting.BundleSettingPlugin;
import org.touchhome.bundle.api.util.NotificationType;

public class GoogleDriveStatusSetting implements BundleSettingPlugin<String> {

    @Override
    public SettingType getSettingType() {
        return SettingType.Info;
    }

    @Override
    public int order() {
        return 120;
    }

    @Override
    public NotificationEntityJSON buildToastrNotificationEntity(String value, EntityContext entityContext) {
        return new NotificationEntityJSON("google-drive-status")
                .setNotificationType(value.toLowerCase().contains("error") ? NotificationType.danger : NotificationType.success)
                .setName("Google Drive: " + value);
    }
}
