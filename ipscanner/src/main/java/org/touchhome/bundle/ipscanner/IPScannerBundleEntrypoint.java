package org.touchhome.bundle.ipscanner;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.azib.ipscan.IPScannerService;
import net.azib.ipscan.core.state.ScanningState;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.touchhome.bundle.api.BundleEntrypoint;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.json.NotificationEntityJSON;
import org.touchhome.bundle.ipscanner.setting.ConsoleScannedPortsSetting;
import org.touchhome.bundle.ipscanner.setting.IpScannerHeaderStartButtonSetting;
import org.touchhome.bundle.ipscanner.setting.IpScannerHeaderStopButtonSetting;

@Log4j2
@Component
@RequiredArgsConstructor
public class IPScannerBundleEntrypoint implements BundleEntrypoint {

    private final EntityContext entityContext;
    private final IPScannerService ipScannerService;

    @Override
    public void init() {
        NotificationEntityJSON scanJSON = new NotificationEntityJSON("ip-scanner-scan").setName("ipscanner.start_scan");
        ipScannerService.getScannerConfig().portString = entityContext.setting().getValue(ConsoleScannedPortsSetting.class).getString("ipscanner_ports");
        ipScannerService.setCompleteHandler(() -> entityContext.ui().hideAlwaysOnViewNotification(scanJSON));

        entityContext.setting().listenValue(ConsoleScannedPortsSetting.class, "ipscanner-ports", jsonObject ->
                ipScannerService.getScannerConfig().portString = jsonObject.getString("ipscanner_ports"));
        entityContext.setting().listenValue(IpScannerHeaderStopButtonSetting.class, "ipscanner-stop", this::stopScanning);
        entityContext.setting().listenValue(IpScannerHeaderStartButtonSetting.class, "ipscanner-start",
                jsonObject -> startScanning(scanJSON, jsonObject));
    }

    @Override
    public void destroy() {
        if (!ipScannerService.getStateMachine().inState(ScanningState.IDLE)) {
            stopScanning();
        }
    }

    @Override
    public String getBundleId() {
        return "ipscanner";
    }

    @Override
    public int order() {
        return 6000;
    }

    private void startScanning(NotificationEntityJSON scanJSON, JSONObject jsonObject) {
        if (ipScannerService.getStateMachine().inState(ScanningState.IDLE)) {
            entityContext.ui().showAlwaysOnViewNotification(
                    scanJSON, "fas fa-spin fa-hourglass-start", "#325E32", IpScannerHeaderStopButtonSetting.class);
            ipScannerService.startScan(jsonObject.getString("startIP"), jsonObject.getString("endIP"));
        }
    }

    private void stopScanning() {
        ipScannerService.getStateMachine().stop(); // stopping
        ipScannerService.getStateMachine().transitionToNext(); // killing
    }
}
















