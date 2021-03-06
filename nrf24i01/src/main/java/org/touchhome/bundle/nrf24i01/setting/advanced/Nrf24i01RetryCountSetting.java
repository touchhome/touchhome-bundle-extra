package org.touchhome.bundle.nrf24i01.setting.advanced;

import org.apache.commons.lang3.StringUtils;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.json.Option;
import org.touchhome.bundle.api.setting.SettingPlugin;
import org.touchhome.bundle.nrf24i01.options.RetryCount;

import java.util.List;

public class Nrf24i01RetryCountSetting implements SettingPlugin<RetryCount> {

    @Override
    public UIFieldType getSettingType() {
        return UIFieldType.SelectBox;
    }

    @Override
    public int order() {
        return 40;
    }

    @Override
    public RetryCount parseValue(EntityContext entityContext, String value) {
        return StringUtils.isEmpty(value) ? null : RetryCount.valueOf(value);
    }

    @Override
    public String getDefaultValue() {
        return RetryCount.RETRY_15.name();
    }

    @Override
    public List<Option> loadAvailableValues(EntityContext entityContext) {
        return Option.list(RetryCount.class);
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
