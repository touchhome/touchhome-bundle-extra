package org.touchhome.bundle.mqtt_client;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Component;
import org.touchhome.bundle.api.BundleEntryPoint;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.ui.BellNotification;
import org.touchhome.bundle.api.util.NotificationLevel;
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
public class MQTTEntrypoint implements BundleEntryPoint, MqttCallbackExtended {

    private final EntityContext entityContext;
    private final BroadcastLockManager broadcastLockManager;

    @Getter
    private MqttClient mqttClient;

    @SneakyThrows
    public void init() {
        entityContext.setting().listenValue(MQTTReconnectSetting.class, "mqtt-reconnect", this::reconnect);
        entityContext.event().addEvent("mqtt-connection-lost", "MQTT lost connection");
        entityContext.event().addEvent("mqtt-connected", "MQTT connected");
        reconnect();
    }

    private void reconnect() {
        entityContext.setting().setValue(MQTTStatusSetting.class, "Connecting...");
        String host = entityContext.setting().getValue(MQTTHostSetting.class);
        Integer port = entityContext.setting().getValue(MQTTPortSetting.class);
        String serverURL = String.format("tcp://%s:%d", host, port);
        try {
            this.destroy();
            if (StringUtils.isNotEmpty(host)) {

                MqttConnectOptions options = new MqttConnectOptions();
                options.setAutomaticReconnect(entityContext.setting().getValue(MQTTAutomaticReconnectSetting.class));
                options.setCleanSession(entityContext.setting().getValue(MQTTCleanSessionOnConnectSetting.class));
                options.setConnectionTimeout((int) TimeUnit.SECONDS.toMillis(entityContext.setting().getValue(MQTTConnectionTimeoutSetting.class)));
                options.setKeepAliveInterval(entityContext.setting().getValue(MQTTKeepAliveSetting.class));
                options.setMaxReconnectDelay(entityContext.setting().getValue(MQTTReconnectTimeSetting.class));
                options.setUserName(entityContext.setting().getValue(MQTTUsernameSetting.class));
                options.setPassword(entityContext.setting().getValue(MQTTPasswordSetting.class, "").toCharArray());

                mqttClient = new MqttClient(serverURL, entityContext.setting().getValue(MQTTClientIDSetting.class, UUID.randomUUID().toString()));
                mqttClient.setCallback(this);
                mqttClient.connect(options);
                mqttClient.subscribe("#");
            }
        } catch (Exception ex) {
            this.connectionLost(ex);
        }
    }

    @Override
    public Set<BellNotification> getBellNotifications() {
        String value = entityContext.setting().getValue(MQTTStatusSetting.class);
        return new HashSet<>(Collections.singletonList(new BellNotification("mqtt-status")
                .setLevel(value.startsWith("Connection lost") ? NotificationLevel.error : NotificationLevel.success)
                .setTitle("MQTT: " + value)));
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
        entityContext.ui().sendErrorMessage("MQTT connection lost", (Exception) cause);
        String msg = TouchHomeUtils.getErrorMessage(cause);
        entityContext.setting().setValue(MQTTStatusSetting.class, "Connection lost: " + msg);
        entityContext.event().fireEvent("mqtt-connection-lost", msg);
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
        entityContext.event().fireEvent("mqtt-connected");
        entityContext.setting().setValue(MQTTStatusSetting.class, "Connected");
        entityContext.ui().sendInfoMessage("MQTT server connected");
    }
}
