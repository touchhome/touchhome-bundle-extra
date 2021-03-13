package org.touchhome.bundle.dropbox;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.entity.MiscEntity;
import org.touchhome.bundle.api.fs.BaseFileSystemEntity;
import org.touchhome.bundle.api.ui.UISidebarChildren;
import org.touchhome.bundle.api.ui.field.UIField;
import org.touchhome.bundle.api.ui.field.action.impl.DynamicContextMenuAction;
import org.touchhome.bundle.api.util.SecureString;

import javax.persistence.Entity;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Entity
@UISidebarChildren(icon = "fab fa-dropbox", color = "#0d2481")
public class DropboxEntity extends MiscEntity<DropboxEntity> implements BaseFileSystemEntity<DropboxEntity, DropboxFileSystem> {

    public static final String PREFIX = "dropbox_";

    private static Map<String, DropboxFileSystem> fileSystemMap = new HashMap<>();

    @UIField(order = 30, required = true, inlineEditWhenEmpty = true)
    public SecureString getDropboxApiToken() {
        return new SecureString(getJsonData("apiToken"));
    }

    public DropboxEntity setDropboxApiToken(String value) {
        return setJsonData("apiToken", value);
    }

    @Override
    public String getEntityPrefix() {
        return PREFIX;
    }

    @Override
    public boolean requireConfigure() {
        return StringUtils.isEmpty(getDropboxApiToken());
    }

    @JsonIgnore
    public DropboxFileSystem getFileSystem(EntityContext entityContext) {
        return fileSystemMap.computeIfAbsent(getEntityID(), s -> new DropboxFileSystem(this, entityContext));
    }

    @Override
    public Map<String, DropboxFileSystem> getFileSystemMap() {
        return fileSystemMap;
    }

    @Override
    public long getConnectionHashCode() {
        return Objects.hash(getDropboxApiToken().asString());
    }

    @Override
    public String getDefaultName() {
        return "Dropbox";
    }

    @Override
    public Set<DynamicContextMenuAction> getActions(EntityContext entityContext) {
        return null;
    }
}
