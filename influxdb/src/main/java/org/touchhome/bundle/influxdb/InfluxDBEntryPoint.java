package org.touchhome.bundle.influxdb;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.touchhome.bundle.api.BundleEntryPoint;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.entity.dependency.DependencyExecutableInstaller;
import org.touchhome.bundle.influxdb.setting.InfluxDBConfigureButtonSetting;
import org.touchhome.bundle.influxdb.setting.InfluxDBDependencyExecutableInstaller;
import org.touchhome.bundle.influxdb.setting.InfluxDBInstallSetting;

@Log4j2
@Component
@RequiredArgsConstructor
public class InfluxDBEntryPoint implements BundleEntryPoint {

    private final EntityContext entityContext;

    public void init() {
        entityContext.setting().listenValue(InfluxDBInstallSetting.class, "listen-install_btn-influxdb", () -> {
            DependencyExecutableInstaller installer = entityContext.getBean(InfluxDBDependencyExecutableInstaller.class);
            if (installer != null && installer.isRequireInstallDependencies(entityContext, false)) {
                entityContext.bgp().runWithProgress("install-deps-" + installer.getName(), false,
                        progressBar -> {
                            installer.installDependency(entityContext, progressBar);
                            entityContext.ui().reloadWindow("Influx db installed");
                        }, null,
                        () -> new RuntimeException("INSTALL_DEPENDENCY_IN_PROGRESS"));
            }
        });

        entityContext.getBean(InfluxDBDependencyExecutableInstaller.class).runDbIfRequire(entityContext);
        entityContext.setting().listenValue(InfluxDBConfigureButtonSetting.class, "influx-listen-configure", () ->
                InfluxDBConfigureButtonSetting.configure(entityContext));
    }

    @Override
    public int order() {
        return 200;
    }

    @Override
    public BundleImageColorIndex getBundleImageColorIndex() {
        return BundleImageColorIndex.ONE;
    }
}
