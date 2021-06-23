package org.touchhome.bundle.influxdb.setting;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.entity.dependency.DependencyExecutableInstaller;
import org.touchhome.bundle.api.hardware.other.MachineHardwareRepository;
import org.touchhome.bundle.api.model.ProgressBar;
import org.touchhome.bundle.api.setting.SettingPluginOptionsFileExplorer;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import java.nio.file.Files;
import java.nio.file.Path;

@Log4j2
@Component
public class InfluxDBDependencyExecutableInstaller extends DependencyExecutableInstaller {

    @Override
    public String getName() {
        return "influx";
    }

    @Override
    public Path installDependencyInternal(EntityContext entityContext, ProgressBar progressBar) {
        if (TouchHomeUtils.OS.isLinux()) {
            MachineHardwareRepository machineHardwareRepository = entityContext.getBean(MachineHardwareRepository.class);
            machineHardwareRepository.execute(
                    "curl -s https://repos.influxdata.com/influxdb.key | gpg --dearmor > /etc/apt/trusted.gpg.d/influxdb.gpg\n" +
                            "export DISTRIB_ID=$(lsb_release -si); export DISTRIB_CODENAME=$(lsb_release -sc)\n" +
                            "echo \"deb [signed-by=/etc/apt/trusted.gpg.d/influxdb.gpg] https://repos.influxdata.com/${DISTRIB_ID,,} ${DISTRIB_CODENAME} stable\" > /etc/apt/sources.list.d/influxdb.list");
            machineHardwareRepository.update();
            machineHardwareRepository.installSoftware("influxdb");
            machineHardwareRepository.execute("sudo service influxdb start");
            return null;
        } else {
            Path targetFolder;
            if (Files.isRegularFile(TouchHomeUtils.getInstallPath().resolve("influxdb").resolve("influxdb2-2.0.6-windows-amd64").resolve("influx.exe"))) {
                targetFolder = TouchHomeUtils.getInstallPath().resolve("influxdb");
            } else {
                targetFolder = downloadAndExtract("https://dl.influxdata.com/influxdb/releases/influxdb2-2.0.6-windows-amd64.zip",
                        "zip", "influxdb", progressBar, log);
            }
            return targetFolder.resolve("influxdb2-2.0.6-windows-amd64").resolve("influx.exe");
        }
    }

    @Override
    protected void afterDependencyInstalled(EntityContext entityContext, Path path) {
        // run db
        runDbIfRequire(entityContext);
        InfluxDBConfigureButtonSetting.configure(entityContext);
    }

    @Override
    public boolean checkWinDependencyInstalled(MachineHardwareRepository repository, Path targetPath) {
        return !repository.execute(targetPath + " version").startsWith("Influx CLI");
    }

    @Override
    public Class<? extends SettingPluginOptionsFileExplorer> getDependencyPluginSettingClass() {
        return InfluxDBPathSetting.class;
    }

    /**
     * Start db if need
     */
    public void runDbIfRequire(EntityContext entityContext) {
        if (!entityContext.setting().getValue((InfluxDBRunAtStartupSetting.class))) {
            return;
        }
        Path cliPath = entityContext.setting().getValue(InfluxDBPathSetting.class);
        if (cliPath != null && Files.isRegularFile(cliPath)) {
            boolean requireInstallDependencies = entityContext.getBean(InfluxDBDependencyExecutableInstaller.class).isRequireInstallDependencies(entityContext, true);
            if (requireInstallDependencies) {
                return;
            }

            Path dbPath = cliPath.getParent().resolve(TouchHomeUtils.OS.isLinux() ? "influxd" : "influxd.exe");
            entityContext.bgp().run("InfluxDB run thread", () -> {
                entityContext.getBean(MachineHardwareRepository.class).executeEcho(dbPath.toString() + " run");
            }, true);

        }
    }
}
