package org.touchhome.bundle.ftp;

import com.google.common.primitives.Bytes;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.fs.CachedFileSystem;
import org.touchhome.bundle.api.fs.VendorFileSystem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FtpFileSystem extends VendorFileSystem<FtpEntity, FtpFileSystem.FTPCacheFileSystem, FtpEntity> {

    public FtpFileSystem(FtpEntity entity, EntityContext entityContext) {
        super(entity, entityContext);
        this.setDrive(entity);
        this.dispose();
    }

    @Override
    protected void onEntityUpdated() {
        if (!Objects.equals(this.getEntity().getUrl(), getRoot().getSource().file.getUser())) {
            this.dispose();
        }
    }

    public void dispose() {
        FTPFile ftpFile = new FTPFile();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(0);
        ftpFile.setTimestamp(calendar);
        ftpFile.setName("");
        ftpFile.setUser(getEntity().getUrl());

        setRoot(new FTPCacheFileSystem(new FTPCacheFileSource(ftpFile), null));
    }

    @Override
    public long getTotalSpace() {
        return -1;
    }

    @Override
    public long getUsedSpace() {
        return -1;
    }

    @Override
    public void upload(String[] parentPath, String fileName, byte[] content, boolean append) throws Exception {
        getDrive().execute(ftpClient -> {
            String targetPath = Paths.get("", parentPath).resolve(fileName).toString();

            byte[] value = content;
            if (append) {
                byte[] prependContent = getRoot().findFileByPath(targetPath).download(getDrive());
                value = Bytes.concat(prependContent, content);
            }

            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            if (!ftpClient.storeFile(targetPath, new ByteArrayInputStream(value))) {
                throw new RuntimeException(ftpClient.getReplyString());
            }
            return null;
        }, true);
    }

    @Override
    public boolean delete(String[] path) throws Exception {
        FTPCacheFileSystem file = getRoot().findFileById(path[path.length - 1]);
        if (file != null) {
            return getDrive().execute(ftpClient -> ftpClient.deleteFile(file.getPath().toString()), true);
        } else {
            return getDrive().execute(ftpClient -> ftpClient.deleteFile(Paths.get("", path).toString()), true);
        }
    }

    @SneakyThrows
    @Override
    public void updateCache(boolean force) {
        getDrive().execute(ftpClient -> {
            super.updateCache(force);
            return null;
        }, true);
    }

    @RequiredArgsConstructor
    private static class FTPCacheFileSource implements CachedFileSystem.SourceFileCapability {
        private final FTPFile file;

        @Override
        public String getId() {
            return file.getName();
        }

        @Override
        public String getName() {
            return file.getName();
        }

        @Override
        public long getLastModifiedTime() {
            return file.getTimestamp().getTimeInMillis();
        }

        @Override
        public void setLastModifiedTime(long time) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time);
            file.setTimestamp(calendar);
        }

        @Override
        public boolean isFolder() {
            return file.isDirectory();
        }

        @Override
        public boolean fillDeeper() {
            return !"Recycle Bin".equals(getName());
        }
    }

    public static class FTPCacheFileSystem extends CachedFileSystem<FTPCacheFileSystem, FTPCacheFileSource, FtpEntity> {

        public FTPCacheFileSystem(FTPCacheFileSource source, FTPCacheFileSystem parent) {
            super(source, parent, false);
        }

        @Override
        @SneakyThrows
        protected FTPCacheFileSource readFileFromServer(FtpEntity driver) {
            FTPFile[] ftpFiles = driver.getFtpClient().listFiles(getPath().toString());
            if (ftpFiles.length == 0) {
                return null;
            }
            if (ftpFiles.length == 1) {
                return new FTPCacheFileSource(ftpFiles[0]);
            }
            throw new RuntimeException("Found too many ftp files: " + ftpFiles.length);
        }

        @Override
        protected FTPCacheFileSystem newInstance(FTPCacheFileSource source, FTPCacheFileSystem parent) {
            return new FTPCacheFileSystem(source, parent);
        }

        @SneakyThrows
        @Override
        public void updateCache(FtpEntity driver) {
            if (driver.getFtpClient() == null) {
                driver.execute(ftpClient -> {
                    super.updateCache(driver);
                    return null;
                }, true);
            } else {
                super.updateCache(driver);
            }
        }

        @Override
        @SneakyThrows
        protected Collection<FTPCacheFileSource> searchForChildren(FTPCacheFileSource serverSource, FtpEntity ftpEntity) {
            return Stream.of(ftpEntity.getFtpClient().listFiles(getPath().resolve(serverSource.getName()).toString()))
                    .map(FTPCacheFileSource::new).collect(Collectors.toList());
        }

        @SneakyThrows
        @Override
        protected byte[] downloadContent(FtpEntity ftpEntity) {
            return ftpEntity.execute(ftpClient -> {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                String filePath = getPath().toString();
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                if (!ftpClient.retrieveFile(filePath, outputStream)) {
                    throw new RuntimeException("Unable to retrieve file: <" + filePath + "> from ftp. Msg: " + ftpClient.getReplyString());
                }
                return outputStream.toByteArray();
            }, true);
        }
    }
}
