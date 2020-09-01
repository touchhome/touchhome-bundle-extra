package org.touchhome.bundle.mqtt_client;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Component;
import org.touchhome.bundle.api.BundleEntrypoint;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.hardware.HardwareEvents;
import org.touchhome.bundle.api.json.NotificationEntityJSON;
import org.touchhome.bundle.api.util.NotificationType;
import org.touchhome.bundle.api.util.TouchHomeUtils;
import org.touchhome.bundle.api.workspace.BroadcastLockManager;
import org.touchhome.bundle.mqtt_client.setting.*;
import org.touchhome.bundle.mqtt_client.setting.advanced.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Log4j2
@Component
@RequiredArgsConstructor
public class MQTTEntrypoint implements BundleEntrypoint, MqttCallbackExtended {

    private final EntityContext entityContext;
    private final BroadcastLockManager broadcastLockManager;
    private final HardwareEvents hardwareEvents;

    @Getter
    private MqttClient mqttClient;

    @SneakyThrows
    public void init() {
        entityContext.listenSettingValue(MQTTReconnectSetting.class, this::reconnect);
        hardwareEvents.addEvent("mqtt-connection-lost", "MQTT lost connection");
        hardwareEvents.addEvent("mqtt-connected", "MQTT connected");
        reconnect();
    }

    private void reconnect() {
        entityContext.setSettingValue(MQTTStatusSetting.class, "Connecting...");
        String host = entityContext.getSettingValue(MQTTHostSetting.class);
        Integer port = entityContext.getSettingValue(MQTTPortSetting.class);
        String serverURL = String.format("tcp://%s:%d", host, port);
        try {
            this.destroy();
            if (StringUtils.isNotEmpty(host)) {

                MqttConnectOptions options = new MqttConnectOptions();
                options.setAutomaticReconnect(entityContext.getSettingValue(MQTTAutomaticReconnectSetting.class));
                options.setCleanSession(entityContext.getSettingValue(MQTTCleanSessionOnConnectSetting.class));
                options.setConnectionTimeout((int) TimeUnit.SECONDS.toMillis(entityContext.getSettingValue(MQTTConnectionTimeoutSetting.class)));
                options.setKeepAliveInterval(entityContext.getSettingValue(MQTTKeepAliveSetting.class));
                options.setMaxReconnectDelay(entityContext.getSettingValue(MQTTReconnectTimeSetting.class));
                options.setUserName(entityContext.getSettingValue(MQTTUsernameSetting.class));
                options.setPassword(entityContext.getSettingValue(MQTTPasswordSetting.class, "").toCharArray());

                mqttClient = new MqttClient(serverURL, entityContext.getSettingValue(MQTTClientIDSetting.class, UUID.randomUUID().toString()));
                mqttClient.setCallback(this);
                mqttClient.connect(options);
                mqttClient.subscribe("#");
            }
        } catch (Exception ex) {
            this.connectionLost(ex);
        }
    }

    @Override
    public Set<NotificationEntityJSON> getNotifications() {
        String value = entityContext.getSettingValue(MQTTStatusSetting.class);
        return new HashSet<>(Collections.singletonList(new NotificationEntityJSON("mqtt-status")
                .setNotificationType(value.startsWith("Connection lost") ? NotificationType.danger : NotificationType.success)
                .setName("MQTT: " + value)));
    }

    @SneakyThrows
    @Override
    public void destroy() {
        if (this.mqttClient != null && this.mqttClient.isConnected()) {
            this.mqttClient.disconnectForcibly();
            this.mqttClient.close(true);
        }
    }

    @Override
    public String getBundleId() {
        return "mqtt-client";
    }

    @Override
    public int order() {
        return 2000;
    }

    @Override
    public void connectionLost(Throwable cause) {
        entityContext.sendErrorMessage("MQTT connection lost", (Exception) cause);
        String msg = TouchHomeUtils.getErrorMessage(cause);
        entityContext.setSettingValue(MQTTStatusSetting.class, "Connection lost: " + msg);
        hardwareEvents.fireEvent("mqtt-connection-lost", msg);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        try {
            broadcastLockManager.signalAll("mqtt-" + topic, message.toString());
            broadcastLockManager.signalAll("mqtt-#", message.toString());
        } catch (Exception ex) {
            log.error("Unexpected mqtt error", ex);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        hardwareEvents.fireEvent("mqtt-connected");
        entityContext.setSettingValue(MQTTStatusSetting.class, "Connected");
        entityContext.sendInfoMessage("MQTT server connected");
    }
}
