package org.touchhome.bundle.drive;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.touchhome.bundle.api.BundleEntrypoint;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.json.NotificationEntityJSON;
import org.touchhome.bundle.api.util.NotificationType;
import org.touchhome.bundle.api.util.TouchHomeUtils;
import org.touchhome.bundle.drive.setting.GoogleDriveRestartButtonSetting;
import org.touchhome.bundle.drive.setting.GoogleDriveStatusSetting;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Log4j2
@Component
@RequiredArgsConstructor
public class GoogleDriveEntrypoint implements BundleEntrypoint {
    private static final String CONNECTED = "Connected.";

    @Getter
    private final GoogleDriveFileSystem googleDriveFileSystem;
    private final EntityContext entityContext;

    @Override
    public void init() {
        checkGoogleDriveAccess();
        entityContext.listenSettingValue(GoogleDriveRestartButtonSetting.class, this::checkGoogleDriveAccess);
    }

    private void checkGoogleDriveAccess() {
        try {
            googleDriveFileSystem.invalidate();
            googleDriveFileSystem.getFileByName("root");
            entityContext.setSettingValue(GoogleDriveStatusSetting.class, CONNECTED);
        } catch (Exception ex) {
            entityContext.setSettingValue(GoogleDriveStatusSetting.class, TouchHomeUtils.getErrorMessage(ex));
        }
    }

    @Override
    public String getBundleId() {
        return "drive";
    }

    @Override
    public int order() {
        return 2100;
    }

    @Override
    public BundleImageColorIndex getBundleImageColorIndex() {
        return BundleImageColorIndex.ONE;
    }

    @Override
    public Set<NotificationEntityJSON> getNotifications() {
        String value = entityContext.getSettingValue(GoogleDriveStatusSetting.class);
        return new HashSet<>(Collections.singletonList(new NotificationEntityJSON("drive-status")
                .setNotificationType(!value.equals(CONNECTED) ? NotificationType.danger : NotificationType.success)
                .setName("Google Drive: " + value)));
    }
}
