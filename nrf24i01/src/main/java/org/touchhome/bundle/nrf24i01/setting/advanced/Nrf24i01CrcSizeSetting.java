package org.touchhome.bundle.nrf24i01.setting.advanced;

import org.apache.commons.lang3.StringUtils;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.json.Option;
import org.touchhome.bundle.api.setting.SettingPlugin;
import org.touchhome.bundle.nrf24i01.options.CRCSize;

import java.util.List;

public class Nrf24i01CrcSizeSetting implements SettingPlugin<CRCSize> {

    @Override
    public UIFieldType getSettingType() {
        return UIFieldType.SelectBox;
    }

    @Override
    public int order() {
        return 10;
    }

    @Override
    public CRCSize parseValue(EntityContext entityContext, String value) {
        return StringUtils.isEmpty(value) ? null : CRCSize.valueOf(value);
    }

    @Override
    public String getDefaultValue() {
        return CRCSize.ENABLE_8_BITS.name();
    }

    @Override
    public List<Option> loadAvailableValues(EntityContext entityContext) {
        return Option.list(CRCSize.class);
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
