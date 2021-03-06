package org.touchhome.bundle.nrf24i01.setting.advanced;

import org.apache.commons.lang3.StringUtils;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.json.Option;
import org.touchhome.bundle.api.setting.SettingPlugin;
import org.touchhome.bundle.nrf24i01.options.PALevel;

import java.util.List;

public class Nrf24i01PALevelSetting implements SettingPlugin<PALevel> {

    @Override
    public UIFieldType getSettingType() {
        return UIFieldType.SelectBox;
    }

    @Override
    public int order() {
        return 30;
    }

    @Override
    public PALevel parseValue(EntityContext entityContext, String value) {
        return StringUtils.isEmpty(value) ? null : PALevel.valueOf(value);
    }

    @Override
    public String getDefaultValue() {
        return PALevel.RF24_PA_MIN.name();
    }

    @Override
    public List<Option> loadAvailableValues(EntityContext entityContext) {
        return Option.list(PALevel.class);
    }

    @Override
    public boolean isReverted() {
        return true;
    }

    @Override
    public boolean isAdvanced() {
        return true;
    }
}
