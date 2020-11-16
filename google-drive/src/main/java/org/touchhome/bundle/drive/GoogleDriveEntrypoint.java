package org.touchhome.bundle.drive;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.touchhome.bundle.api.BundleEntrypoint;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.setting.BundleSettingPluginStatus;
import org.touchhome.bundle.drive.setting.GoogleDriveRestartButtonSetting;
import org.touchhome.bundle.drive.setting.GoogleDriveStatusSetting;

@Log4j2
@Component
@RequiredArgsConstructor
public class GoogleDriveEntrypoint implements BundleEntrypoint {

    @Getter
    private final GoogleDriveFileSystem googleDriveFileSystem;
    private final EntityContext entityContext;

    @Override
    public void init() {
        restart();
        entityContext.setting().listenValue(GoogleDriveRestartButtonSetting.class, "google-drive-restart", this::restart);
    }

    private void restart() {
        try {
            googleDriveFileSystem.invalidate();
            googleDriveFileSystem.getFileByName("root");
            entityContext.setting().setValue(GoogleDriveStatusSetting.class, BundleSettingPluginStatus.ONLINE);
        } catch (Exception ex) {
            entityContext.setting().setValue(GoogleDriveStatusSetting.class, BundleSettingPluginStatus.error(ex));
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
    public Class<? extends BundleSettingPluginStatus> getBundleStatusSetting() {
        return GoogleDriveStatusSetting.class;
    }
}
