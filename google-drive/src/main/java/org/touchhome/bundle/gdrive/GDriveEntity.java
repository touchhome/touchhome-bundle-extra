package org.touchhome.bundle.gdrive;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.api.client.auth.oauth2.Credential;
import org.apache.commons.lang3.StringUtils;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.Lang;
import org.touchhome.bundle.api.entity.storage.BaseFileSystemEntity;
import org.touchhome.bundle.api.entity.storage.StorageEntity;
import org.touchhome.bundle.api.model.Status;
import org.touchhome.bundle.api.ui.UISidebarChildren;
import org.touchhome.bundle.api.ui.field.UIField;
import org.touchhome.bundle.api.ui.field.action.ActionInputParameter;
import org.touchhome.bundle.api.ui.field.action.impl.DynamicContextMenuAction;
import org.touchhome.bundle.api.util.SecureString;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import javax.persistence.Entity;
import java.util.*;

@Entity
@UISidebarChildren(icon = "fab fa-google-drive", color = "#0DA10A")
public class GDriveEntity extends StorageEntity<GDriveEntity> implements BaseFileSystemEntity<GDriveEntity, GDriveFileSystem> {

    public static final String PREFIX = "gdrive_";

    private static Map<String, GDriveFileSystem> fileSystemMap = new HashMap<>();

    @UIField(order = 30, required = true, inlineEditWhenEmpty = true)
    public String getClientID() {
        return getJsonData("clientID");
    }

    public GDriveEntity setClientID(String value) {
        return setJsonData("clientID", value);
    }

    @UIField(order = 40, required = true, inlineEditWhenEmpty = true)
    public SecureString getClientSecret() {
        return new SecureString(getJsonData("clientSecret"));
    }

    public GDriveEntity setClientSecret(String value) {
        return setJsonData("clientSecret", value);
    }

    @UIField(order = 50, readOnly = true)
    public String getAccessToken() {
        return getJsonData("accessToken");
    }

    public GDriveEntity setAccessToken(String value) {
        return setJsonData("accessToken", value);
    }

    @UIField(order = 60, readOnly = true)
    public String getRefreshToken() {
        return getJsonData("refreshToken");
    }

    public GDriveEntity setRefreshToken(String value) {
        return setJsonData("refreshToken", value);
    }

    @UIField(order = 100)
    public String getEmail() {
        return getJsonData("email");
    }

    public GDriveEntity setEmail(String value) {
        return setJsonData("email", value);
    }

    @Override
    public String getEntityPrefix() {
        return PREFIX;
    }

    @Override
    public Set<DynamicContextMenuAction> getActions(EntityContext entityContext) {
        if (StringUtils.isNotEmpty(getAccessToken())) {
            return Collections.emptySet();
        }
        GDriveFileSystem gDriveFileSystem = this.getFileSystem(entityContext);
        DynamicContextMenuAction authAction = new DynamicContextMenuAction("OAUTH2_AUTHENTICATE",
                0, jsonObject -> {
            String code = jsonObject.getString("code");

            try {
                Credential credential = gDriveFileSystem.buildDriveByCode(code);
                entityContext.save(this
                        .setStatus(Status.ONLINE)
                        .setStatusMessage(null)
                        .setAccessToken(credential.getAccessToken())
                        .setRefreshToken(credential.getRefreshToken()));
                entityContext.ui().sendSuccessMessage("GDrive Oauth2 authenticate successful");
            } catch (Exception ex) {
                String msg = TouchHomeUtils.getErrorMessage(ex);
                entityContext.save(setStatus(Status.ERROR).setStatusMessage(msg));
                entityContext.ui().sendErrorMessage("Error during Oauth2 authenticate. " + msg);
            }

        }).setIcon("fas fa-sign-in-alt");
        authAction.addInput(ActionInputParameter.text("code", "past_code_here")
                .setDescription(Lang.getServerMessage("gdrive.code_description", "URL",
                        gDriveFileSystem.getAuthorizationUrl())));
        return Collections.singleton(authAction);
    }

    @Override
    public boolean requireConfigure() {
        return StringUtils.isEmpty(getAccessToken());
    }

    @JsonIgnore
    public GDriveFileSystem getFileSystem(EntityContext entityContext) {
        return fileSystemMap.computeIfAbsent(getEntityID(), s -> new GDriveFileSystem(this, entityContext));
    }

    @Override
    public Map<String, GDriveFileSystem> getFileSystemMap() {
        return fileSystemMap;
    }

    @Override
    public long getConnectionHashCode() {
        return Objects.hash(getAccessToken(), getRefreshToken(), getClientID(), getClientSecret().asString());
    }

    @Override
    public String getDefaultName() {
        return "GDrive";
    }
}
