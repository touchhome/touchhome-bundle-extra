package org.touchhome.bundle.influxdb.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.touchhome.bundle.api.Lang;
import org.touchhome.bundle.api.entity.HasStatusAndMsg;
import org.touchhome.bundle.api.entity.storage.StorageEntity;
import org.touchhome.bundle.api.model.Status;
import org.touchhome.bundle.api.ui.field.UIField;
import org.touchhome.bundle.api.ui.field.UIFieldType;
import org.touchhome.bundle.api.ui.field.color.UIFieldColorStatusMatch;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Getter
@Accessors(chain = true)
public abstract class InfluxDBBaseEntity<T extends InfluxDBBaseEntity> extends StorageEntity<T> implements HasStatusAndMsg<T> {

    @Getter
    @UIField(order = 22, readOnly = true, hideOnEmpty = true)
    @UIFieldColorStatusMatch
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private Status status;

    @Getter
    @UIField(order = 23, readOnly = true, hideOnEmpty = true)
    @Column(length = 512)
    private String statusMessage;

    @Override
    public T setStatus(Status status) {
        this.status = status;
        return (T) this;
    }

    @Override
    public T setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
        return (T) this;
    }

    public abstract boolean isRequireConfigure();

    @UIField(order = 1, readOnly = true, hideOnEmpty = true, fullWidth = true, bg = "#334842", type = UIFieldType.HTML)
    public final String getDescription() {
        return Lang.getServerMessage(getDescription(isRequireConfigure()));
    }

    public abstract String getDescription(boolean require);
}
