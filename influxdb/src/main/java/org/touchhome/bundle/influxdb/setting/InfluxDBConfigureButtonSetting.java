package org.touchhome.bundle.influxdb.setting;

import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.EntityContextUI;
import org.touchhome.bundle.api.hardware.other.MachineHardwareRepository;
import org.touchhome.bundle.api.setting.SettingPluginButton;
import org.touchhome.bundle.api.ui.field.action.ActionInputParameter;
import org.touchhome.bundle.api.util.TouchHomeUtils;
import org.touchhome.bundle.influxdb.entity.InfluxCloudDBEntity;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class InfluxDBConfigureButtonSetting implements SettingPluginButton {

    @Override
    public int order() {
        return 120;
    }

    @Override
    public String getIcon() {
        return "fas fa-wrench";
    }

    @Override
    public boolean isDisabled(EntityContext entityContext) {
        return entityContext.getBean(InfluxDBDependencyExecutableInstaller.class).isRequireInstallDependencies(entityContext, true);
    }

    public static void configure(EntityContext entityContext) {
        List<ActionInputParameter> inputs = new ArrayList<>();
        inputs.add(ActionInputParameter.message("Request for configure influx db and create influxDB entity"));
        inputs.add(ActionInputParameter.text("Url", "http://localhost:8086"));
        inputs.add(ActionInputParameter.text("Organization", "primary"));
        inputs.add(ActionInputParameter.text("Bucket", "th_bucket"));
        inputs.add(ActionInputParameter.text("User", "th_admin"));
        inputs.add(ActionInputParameter.text("Password", "th_password"));
        inputs.add(ActionInputParameter.text("Token", "th_secret_token"));
        inputs.add(ActionInputParameter.bool("Create InfluxDB entity", true));

        entityContext.ui().sendDialogRequest("create-influx-entity", "Configure influxdb database",
                (responseType, pressedButton, params) -> {
                    if (responseType == EntityContextUI.DialogResponseType.Accepted) {
                        InfluxCloudDBEntity entity = new InfluxCloudDBEntity()
                                .setEntityID(InfluxCloudDBEntity.PREFIX + "influxPrimaryDb")
                                .setBucket(params.getString("Bucket"))
                                .setOrg(params.getString("Organization"))
                                .setToken(params.getString("Token"))
                                .setUrl(params.getString("Url"))
                                .setPassword(params.getString("Password"))
                                .setUser(params.getString("User"));

                        String command = String.format(" setup -f -o %s -b %s -p %s -u %s -t %s",
                                entity.getOrg(),
                                entity.getBucket(),
                                entity.getPassword().asString(),
                                entity.getUser(),
                                entity.getToken().asString());
                        entity.setEntityID(InfluxCloudDBEntity.PREFIX + command.hashCode());

                        if (params.getBoolean("Create InfluxDB entity") && entityContext.getEntity(entity) == null) {
                            entityContext.save(entity);
                        }

                        if (TouchHomeUtils.OS.isWindows()) {
                            Path path = entityContext.setting().getValue(InfluxDBPathSetting.class);
                            try {
                                entityContext.getBean(MachineHardwareRepository.class).executeEcho(path.toString() + command);
                                entityContext.ui().sendSuccessMessage("InfluxDB configured successfully");
                            } catch (Exception ex) {
                                entityContext.ui().sendErrorMessage("Error while configure influxDB", "", ex);
                            }
                        }
                    }
                }, dialogBuilder -> dialogBuilder.submitButton("Configure", btn -> btn.setColor("#3880CD"))
                        .cancelButton("Discard").group("General", inputs));
    }
}
