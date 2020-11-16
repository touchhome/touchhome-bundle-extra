package org.touchhome.bundle.mqtt_client.workspace;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Component;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.scratch.*;
import org.touchhome.bundle.api.workspace.BroadcastLock;
import org.touchhome.bundle.api.workspace.BroadcastLockManager;
import org.touchhome.bundle.mqtt_client.MQTTEntrypoint;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@Component
public class Scratch3MQTTBlocks extends Scratch3ExtensionBlocks {

    private static final String TOPIC = "TOPIC";
    private static final String PAYLOAD = "PAYLOAD";
    private static final String LEVEL = "LEVEL";
    private static final String RETAINED = "RETAINED";

    private final MQTTEntrypoint mqttEntrypoint;
    private final BroadcastLockManager broadcastLockManager;
    private final MenuBlock.StaticMenuBlock<QoSLevel> publishLevelMenu;

    private final Scratch3Block publish;
    private final Scratch3Block subscribe;
    private final Scratch3Block subscribeToValue;
    private final Scratch3Block subscribeToAnything;

    public Scratch3MQTTBlocks(EntityContext entityContext, MQTTEntrypoint mqttEntrypoint, BroadcastLockManager broadcastLockManager) {
        super("#7D713E", entityContext, mqttEntrypoint);
        this.mqttEntrypoint = mqttEntrypoint;
        this.broadcastLockManager = broadcastLockManager;

        // menu
        this.publishLevelMenu = MenuBlock.ofStatic(LEVEL, QoSLevel.class, QoSLevel.AtMostOnce);

        // blocks
        this.subscribeToAnything = Scratch3Block.ofHandler(10, "subscribe_any", BlockType.hat,
                "Subscribe to any topic", this::subscribeToAnything);

        this.subscribe = Scratch3Block.ofHandler(20, "subscribe_topic", BlockType.hat,
                "Subscribe to topic [TOPIC]", workspaceBlock -> subscribeToValue(workspaceBlock, null));
        this.subscribe.addArgument(TOPIC, ArgumentType.string);

        this.subscribeToValue = Scratch3Block.ofHandler(30, "subscribe_payload", BlockType.hat,
                "Subscribe to topic [TOPIC] and payload [PAYLOAD]", this::subscribeToValue);
        this.subscribeToValue.addArgument(TOPIC);
        this.subscribeToValue.addArgument(PAYLOAD);

        this.publish = Scratch3Block.ofHandler(40, "publish", BlockType.command,
                "Publish payload [PAYLOAD] to topic [TOPIC] | Level: [LEVEL], Retained: [RETAINED]", this::publish);
        this.publish.addArgument(TOPIC, ArgumentType.string);
        this.publish.addArgument(PAYLOAD, ArgumentType.string);
        this.publish.addArgument(LEVEL, this.publishLevelMenu);
        this.publish.addArgument(RETAINED, ArgumentType.checkbox);

        postConstruct();
    }

    private void subscribeToAnything(WorkspaceBlock workspaceBlock) {
        subscribeToValue(workspaceBlock, null, "#");
    }

    private void subscribeToValue(WorkspaceBlock workspaceBlock) {
        String payload = workspaceBlock.getInputString(PAYLOAD);
        if (isNotBlank(payload)) {
            subscribeToValue(workspaceBlock, payload);
        }
    }

    private void subscribeToValue(WorkspaceBlock workspaceBlock, String payload) {
        subscribeToValue(workspaceBlock, payload, workspaceBlock.getInputString(TOPIC));
    }

    private void subscribeToValue(WorkspaceBlock workspaceBlock, String payload, String topic) {
        if (workspaceBlock.getNext() != null && isNotBlank(topic)) {
            BroadcastLock lock = broadcastLockManager.getOrCreateLock(workspaceBlock, "mqtt-" + topic, payload);
            workspaceBlock.subscribeToLock(lock);
        }
    }

    private void publish(WorkspaceBlock workspaceBlock) throws MqttException {
        String topic = workspaceBlock.getInputString(TOPIC);
        String payload = workspaceBlock.getInputString(PAYLOAD);
        if (isNotBlank(topic) && isNotBlank(payload)) {
            this.mqttEntrypoint.getMqttClient().publish(
                    topic,
                    payload.getBytes(),
                    workspaceBlock.getMenuValue(LEVEL, this.publishLevelMenu).ordinal(),
                    workspaceBlock.getInputBoolean(RETAINED));
        }
    }

    @RequiredArgsConstructor
    private enum QoSLevel {
        AtMostOnce("At Most Once"),
        AtLeastOnce("At Least Once"),
        ExactlyOnce("Exactly Once");

        private final String name;

        @Override
        public String toString() {
            return name;
        }
    }
}
