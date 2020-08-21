package org.touchhome.bundle.ipscanner.setting;

import org.touchhome.bundle.api.BundleSettingPluginButton;
import org.touchhome.bundle.api.EntityContext;

import java.util.Arrays;
import java.util.List;

public class IpScannerHeaderStopButtonSetting implements BundleSettingPluginButton {

    @Override
    public int order() {
        return 100;
    }
}
