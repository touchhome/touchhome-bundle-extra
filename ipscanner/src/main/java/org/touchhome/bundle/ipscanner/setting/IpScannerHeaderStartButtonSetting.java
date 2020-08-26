package org.touchhome.bundle.ipscanner.setting;

import org.touchhome.bundle.api.setting.BundleSettingPluginButton;
import org.touchhome.bundle.api.EntityContext;

import java.util.Arrays;
import java.util.List;

public class IpScannerHeaderStartButtonSetting implements BundleSettingPluginButton {

    @Override
    public String getIcon() {
        return "fas fa-search-location";
    }

    @Override
    public List<InputParameter> getInputParameters(EntityContext entityContext, String value) {
        return Arrays.asList(
                new InputParameter("startIP", InputParameterType.text, InputParameterValidator.ip, "0.0.0.0"),
                new InputParameter("endIP", InputParameterType.text, InputParameterValidator.ip, "0.0.0.1")
        );
    }

    @Override
    public int order() {
        return 100;
    }
}
