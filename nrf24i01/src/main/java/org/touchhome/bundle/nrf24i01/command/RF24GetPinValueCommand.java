package org.touchhome.bundle.nrf24i01.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.touchhome.bundle.nrf24i01.communication.RF24Message;
import org.touchhome.bundle.nrf24i01.communication.SendCommand;

import static org.touchhome.bundle.nrf24i01.ArduinoBaseCommand.GET_PIN_VALUE_COMMAND;

@Component
@RequiredArgsConstructor
public class RF24GetPinValueCommand implements RF24CommandPlugin {

    private final LockManager<Integer> lockManager = new LockManager<>();

    @Override
    public Byte getCommandIndex() {
        return (byte) GET_PIN_VALUE_COMMAND.getValue();
    }

    @Override
    public String getName() {
        return GET_PIN_VALUE_COMMAND.name();
    }

    public Integer waitForValue(RF24Message rf24Message) {
        return lockManager.await(String.valueOf(rf24Message.getMessageID()), 10000);
    }

    @Override
    public SendCommand execute(RF24Message message) {
        lockManager.signalAll(String.valueOf(message.getMessageID()), (int) message.getPayloadBuffer().get());
        return null;
    }

    @Override
    public boolean canReceiveGeneral() {
        return true;
    }
}
