package org.touchhome.bundle.influxdb.entity;

import com.influxdb.client.domain.Bucket;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.entity.BaseEntity;
import org.touchhome.bundle.api.model.OptionModel;
import org.touchhome.bundle.api.ui.action.DynamicOptionLoader;
import org.touchhome.bundle.api.ui.field.UIField;
import org.touchhome.bundle.api.ui.field.selection.UIFieldSelection;
import org.touchhome.bundle.api.ui.field.selection.dynamic.DynamicParameterFields;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InfluxCloudQueryParameter implements DynamicParameterFields {

    @UIField(order = 110, required = true)
    @UIFieldSelection(SelectBucket.class)
    public String influxBucket;

    @UIField(order = 130)
    @UIFieldSelection(value = SelectMeasurement.class, staticParameters = {"_measurement"})
    public Set<String> influxMeasurementFilter;

    @UIField(order = 140)
    @UIFieldSelection(value = SelectMeasurement.class, staticParameters = {"_field"})
    public Set<String> influxFieldFilters;

    public static class SelectBucket implements DynamicOptionLoader {
        @Override
        public Collection<OptionModel> loadOptions(BaseEntity baseEntity, EntityContext entityContext, String[] staticParameters) {
            List<Bucket> buckets = ((InfluxCloudDBEntity) baseEntity).getOrCreateInfluxDB().getBucketsApi().findBuckets();
            return buckets.stream().map(b -> OptionModel.key(b.getName())).collect(Collectors.toList());
        }
    }

    public static class SelectMeasurement implements DynamicOptionLoader {
        @Override
        public Collection<OptionModel> loadOptions(BaseEntity baseEntity, EntityContext entityContext, String[] staticParameters) {
            InfluxCloudDBEntity entity = (InfluxCloudDBEntity) baseEntity;
            String query = "from(bucket:\"" + entity.getBucket() + "\")\n" +
                    "  |> range(start:-1y)\n" +
                    "  |> keys()";

            return entity.getOrCreateInfluxDB().getQueryApi()
                    .query(query, entity.getOrg())
                    .stream()
                    .map(FluxTable::getRecords)
                    .flatMap(Collection::stream)
                    .map(FluxRecord::getValues)
                    .map(m -> m.get(staticParameters[0]))
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .distinct()
                    .map(OptionModel::key)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public String getGroupName() {
        return "InfluxDB query";
    }

    @Override
    public String getBorderColor() {
        return "#0E7EBC";
    }
}
