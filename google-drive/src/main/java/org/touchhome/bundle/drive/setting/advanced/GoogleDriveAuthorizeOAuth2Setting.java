package org.touchhome.bundle.drive.setting.advanced;

import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.setting.BundleSettingPluginButton;
import org.touchhome.bundle.drive.GoogleDriveFileSystem;

public class GoogleDriveAuthorizeOAuth2Setting implements BundleSettingPluginButton {

    @Override
    public int order() {
        return 50;
    }

    @Override
    public JSONObject getParameters(EntityContext entityContext, String value) {
        if (entityContext == null) {
            return null;
        }
        return new JSONObject().put("url", entityContext.getBean(GoogleDriveFileSystem.class).getAuthorizationUrl());
    }

    @Override
    public boolean isAdvanced() {
        return true;
    }

    @Override
    public boolean isSecuredValue() {
        return true;
    }
}
