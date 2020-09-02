package org.touchhome.bundle.nrf24i01.setting.advanced;

import org.apache.commons.lang3.StringUtils;
import org.touchhome.bundle.api.setting.BundleSettingPlugin;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.json.Option;
import org.touchhome.bundle.nrf24i01.options.RetryDelay;

import java.util.List;

public class Nrf24i01RetryDelaySetting implements BundleSettingPlugin<RetryDelay> {

    @Override
    public SettingType getSettingType() {
        return SettingType.SelectBox;
    }

    @Override
    public int order() {
        return 50;
    }

    @Override
    public RetryDelay parseValue(EntityContext entityContext, String value) {
        return StringUtils.isEmpty(value) ? null : RetryDelay.valueOf(value);
    }

    @Override
    public String getDefaultValue() {
        return RetryDelay.DELAY_15.name();
    }

    @Override
    public List<Option> loadAvailableValues(EntityContext entityContext) {
        return Option.list(RetryDelay.class);
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