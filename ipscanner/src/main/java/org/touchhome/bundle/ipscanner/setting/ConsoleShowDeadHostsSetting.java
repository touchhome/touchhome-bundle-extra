package org.touchhome.bundle.ipscanner.setting;

import org.touchhome.bundle.api.setting.BundleConsoleSettingPlugin;

public class ConsoleShowDeadHostsSetting implements BundleConsoleSettingPlugin<Boolean> {

    @Override
    public SettingType getSettingType() {
        return SettingType.Boolean;
    }

    @Override
    public int order() {
        return 100;
    }

    @Override
    public String[] pages() {
        return new String[]{"ipscanner"};
    }

    @Override
    public String getDefaultValue() {
        return Boolean.TRUE.toString();
    }
}
