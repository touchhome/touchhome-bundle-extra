package org.touchhome.bundle.dropbox;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.touchhome.bundle.api.BundleEntrypoint;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.setting.BundleSettingPluginStatus;
import org.touchhome.bundle.dropbox.settings.DropboxApiTokenSetting;
import org.touchhome.bundle.dropbox.settings.DropboxRestartButtonSetting;
import org.touchhome.bundle.dropbox.settings.DropboxStatusSetting;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@Component
@RequiredArgsConstructor
public class DropboxEntrypoint implements BundleEntrypoint {
    private final EntityContext entityContext;
    private final DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox").build();

    private long fileCacheTime = 0;
    private Map<String, Metadata> fileCache = new HashMap<>();

    @Getter
    private DbxClientV2 client;

    public void init() {
        restart();
        entityContext.listenSettingValue(DropboxRestartButtonSetting.class, this::restart);
    }

    @Override
    public Class<? extends BundleSettingPluginStatus> getBundleStatusSetting() {
        return DropboxStatusSetting.class;
    }

    private void restart() {
        try {
            this.client = new DbxClientV2(config, entityContext.getSettingValue(DropboxApiTokenSetting.class));
            client.users().getCurrentAccount();
            entityContext.setSettingValue(DropboxStatusSetting.class, BundleSettingPluginStatus.ONLINE);
        } catch (Exception ex) {
            entityContext.setSettingValue(DropboxStatusSetting.class, BundleSettingPluginStatus.error(ex));
        }
    }

    @Override
    public String getBundleId() {
        return "dropbox";
    }

    @Override
    public int order() {
        return 3000;
    }

    @SneakyThrows
    public Map<String, Metadata> getFiles() {
        if (System.currentTimeMillis() - fileCacheTime > 60000) {
            fileCacheTime = System.currentTimeMillis();
            fileCache.clear();
            fetchFolder("");
        }
        return fileCache;
    }

    private void fetchFolder(String path) throws DbxException {
        ListFolderResult result = client.files().listFolder(path);
        while (true) {
            for (Metadata metadata : result.getEntries()) {
                fileCache.put(metadata.getName(), metadata);
                if (metadata instanceof FolderMetadata) {
                    fetchFolder(((FolderMetadata) metadata).getId());
                }
            }
            if (!result.getHasMore()) {
                break;
            }
            result = client.files().listFolderContinue(result.getCursor());
        }
    }
}
