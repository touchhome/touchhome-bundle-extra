package org.touchhome.bundle.ipscanner.setting;

import org.json.JSONObject;
import org.touchhome.bundle.api.BundleConsoleSettingPlugin;
import org.touchhome.bundle.api.BundleSettingPluginButton;
import org.touchhome.bundle.api.EntityContext;

import java.util.Collections;
import java.util.List;

public class ConsoleScannedPortsSetting implements BundleConsoleSettingPlugin<JSONObject>, BundleSettingPluginButton {

    private static final String DEFAULT_VALUE = new JSONObject().put("ipscanner_ports", "80,443,8080").toString();

    @Override
    public String getIcon() {
        return "fab fa-megaport";
    }

    @Override
    public int order() {
        return 200;
    }

    @Override
    public String[] pages() {
        return new String[]{"ipscanner"};
    }

    @Override
    public String getDefaultValue() {
        return DEFAULT_VALUE;
    }

    @Override
    public List<InputParameter> getInputParameters(EntityContext entityContext, String value) {
        return Collections.singletonList(new InputParameter("ipscanner_ports", InputParameterType.textarea,
                null, getDefaultValue())
                .setDescription("ipscanner_ports_description"));
    }
}
