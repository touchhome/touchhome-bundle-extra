package org.touchhome.bundle.nrf24i01.setting.advanced;

import org.apache.commons.lang3.StringUtils;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.json.Option;
import org.touchhome.bundle.api.setting.SettingPlugin;
import org.touchhome.bundle.nrf24i01.options.DataRate;

import java.util.List;

public class Nrf24i01DataRateSetting implements SettingPlugin<DataRate> {

    @Override
    public UIFieldType getSettingType() {
        return UIFieldType.SelectBox;
    }

    @Override
    public int order() {
        return 20;
    }

    @Override
    public DataRate parseValue(EntityContext entityContext, String value) {
        return StringUtils.isEmpty(value) ? null : DataRate.valueOf(value);
    }

    @Override
    public String getDefaultValue() {
        return DataRate.RF24_250KBPS.name();
    }

    @Override
    public List<Option> loadAvailableValues(EntityContext entityContext) {
        return Option.list(DataRate.class);
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
