package org.touchhome.bundle.gdrive;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.common.primitives.Bytes;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.entity.storage.CachedFileSystem;
import org.touchhome.bundle.api.entity.storage.VendorFileSystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GDriveFileSystem extends VendorFileSystem<Drive, GDriveFileSystem.GoogleDriveCacheFileSystem, GDriveEntity> {
    private static final String APPLICATION_NAME = "TouchHome GGDrive Api";
    private static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private GoogleAuthorizationCodeFlow flow;

    private About about;
    private long aboutLastAccess;

    public GDriveFileSystem(GDriveEntity gDriveEntity, EntityContext entityContext) {
        super(gDriveEntity, entityContext);
        this.dispose();
    }

    @Override
    protected void onEntityUpdated() {

    }

    @SneakyThrows
    public Credential buildDriveByCode(String code) {
        setDrive(null);
        flow = null;
        Credential credentials = exchangeCode(code);
        setDrive(new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials).setApplicationName(APPLICATION_NAME).build());
        return credentials;
    }

    Credential getStoredCredentials() {
        GDriveEntity entity = getEntity();
        GoogleCredential credential = new GoogleCredential.Builder()
                .setJsonFactory(JSON_FACTORY)
                .setTransport(HTTP_TRANSPORT).setClientSecrets(entity.getClientID(), entity.getClientSecret().asString()).build();
        credential.setAccessToken(entity.getAccessToken());
        credential.setRefreshToken(entity.getRefreshToken());
        return credential;
    }

    /**
     * Build an authorization flow and store it as a static class attribute.
     *
     * @return GoogleAuthorizationCodeFlow instance.
     */
    GoogleAuthorizationCodeFlow getFlow() {
        if (flow == null) {
            GoogleClientSecrets googleClientSecrets = new GoogleClientSecrets();
            GoogleClientSecrets.Details det = new GoogleClientSecrets.Details();
            det.setClientId(getEntity().getClientID());
            det.setClientSecret(getEntity().getClientSecret().asString());
            det.setRedirectUris(Collections.singletonList(REDIRECT_URI));
            googleClientSecrets.setInstalled(det);

            flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, googleClientSecrets, SCOPES).setAccessType("offline").setApprovalPrompt("force").build();
        }
        return flow;
    }

    /**
     * Exchange an authorization code for OAuth 2.0 credentials.
     *
     * @param authorizationCode Authorization code to exchange for OAuth 2.0
     *                          credentials.
     * @return OAuth 2.0 credentials.
     */
    Credential exchangeCode(String authorizationCode) throws IOException {
        GoogleTokenResponse response = getFlow().newTokenRequest(authorizationCode).setRedirectUri(REDIRECT_URI).setScopes(SCOPES).execute();
        return getFlow().createAndStoreCredential(response, null);
    }

    public String getAuthorizationUrl() {
        return getFlow().newAuthorizationUrl().setRedirectUri(REDIRECT_URI)
                .setClientId(getEntity().getClientID())
                .set("user_id", getEntity().getEmail()).build();
    }

    @SneakyThrows
    public About getAbout() {
        // not often that once per minute
        if (about == null || System.currentTimeMillis() - aboutLastAccess > 600000) {
            Drive.About.Get get = getDrive().about().get();
            get.setFields("*");
            about = get.execute();
            aboutLastAccess = System.currentTimeMillis();
        }
        return about;
    }

    public void dispose() {
        this.setDrive(null);
        this.flow = null;
        this.setRoot(new GoogleDriveCacheFileSystem(new GDriveFile(
                new File().setId("root").setName("root").setMimeType("application/vnd.google-apps.folder")
                        .setModifiedTime(new DateTime(0))), null));
    }

    @Override
    protected Drive buildDrive() {
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getStoredCredentials()).setApplicationName(APPLICATION_NAME).build();
    }

    @Override
    public long getTotalSpace() {
        return getAbout().getStorageQuota().getLimit();
    }

    @Override
    public long getUsedSpace() {
        return getAbout().getStorageQuota().getUsageInDrive();
    }

    @SneakyThrows
    @Override
    public void upload(String[] parentPath, String fileName, byte[] content, boolean append) {
        GoogleDriveCacheFileSystem parent = getRoot().findFileByPath(parentPath);
        GoogleDriveCacheFileSystem gdriveFileSystem = null;
        File file = null;
        if (parent != null) {
            gdriveFileSystem = parent.findFileByIdOrName(fileName, false);
            if (gdriveFileSystem != null) {
                file = gdriveFileSystem.getSource().file;
            }
        }
        if (file == null) {
            file = new File();
        } else if (append) {
            byte[] prependContent = gdriveFileSystem.download(getDrive());
            content = Bytes.concat(prependContent, content);
        }
        if (parent != null) {
            file.setParents(Collections.singletonList(parent.getSource().getId()));
        }
        file.setModifiedTime(new DateTime(System.currentTimeMillis()));
        if (file.getId() == null) {
            file.setName(fileName);
            file = getDrive().files().create(file).setFields("id").execute();
        }
        getDrive().files().update(file.getId(), null, new ByteArrayContent(null, content)).execute();
        this.getRoot().updateCache(getDrive());
    }

    @Override
    @SneakyThrows
    public boolean delete(String[] path) {
        GoogleDriveCacheFileSystem file = getRoot().findFileByIdOrName(path[path.length - 1], true);
        if (file != null) {
            getDrive().files().delete(file.getSource().getId()).execute();
        } else {
            getDrive().files().delete(path[path.length - 1]).execute();
        }
        return true;
    }

    public static class GoogleDriveCacheFileSystem extends CachedFileSystem<GoogleDriveCacheFileSystem, GDriveFile, Drive> {

        public GoogleDriveCacheFileSystem(GDriveFile source, GoogleDriveCacheFileSystem parent) {
            super(source, parent, true);
        }

        @SneakyThrows
        @Override
        protected GDriveFile readFileFromServer(Drive driver) {
            return new GDriveFile(driver.files().get(getSource().getId()).setFields("*").execute());
        }

        @Override
        protected GoogleDriveCacheFileSystem newInstance(GDriveFile source, GoogleDriveCacheFileSystem parent) {
            return new GoogleDriveCacheFileSystem(source, parent);
        }

        @SneakyThrows
        @Override
        protected Collection<GDriveFile> searchForChildren(GDriveFile serverSource, Drive driver) {
            String pageToken = null;
            List<File> files = new ArrayList<>();
            String query = "'" + serverSource.file.getId() + "' in parents";
            do {
                FileList result = driver.files().list()
                        .setQ(query)
                        .setFields("nextPageToken, files(id, name, mimeType, modifiedTime)")
                        .setPageToken(pageToken)
                        .execute();
                files.addAll(result.getFiles());
                pageToken = result.getNextPageToken();
            } while (pageToken != null);
            return files.stream().map(GDriveFile::new).collect(Collectors.toList());
        }

        @Override
        @SneakyThrows
        protected byte[] downloadContent(Drive drive) {
            return IOUtils.toByteArray(drive.files().get(getSource().getId()).executeMediaAsInputStream());
        }
    }

    @RequiredArgsConstructor
    private static class GDriveFile implements CachedFileSystem.SourceFileCapability {
        private final File file;

        @Override
        public String getId() {
            return file.getId();
        }

        @Override
        public String getName() {
            return file.getName();
        }

        @Override
        public long getLastModifiedTime() {
            return file.getModifiedTime().getValue();
        }

        @Override
        public void setLastModifiedTime(long time) {
            file.setModifiedTime(new DateTime(time));
        }

        @Override
        public boolean isFolder() {
            return file.getMimeType().equals("application/vnd.google-apps.folder");
        }
    }
}
