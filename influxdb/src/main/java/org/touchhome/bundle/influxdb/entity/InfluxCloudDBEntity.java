package org.touchhome.bundle.influxdb.entity;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.entity.widget.HasLineChartSeries;
import org.touchhome.bundle.api.model.ActionResponseModel;
import org.touchhome.bundle.api.ui.UISidebarChildren;
import org.touchhome.bundle.api.ui.field.UIField;
import org.touchhome.bundle.api.ui.field.action.HasDynamicContextMenuActions;
import org.touchhome.bundle.api.ui.field.action.UIContextMenuAction;
import org.touchhome.bundle.api.ui.field.action.impl.DynamicContextMenuAction;
import org.touchhome.bundle.api.ui.field.selection.dynamic.SelectionWithDynamicParameterFields;
import org.touchhome.bundle.api.util.SecureString;

import javax.persistence.Entity;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.touchhome.bundle.api.EntityContextWidget.LINE_CHART_WIDGET_PREFIX;

@Getter
@Setter
@Entity
@Accessors(chain = true)
@UISidebarChildren(icon = "fab fa-cloudflare", color = "#90c211")
public class InfluxCloudDBEntity extends InfluxDBBaseEntity<InfluxCloudDBEntity> implements HasDynamicContextMenuActions,
        HasLineChartSeries, SelectionWithDynamicParameterFields {

    public static final String PREFIX = "influxclouddb_";
    public static final SimpleDateFormat FORMAT_RANGE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Override
    public boolean isRequireConfigure() {
        return isEmpty(getToken()) || isEmpty(getUrl()) || isEmpty(getBucket());
    }

    @Override
    public String getDescription(boolean require) {
        return require ? "influxclouddb.require_description" : "influxclouddb.description";
    }

    @UIField(order = 30, required = true, inlineEditWhenEmpty = true)
    public SecureString getToken() {
        return new SecureString(getJsonData("token"));
    }

    public InfluxCloudDBEntity setToken(String value) {
        return setJsonData("token", value);
    }

    public String getUser() {
        return getJsonData("user");
    }

    public InfluxCloudDBEntity setUser(String value) {
        return setJsonData("user", value);
    }

    public SecureString getPassword() {
        return new SecureString(getJsonData("pwd"));
    }

    public InfluxCloudDBEntity setPassword(String value) {
        return setJsonData("pwd", value);
    }

    @UIField(order = 40)
    public String getBucket() {
        return getJsonData("bucket", "touchHomeBucket");
    }

    public InfluxCloudDBEntity setBucket(String value) {
        return setJsonData("bucket", value);
    }


    @UIField(order = 40)
    public String getOrg() {
        return getJsonData("org", "primary");
    }

    public InfluxCloudDBEntity setOrg(String value) {
        return setJsonData("org", value);
    }

    @UIField(order = 100, required = true, inlineEditWhenEmpty = true)
    public String getUrl() {
        return getJsonData("url", "https://eu-central-1-1.aws.cloud2.influxdata.com");
    }

    public InfluxCloudDBEntity setUrl(String value) {
        return setJsonData("url", value);
    }

    @Override
    public String getDefaultName() {
        if (StringUtils.isEmpty(getBucket())) {
            return "InfluxCloudDB";
        }
        return "InfluxCloudDB/" + getBucket();
    }

    @Override
    public String getEntityPrefix() {
        return PREFIX;
    }

    private static Map<String, InfluxDBClient> entityToInfluxDB = new HashMap<>();

    public InfluxDBClient getOrCreateInfluxDB() {
        return entityToInfluxDB.computeIfAbsent(getEntityID(), entityID -> InfluxDBClientFactory.create(getUrl(),
                getToken().asString().toCharArray()));
    }

    @Override
    public void afterDelete(EntityContext entityContext) {
        entityToInfluxDB.remove(getEntityID());
    }

    @UIContextMenuAction("CHECK_DB_CONNECTION")
    public ActionResponseModel checkConnection() {
        InfluxDBClient influxDB = getOrCreateInfluxDB();
        // check that api works
        influxDB.getUsersApi().findUsers();
        return ActionResponseModel.showSuccess("ACTION.SUCCESS");
    }

    @Override
    public Set<? extends DynamicContextMenuAction> getActions(EntityContext entityContext) {
        String widgetKey = LINE_CHART_WIDGET_PREFIX + "influx_widget";
        if (entityContext.getEntity(widgetKey) == null) {
            return Collections.singleton(new DynamicContextMenuAction("WIDGET.CREATE_LINE_CHART", 0, params -> {
                entityContext.widget().createLineChartWidget(widgetKey, "InfluxDB query widget",
                        builder -> builder.addLineChart(null, InfluxCloudDBEntity.this),
                        builder -> builder.showButtons(true), null);
                // update item to remove dynamic context
                entityContext.ui().updateItem(this);
            }));
        }
        return Collections.emptySet();
    }

    @Override
    public Map<LineChartDescription, List<Object[]>> getLineChartSeries(EntityContext entityContext, JSONObject parameters, Date from, Date to, String dateFromNow) {
        String bucket = parameters.optString("influxBucket");

        if (StringUtils.isEmpty(bucket)) {
            return Collections.emptyMap();
        }

        // range
        String query = "from(bucket:\"" + bucket + "\")\n";
        if (StringUtils.isNotEmpty(dateFromNow)) {
            query += "        |> range(start: " + dateFromNow + ")";
        } else {
            query += "        |> range(start: " + FORMAT_RANGE.format(from) + ", end: " + FORMAT_RANGE.format(to) + ")";
        }

        query = updateQueryWithFilter(parameters, query, "influxMeasurementFilter", "_measurement");
        query = updateQueryWithFilter(parameters, query, "influxFieldFilters", "_field");

        InfluxDBClient influxDB = InfluxDBClientFactory.create(getUrl(), getToken().asString().toCharArray());
        List<FluxTable> tables = influxDB.getQueryApi().query(query, getOrg());

        Map<LineChartDescription, List<Object[]>> charts = new HashMap<>(tables.size());
        for (FluxTable fluxTable : tables) {
            List<Object[]> values = new ArrayList<>(fluxTable.getRecords().size());

            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                values.add(new Object[]{fluxRecord.getTime().toEpochMilli(), ((Double) fluxRecord.getValue()).floatValue(), fluxRecord.getMeasurement()});
            }

            charts.put(new LineChartDescription(), values);
        }
        influxDB.close();
        return charts;
    }

    private String updateQueryWithFilter(JSONObject parameters, String query, String influxMeasurementFilter, String queryFilterKey) {
        JSONArray measurementFilters = parameters.optJSONArray(influxMeasurementFilter);
        if (measurementFilters != null && !measurementFilters.isEmpty()) {
            query += "\n        |> filter(fn: (r) => " + measurementFilters.toList().stream()
                    .map(m -> "r[\"" + queryFilterKey + "\"] == \"" + m + "\"").collect(Collectors.joining(" or ")) + " )";
        }
        return query;
    }

    @Override
    public InfluxCloudQueryParameter getDynamicParameterFields(Object selectionHolder) {
        return new InfluxCloudQueryParameter(getBucket(), Collections.singleton("r[\"_measurement\"] == \"sample\")"), Collections.singleton("r[\"_field\"] == \"test\""));
    }
}
