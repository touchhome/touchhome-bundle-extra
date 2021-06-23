package org.touchhome.bundle.dropbox;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.SpaceUsage;
import com.google.common.primitives.Bytes;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.entity.storage.CachedFileSystem;
import org.touchhome.bundle.api.entity.storage.VendorFileSystem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class DropboxFileSystem extends VendorFileSystem<DbxClientV2, DropboxFileSystem.DropboxCacheFileSystem, DropboxEntity> {
    private static final DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox").build();

    private SpaceUsage spaceUsage;
    private long aboutSpaceUsage;

    public DropboxFileSystem(DropboxEntity dropboxEntity, EntityContext entityContext) {
        super(dropboxEntity, entityContext);
        this.dispose();
    }

    @Override
    protected void onEntityUpdated() {

    }

    @SneakyThrows
    public SpaceUsage getAbout() {
        if (spaceUsage == null || System.currentTimeMillis() - aboutSpaceUsage > 600000) {
            spaceUsage = getDrive().users().getSpaceUsage();
            aboutSpaceUsage = System.currentTimeMillis();
        }
        return spaceUsage;
    }

    public void dispose() {
        this.setDrive(null);
        this.setRoot(new DropboxCacheFileSystem(new DropboxFile(new Metadata("")), null));
    }

    @SneakyThrows
    @Override
    protected DbxClientV2 buildDrive() {
        DbxClientV2 client = new DbxClientV2(config, getEntity().getDropboxApiToken().asString());
        client.users().getCurrentAccount();
        return client;
    }

    @Override
    public long getTotalSpace() {
        return getAbout().getAllocation().getIndividualValue().getAllocated();
    }

    @Override
    public long getUsedSpace() {
        return getAbout().getUsed();
    }

    @SneakyThrows
    @Override
    public void upload(String[] parentPath, String fileName, byte[] content, boolean append) {
        String path = StringUtils.join(parentPath, "/") + "/" + fileName;
        if (append) {
            byte[] prependContent = getRoot().findFileByPath(path).download(getDrive());
            content = Bytes.concat(prependContent, content);
        }
        getDrive().files().uploadBuilder(path).uploadAndFinish(new ByteArrayInputStream(content));
    }

    @SneakyThrows
    @Override
    public boolean delete(String[] path) {
        DropboxCacheFileSystem file = getRoot().findFileById(path[path.length - 1]);
        String pathStr = file == null ? pathToString(Paths.get("", path)) : file.getSource().getId();
        getDrive().files().deleteV2(pathStr);
        return true;
    }

    public static class DropboxCacheFileSystem extends CachedFileSystem<DropboxCacheFileSystem, DropboxFile, DbxClientV2> {

        public DropboxCacheFileSystem(DropboxFile source, DropboxCacheFileSystem parent) {
            super(source, parent, false);
        }

        @SneakyThrows
        @Override
        protected DropboxFile readFileFromServer(DbxClientV2 driver) {
            return new DropboxFile(driver.files().getMetadata(getSource().getId()));
        }

        @Override
        protected DropboxCacheFileSystem newInstance(DropboxFile source, DropboxCacheFileSystem parent) {
            return new DropboxCacheFileSystem(source, parent);
        }

        @SneakyThrows
        @Override
        protected Collection<DropboxFile> searchForChildren(DropboxFile serverSource, DbxClientV2 driver) {
            ListFolderResult result = driver.files().listFolder(serverSource.getId());
            Collection<DropboxFile> files = new ArrayList<>();
            while (true) {
                files.addAll(result.getEntries().stream().map(DropboxFile::new).collect(Collectors.toList()));
                if (!result.getHasMore()) {
                    break;
                }
                result = driver.files().listFolderContinue(result.getCursor());
            }
            return files;
        }

        @Override
        @SneakyThrows
        protected byte[] downloadContent(DbxClientV2 drive) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            drive.files().download(this.getSource().getId()).download(stream);
            return stream.toByteArray();
        }
    }

    public static String pathToString(Path path) {
        String pathStr = path.toString().replace("\\", "/");
        return pathStr.equals("/") ? "" : pathStr;
    }

    private static class DropboxFile implements CachedFileSystem.SourceFileCapability {
        private final Metadata file;
        private long lasModified;

        public DropboxFile(Metadata file) {
            this.file = file;
            lasModified = file.getName().equals("") ? 0 : System.currentTimeMillis();
        }

        @Override
        public String getId() {
            return file instanceof FolderMetadata ? ((FolderMetadata) file).getId() :
                    file instanceof FileMetadata ? ((FileMetadata) file).getId() : file.getName();
        }

        @Override
        public String getName() {
            return file.getName();
        }

        @Override
        public long getLastModifiedTime() {
            if (file instanceof FileMetadata) {
                return ((FileMetadata) file).getServerModified().getTime();
            }
            return lasModified;
        }

        @Override
        public void setLastModifiedTime(long time) {
            lasModified = time;
        }

        @Override
        public boolean isFolder() {
            return file instanceof FolderMetadata;
        }
    }
}
