package org.touchhome.bundle.drive;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.drive.setting.GoogleDriveEmailSetting;
import org.touchhome.bundle.drive.setting.advanced.GoogleDriveAccessTokenSetting;
import org.touchhome.bundle.drive.setting.advanced.GoogleDriveCodeSetting;
import org.touchhome.bundle.drive.setting.advanced.GoogleDriveRefreshTokenSetting;
import org.touchhome.bundle.drive.setting.client.GoogleDriveClientIdSetting;
import org.touchhome.bundle.drive.setting.client.GoogleDriveClientSecretSetting;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GoogleDriveFileSystem {
    private static final String APPLICATION_NAME = "Smart House GGDrive Api";
    private static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private final EntityContext entityContext;
    private GoogleAuthorizationCodeFlow flow;

    private Drive drive;
    private Map<String, File> fileIdFileCache = new HashMap<>();
    private Map<String, String> fileIdByName = new HashMap<>();
    private FileList fileList;

    public GoogleDriveFileSystem(EntityContext entityContext) {
        this.entityContext = entityContext;
    }

    @Scheduled(fixedDelay = 300000)
    public void clearRootFolder() {
        fileList = null;
        fileIdFileCache.clear();
        fileIdByName.clear();
    }

    @SneakyThrows
    public Drive getDrive() {
        return getDrive(entityContext.getSettingValue(GoogleDriveCodeSetting.class));
    }

    private Drive getDrive(String code) throws IOException {
        if (drive == null) {
            if (StringUtils.isEmpty(code)) {
                drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getStoredCredentials()).setApplicationName(APPLICATION_NAME).build();
            } else {
                // create code after receiving
                entityContext.setSettingValue(GoogleDriveCodeSetting.class, null);
                Credential credentials = getCredentials(code);
                drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials).setApplicationName(APPLICATION_NAME).build();
            }
        }
        return drive;
    }

    Credential getStoredCredentials() {
        GoogleCredential credential = new GoogleCredential.Builder()
                .setJsonFactory(JSON_FACTORY)
                .setTransport(HTTP_TRANSPORT).setClientSecrets(
                        entityContext.getSettingValue(GoogleDriveClientIdSetting.class),
                        entityContext.getSettingValue(GoogleDriveClientSecretSetting.class)
                ).build();
        credential.setAccessToken(entityContext.getSettingValue(GoogleDriveAccessTokenSetting.class));
        credential.setRefreshToken(entityContext.getSettingValue(GoogleDriveRefreshTokenSetting.class));
        return credential;
    }

    /**
     * Build an authorization flow and store it as a static class attribute.
     *
     * @return GoogleAuthorizationCodeFlow instance.
     */
    GoogleAuthorizationCodeFlow getFlow() {
        if (flow == null) {
            String clientId = entityContext.getSettingValue(GoogleDriveClientIdSetting.class);
            String clientSecret = entityContext.getSettingValue(GoogleDriveClientSecretSetting.class);
            GoogleClientSecrets googleClientSecrets = new GoogleClientSecrets();
            GoogleClientSecrets.Details det = new GoogleClientSecrets.Details();
            det.setClientId(clientId);
            det.setClientSecret(clientSecret);
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
                .setClientId(entityContext.getSettingValue(GoogleDriveClientIdSetting.class))
                .set("user_id", entityContext.getSettingValue(GoogleDriveEmailSetting.class)).build();
    }

    private Credential getCredentials(String authorizationCode) throws IOException {
        Credential credentials = exchangeCode(authorizationCode);
        entityContext.setSettingValue(GoogleDriveAccessTokenSetting.class, credentials.getAccessToken());
        entityContext.setSettingValue(GoogleDriveRefreshTokenSetting.class, credentials.getRefreshToken());
        return credentials;
    }

    @SneakyThrows
    public String getFileContent(String fileId) {
        return IOUtils.toString(getFileInputStream(fileId));
    }

    @SneakyThrows
    public byte[] getFileRawContent(String fileId) {
        return IOUtils.toByteArray(getFileInputStream(fileId));
    }

    @SneakyThrows
    public InputStream getFileInputStream(String fileId) {
        return getDrive().files().get(fileId).executeMediaAsInputStream();
    }

    public void invalidate() {
        this.drive = null;
        this.flow = null;
    }

    public List<File> getFiles() {
        return getOrCreateFileList().getFiles();
    }

    @SneakyThrows
    private File getFileByFileId(String fileId) {
        File file = fileIdFileCache.get(fileId);
        if (file == null) {
            file = getDrive().files().get(fileId).execute();
            fileIdFileCache.put(fileId, file);
        }
        return file;
    }

    public void addOrUpdateFile(String parent, byte[] array, String name, String description) {
        insertOrUpdateFile(name, parent, description, new ByteArrayContent(null, array));
    }

    private File getFileByNameOrNew(String fileName) {
        File file = getFileByName(fileName);
        return file == null ? new File() : file;
    }

    public File getFileByName(String fileName) {
        String fileId = getFileIdByName(fileName);
        if (fileId != null) {
            return fileIdFileCache.computeIfAbsent(fileId, s -> getFileByFileId(fileId));
        }
        return null;
    }

    private String getFileIdByName(String fileName) {
        return fileIdByName.computeIfAbsent(fileName, s ->
                getFiles().stream().filter(f -> f.getName().equals(fileName)).map(File::getId).findAny().orElse(null));
    }

    private FileList getOrCreateFileList() {
        if (this.fileList == null) {
            try {
                this.fileList = getDrive().files().list().setFields("files(id, name)").execute();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return this.fileList;
    }

    @SneakyThrows
    private void insertOrUpdateFile(String name, String parent, String description, AbstractInputStreamContent mediaContent) {
        File file = getFileByNameOrNew(name);
        if (StringUtils.isNotEmpty(parent)) {
            File parentFile = getFileByName(parent);
            if (parentFile == null) {
                throw new IllegalArgumentException("Unable to find parent file: " + parent);
            } else {
                file.setParents(Collections.singletonList(parentFile.getId()));
            }
        }
        if (description != null) {
            file.setDescription(description);
        }
        file.setModifiedTime(new DateTime(System.currentTimeMillis()));
        if (file.getId() == null) {
            file.setName(name);
            file = drive.files().create(file).setFields("id").execute();
        }
        if (mediaContent != null) {
            getDrive().files().update(file.getId(), null, mediaContent).execute();
        }
        clearRootFolder();
    }
}
