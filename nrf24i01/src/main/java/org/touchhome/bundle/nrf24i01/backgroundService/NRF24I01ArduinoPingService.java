package org.touchhome.bundle.nrf24i01.backgroundService;

import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.util.UpdatableValue;
import org.touchhome.bundle.arduino.repository.ArduinoDeviceRepository;
import org.touchhome.bundle.nrf24i01.NRF24I01BundleEntrypoint;

import java.util.HashMap;
import java.util.Map;

/**
 * Should be in general
 */
public class NRF24I01ArduinoPingService /*extends BackgroundProcessService<Void> */ {

    private final NRF24I01BundleEntrypoint NRF24I01BundleEntrypoint;
    private final ArduinoDeviceRepository arduinoDeviceRepository;
    private final Map<String, UpdatableValue<Integer>> missedPings = new HashMap<>();

    public NRF24I01ArduinoPingService(EntityContext entityContext) {
       // super(NRF24I01ArduinoPingService.class.getSimpleName(), entityContext);
        NRF24I01BundleEntrypoint = entityContext.getBean(NRF24I01BundleEntrypoint.class);
        arduinoDeviceRepository = entityContext.getBean(ArduinoDeviceRepository.class);
    }

    /*@Override
    public Void runInternal() {
        for (ArduinoDeviceEntity arduinoEntity : entityContext.findAll(ArduinoDeviceEntity.class)) {
            try {
                RF24Message pingMessage = NRF24I01BundleEntrypoint.createPingCommand(arduinoEntity);
                SendCommand sendCommand = SendCommand.sendPayload(ArduinoBaseCommand.PING);

                NRF24I01BundleEntrypoint.subscribeForReading(new ReadListener() {
                    @Override
                    public boolean canReceive(RF24Message rf24Message) {
                        return rf24Message.getMessageID() == pingMessage.getMessageID()
                                && rf24Message.getCommandPlugin().getCommandIndex() == ArduinoBaseCommand.PING.getValue();
                    }

                    @Override
                    public void received(RF24Message rf24Message) {
                        //arduinoEntity.setPingTime(new Date());
                        missedPings.put(arduinoEntity.getEntityID(), UpdatableValue.wrap(0, "mp"));
                        logInfo("Got ping from device <{}>", arduinoEntity.getEntityID());
                    }

                    @Override
                    public void notReceived() {
                        UpdatableValue<Integer> value = missedPings.get(arduinoEntity.getEntityID());
                        value.update(value.getValue() + 1);
                        if (value.getValue() > ARDUINO_MAX_MISSED_PINGS) {
                            removeArduino(arduinoEntity, "Device <" + arduinoEntity.getEntityID() + "> has been removed due no ping");
                        } else {
                            entityContext.save(arduinoEntity);
                        }
                        logError("Found <{}> missed ping for arduino device <{}>", value.getValue(), arduinoEntity.getEntityID());
                    }

                    @Override
                    public Integer maxTimeout() {
                        return 3000;
                    }

                    @Override
                    public String getId() {
                        return "Ping for " + arduinoEntity.getEntityID();
                    }
                });
                NRF24I01BundleEntrypoint.scheduleSend(sendCommand, pingMessage, new Pipe(arduinoEntity.getPipe()));

            } catch (Exception ex) {
                removeArduino(arduinoEntity, ex.getMessage());
            }
        }
        return null;
    }

    private void removeArduino(ArduinoDeviceEntity arduinoDeviceEntity, String msg) {
        arduinoDeviceRepository.deleteByEntityID(arduinoDeviceEntity.getEntityID());
    }

    @Override
    public boolean onException(Exception ex) {
        return false;
    }

    @Override
    public long getPeriod() {
        return ARDUINO_PING_INTERVAL;
    }

    @Override
    public boolean canWork() {
        return NRF24I01BundleEntrypoint.isNrf24L01Works();
    }

    @Override
    protected boolean isAutoStart() {
        return true;
    }

    @Override
    public boolean shouldStartNow() {
        return true;
    }*/
}
