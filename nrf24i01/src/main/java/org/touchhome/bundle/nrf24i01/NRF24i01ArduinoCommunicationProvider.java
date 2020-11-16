package org.touchhome.bundle.nrf24i01;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.touchhome.bundle.arduino.model.ArduinoCommunicationProvider;
import org.touchhome.bundle.arduino.model.ArduinoDeviceEntity;
import org.touchhome.bundle.arduino.repository.ArduinoDeviceRepository;
import org.touchhome.bundle.nrf24i01.command.RF24GetPinValueCommand;
import org.touchhome.bundle.nrf24i01.communication.RF24Message;
import org.touchhome.bundle.nrf24i01.communication.Rf24Communicator;
import org.touchhome.bundle.nrf24i01.communication.SendCommand;
import pl.grzeslowski.smarthome.rf24.helpers.Pipe;

import java.nio.ByteBuffer;
import java.util.Map;

import static org.touchhome.bundle.nrf24i01.ArduinoBaseCommand.*;

@Log4j2
@Component
@RequiredArgsConstructor
public class NRF24i01ArduinoCommunicationProvider implements ArduinoCommunicationProvider {

    private static byte messageID = 0;
    private final Rf24Communicator rf24Communicator;
    private final RF24GetPinValueCommand rf24GetPinValueCommand;

   /* public void sendArduinoUpdatePinBulkValues(ArduinoDeviceEntity arduinoDeviceEntity, Map<Pin, Byte> bulkUpdate) {
        if (!bulkUpdate.isEmpty()) {
            ByteBuffer buffer = ByteBuffer.allocate(1 + bulkUpdate.size() * 2);
            buffer.put((byte) bulkUpdate.size());
            for (Map.Entry<Pin, Byte> item : bulkUpdate.entrySet()) {
                buffer.put((byte) item.getKey().getAddress()); // pinID
                buffer.put(item.getValue()); // value
            }
            SendCommand sendCommand = SendCommand.sendPayload(SET_PIN_VALUE_BULK_COMMAND, buffer);
            sendCommand(arduinoDeviceEntity, sendCommand);
        } else {
            log.info("No bulk values to putToCache arduino");
        }
    }*/

    /**
     * Should fire arduino device to putToCache it's responseManager to send value each <interval> seconds
     */
    public void sendArduinoRequestValue(ArduinoDeviceEntity arduinoDeviceEntity, byte secondsInterval, Pin pin, Byte handlerID, Byte pinRequestType, boolean remove) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.put((byte) pin.getAddress());
        buffer.put(secondsInterval);
        buffer.put(handlerID);
        buffer.put(pinRequestType);

        SendCommand sendCommand;
        if (remove) {
            sendCommand = SendCommand.sendPayload(REMOVE_GET_PIN_VALUE_REQUEST_COMMAND, buffer);
        } else {
            sendCommand = SendCommand.sendPayload(GET_PIN_VALUE_REQUEST_COMMAND, buffer);
        }
        sendCommand(arduinoDeviceEntity, sendCommand);
    }

    /**
     * Send handler to arduino when specific pin has value more than 'value'
     * moreThanValue - 0..1024. For digital pin 1024 - '1'
     */
    /*public void sendArduinoHandlerRequestWhenPinValueOpThan(ArduinoDeviceEntity arduinoDeviceEntity, Pin pin, CommandBuilder commandBuilder, PinMode pinMode, byte moreThanValue, byte op, boolean remove) {
        ByteBuffer buffer = ByteBuffer.allocate(3 + commandBuilder.getSize());
        buffer.put((byte) pin.getAddress()); // pinID

        // value
        buffer.put(moreThanValue);
        byte opMode = -1;
        switch (op) {
            case 0:
                opMode = (byte) (pinMode == PinMode.DIGITAL_INPUT ? 0 : 3);
                break;
            case 1:
                opMode = (byte) (pinMode == PinMode.DIGITAL_INPUT ? 1 : 4);
                break;
            case 2:
                opMode = (byte) (pinMode == PinMode.DIGITAL_INPUT ? 2 : 5);
                break;
        }

        buffer.put(opMode); // operation: >, <, == and mode
        buffer.put(commandBuilder.getBytes()); // handler type and related data...

        SendCommand sendCommand;
        if (remove) {
            sendCommand = SendCommand.sendPayload(REMOVE_HANDLER_REQUEST_WHEN_PIN_VALUE_OP_THAN, buffer);
        } else {
            sendCommand = SendCommand.sendPayload(HANDLER_REQUEST_WHEN_PIN_VALUE_OP_THAN, buffer);
        }

        sendCommand(arduinoDeviceEntity, sendCommand);
    }*/

    private RF24Message sendCommand(ArduinoDeviceEntity arduinoDeviceEntity, SendCommand sendCommand) {
        RF24Message sendMessage = generateCommand(Short.parseShort(arduinoDeviceEntity.getEntityID().substring(ArduinoDeviceRepository.PREFIX.length())));
        scheduleSend(sendCommand, sendMessage, new Pipe(arduinoDeviceEntity.getPipe()));
        return sendMessage;
    }

    public void setValue(ArduinoDeviceEntity arduinoDeviceEntity, Pin pin, Byte value, boolean analog) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.put((byte) pin.getAddress());
        buffer.put(value);
        sendCommand(arduinoDeviceEntity, SendCommand.sendPayload(analog ? SET_PIN_ANALOG_VALUE_COMMAND : SET_PIN_DIGITAL_VALUE_COMMAND, buffer));
    }

    public Integer getValue(ArduinoDeviceEntity arduinoDeviceEntity, Pin pin, boolean analog) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.put((byte) pin.getAddress());
        buffer.put((byte) (analog ? 1 : 0));
        RF24Message rf24Message = sendCommand(arduinoDeviceEntity, SendCommand.sendPayload(GET_PIN_VALUE_COMMAND, buffer));

        return rf24GetPinValueCommand.waitForValue(rf24Message);
    }

    /*public String getTime(String deviceID) {
        String messageID = String.valueOf(System.currentTimeMillis()).substring(0, 10);
        String command = String.format("%s&%s&%s", messageID, GET_TIME_COMMAND, deviceID);
        return sendCommandAndWaitValue(command);
    }*/

    public void scheduleSend(SendCommand sendCommand, RF24Message message, Pipe pipe) {
        rf24Communicator.send(sendCommand, message, pipe);
    }

    public static RF24Message generateCommand(short target) {
        if (messageID > 125) {
            messageID = 0;
        }
        messageID++;
        return new RF24Message(messageID, target, null, null);
    }
}
