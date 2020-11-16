package org.touchhome.bundle.nrf24i01.communication;

import org.touchhome.bundle.nrf24i01.ArduinoBaseCommand;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class SendCommand {
    public static SendCommand SEND_ERROR = new SendCommand(true, null, ArduinoBaseCommand.FAILED_EXECUTED);
    private final boolean isError;
    private final byte[] payload;
    private final byte commandID;

    private SendCommand(boolean isError, byte[] payload, ArduinoBaseCommand arduinoBaseCommand) {
        this.isError = isError;
        this.payload = payload;
        this.commandID = (byte) arduinoBaseCommand.getValue();
    }

    public static SendCommand sendPayload(ArduinoBaseCommand arduinoBaseCommand) {
        return new SendCommand(false, new byte[0], arduinoBaseCommand);
    }

    public static SendCommand sendPayload(ArduinoBaseCommand arduinoBaseCommand, long value) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(value);
        return new SendCommand(false, buffer.array(), arduinoBaseCommand);
    }

    /*public static SendCommand sendPayload(Command command, Pin pin, int value) {
        ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES);
        buffer.put((byte) pin.getAddress());
        buffer.putInt(value);
        return new SendCommand(false, buffer.array(), command);
    }*/

    public static SendCommand sendPayload(ArduinoBaseCommand arduinoBaseCommand, ByteBuffer buffer) {
        return new SendCommand(false, buffer.array(), arduinoBaseCommand);
    }

    /*public static SendCommand sendPayload(Command command, Pin pin) {
        ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES);
        buffer.put((byte) pin.getAddress());
        return new SendCommand(false, buffer.array(), command);
    }*/

    public static SendCommand sendPayload(ArduinoBaseCommand arduinoBaseCommand, int value) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(value);
        return new SendCommand(false, buffer.array(), arduinoBaseCommand);
    }

    public byte[] getPayload() {
        return payload;
    }

    public byte getCommandID() {
        return commandID;
    }

    public boolean isError() {
        return isError;
    }

    @Override
    public String toString() {
        return "SendCommand{" +
                "isError=" + isError +
                ", payload=" + Arrays.toString(payload) +
                ", command=" + commandID +
                '}';
    }
}
