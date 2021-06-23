package org.touchhome.bundle.ftp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pivovarit.function.ThrowingFunction;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.entity.storage.BaseFileSystemEntity;
import org.touchhome.bundle.api.entity.storage.StorageEntity;
import org.touchhome.bundle.api.model.ActionResponseModel;
import org.touchhome.bundle.api.ui.UISidebarChildren;
import org.touchhome.bundle.api.ui.field.UIField;
import org.touchhome.bundle.api.ui.field.action.UIContextMenuAction;
import org.touchhome.bundle.api.ui.field.action.impl.DynamicContextMenuAction;
import org.touchhome.bundle.api.util.SecureString;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Entity
@UISidebarChildren(icon = "fas fa-network-wired", color = "#b32317")
public class FtpEntity extends StorageEntity<FtpEntity> implements BaseFileSystemEntity<FtpEntity, FtpFileSystem> {

    public static final String PREFIX = "ftp_";

    private static Map<String, FtpFileSystem> fileSystemMap = new HashMap<>();

    @UIField(order = 30, required = true, inlineEditWhenEmpty = true)
    public String getUrl() {
        return getJsonData("url");
    }

    public FtpEntity setUrl(String value) {
        setJsonData("url", value);
        return this;
    }

    @UIField(order = 40)
    public String getUser() {
        return getJsonData("user");
    }

    public FtpEntity setUser(String value) {
        setJsonData("user", value);
        return this;
    }

    @UIField(order = 50)
    public SecureString getPassword() {
        return new SecureString(getJsonData("pwd"));
    }

    public FtpEntity setPassword(String value) {
        setJsonData("pwd", value);
        return this;
    }

    @Override
    public boolean requireConfigure() {
        return StringUtils.isEmpty(getUrl());
    }

    @Override
    public FtpFileSystem getFileSystem(EntityContext entityContext) {
        return fileSystemMap.computeIfAbsent(getEntityID(), s -> new FtpFileSystem(this, entityContext));
    }

    @Override
    public Map<String, FtpFileSystem> getFileSystemMap() {
        return fileSystemMap;
    }

    @Override
    public long getConnectionHashCode() {
        return Objects.hash(getUrl(), getUser(), getPassword());
    }

    @Override
    public String getDefaultName() {
        if (StringUtils.isNotEmpty(getUrl())) {
            String name = getUrl();
            if (name.startsWith("ftp.")) {
                name = name.substring(4);
            }
            if (name.endsWith(".com") || name.endsWith(".org")) {
                name = name.substring(0, name.length() - 4);
            }
            return name;
        }
        return "Ftp";
    }

    @Override
    public String getEntityPrefix() {
        return PREFIX;
    }

    @Override
    public Set<DynamicContextMenuAction> getActions(EntityContext entityContext) {
        return null;
    }

    @UIContextMenuAction(value = "TEST_CONNECTION", icon = "fas fa-ethernet")
    public ActionResponseModel testConnection() {
        FTPClient ftpClient = new FTPClient();
        try {
            try {
                ftpClient.connect(getUrl());
            } catch (Exception ex) {
                return ActionResponseModel.showError("Error connect to remove url: " + ex.getMessage());
            }
            try {
                if (!ftpClient.login(getUser(), getPassword().asString())) {
                    throw new RuntimeException("User or password incorrect.");
                }
            } catch (Exception ex) {
                return ActionResponseModel.showError("Error during attempt login to ftp: " + ex.getMessage());
            }
        } finally {
            try {
                ftpClient.disconnect();
            } catch (IOException ignore) {
            }
        }
        return ActionResponseModel.showSuccess("Success connect to ftp");
    }

    @JsonIgnore
    @Transient
    @Getter
    private FTPClient ftpClient;

    public <T> T execute(ThrowingFunction<FTPClient, T, Exception> handler, boolean localPassive) throws Exception {
        FTPClient ftpClient = new FTPClient();
        Exception exception;
        try {
            ftpClient.connect(getUrl());
            if (!ftpClient.login(getUser(), getPassword().asString())) {
                throw new RuntimeException(ftpClient.getReplyString());
            }
            this.ftpClient = ftpClient;
            if (localPassive) {
                ftpClient.enterLocalPassiveMode();
            }
            return handler.apply(ftpClient);
        } catch (Exception ex) {
            exception = ex;
        } finally {
            try {
                ftpClient.logout();
            } catch (Exception ignore) {
            }
            ftpClient.disconnect();
            this.ftpClient = null;
        }
        throw exception;
    }
}
