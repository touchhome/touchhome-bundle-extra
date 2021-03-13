package org.touchhome.bundle.nrf24i01;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.touchhome.bundle.api.BundleEntrypoint;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.setting.SettingPluginStatus;
import org.touchhome.bundle.api.util.RaspberryGpioPin;
import org.touchhome.bundle.api.util.TouchHomeUtils;
import org.touchhome.bundle.arduino.model.ArduinoDeviceEntity;
import org.touchhome.bundle.arduino.repository.ArduinoDeviceRepository;
import org.touchhome.bundle.nrf24i01.communication.RF24Message;
import org.touchhome.bundle.nrf24i01.communication.ReadListener;
import org.touchhome.bundle.nrf24i01.communication.Rf24Communicator;
import org.touchhome.bundle.nrf24i01.communication.SendCommand;
import org.touchhome.bundle.nrf24i01.setting.Nrf24i01EnableButtonsSetting;
import org.touchhome.bundle.nrf24i01.setting.Nrf24i01StatusSetting;
import org.touchhome.bundle.nrf24i01.setting.advanced.*;
import pl.grzeslowski.smarthome.rf24.helpers.Pipe;

import static org.touchhome.bundle.api.util.RaspberryGpioPin.*;

@Log4j2
@Component
@RequiredArgsConstructor
public class NRF24I01BundleEntrypoint implements BundleEntrypoint {
    private static final Pipe GLOBAL_WRITE_PIPE = new Pipe("2Node");
    private static String errorLoadingLibrary = null;
    private final Rf24Communicator rf24Communicator;
    private final EntityContext entityContext;
    private boolean libLoaded = false;

    public boolean isNrf24L01Works() {
        return libLoaded && errorLoadingLibrary == null && rf24Communicator.getNRF24L01() != null;
    }

    private void loadLibrary() {
        if (!libLoaded) {
            try {
                System.load(TouchHomeUtils.getFilesPath().resolve("nrf24i01/librf24bcmjava.so").toAbsolutePath().toString());
            } catch (Throwable ex) {
                log.error("Error while load nrf24i01 library");
                entityContext.setting().setValue(Nrf24i01StatusSetting.class, SettingPluginStatus.error(ex));
            }
        }
    }

    @Override
    public void destroy() {
        rf24Communicator.stopRunPipeReadWrite();
    }

    public void init() {
        loadLibrary();
        if (isNrf24L01Works()) {
            RaspberryGpioPin.occupyPins("NRF21I01", PIN19, PIN21, PIN22, PIN23, PIN24);

            entityContext.setting().listenValue(Nrf24i01CrcSizeSetting.class, value ->
                    rf24Communicator.getNRF24L01().setCRCLength(value.valueSupplier.get()));
            entityContext.setting().listenValue(Nrf24i01PALevelSetting.class, value ->
                    rf24Communicator.getNRF24L01().setPALevel((short) value.valueSupplier.get().swigValue()));
            entityContext.setting().listenValue(Nrf24i01RetryCountSetting.class, value ->
                    rf24Communicator.getNRF24L01().setRetries(entityContext.setting().getValue(Nrf24i01RetryDelaySetting.class).delay, value.count));
            entityContext.setting().listenValue(Nrf24i01RetryDelaySetting.class, value ->
                    rf24Communicator.getNRF24L01().setRetries(value.delay, entityContext.setting().getValue(Nrf24i01RetryCountSetting.class).count));
            entityContext.setting().listenValue(Nrf24i01DataRateSetting.class, value ->
                    rf24Communicator.getNRF24L01().setDataRate(value.valueSupplier.get()));

            rf24Communicator.subscribeForReading(new NRF24I01ReadListener());
        }
        entityContext.setFeatureState("NRF21I01", isNrf24L01Works());
        entityContext.setting().listenValue(Nrf24i01EnableButtonsSetting.class, enable -> {
            if (enable) {
                if (isNrf24L01Works()) {
                    rf24Communicator.runPipeReadWrite();
                }
            } else {
                if (!rf24Communicator.stopRunPipeReadWrite()) {
                    log.error("Unable to stop read/write threads");
                }
            }
        });
    }

    @Override
    public String getBundleId() {
        return "nrf24i01";
    }

    @Override
    public int order() {
        return 1000;
    }

    public synchronized void subscribeForReading(ReadListener readListener) {
        rf24Communicator.subscribeForReading(readListener);
    }

    public void scheduleSend(SendCommand sendCommand, RF24Message message, Pipe pipe) {
        rf24Communicator.send(sendCommand, message, pipe);
    }

    public void scheduleGlobalSend(SendCommand sendCommand, RF24Message message) {
        rf24Communicator.send(sendCommand, message, GLOBAL_WRITE_PIPE);
    }

    public SendCommand executeCommand(RF24Message message) {
        return message.getCommandPlugin().execute(message);
    }

    public RF24Message createPingCommand(ArduinoDeviceEntity arduinoDeviceEntity) {
        return NRF24i01ArduinoCommunicationProvider.generateCommand(Short.parseShort(arduinoDeviceEntity.getEntityID().substring(ArduinoDeviceRepository.PREFIX.length())));
    }

    @Override
    public Class<? extends SettingPluginStatus> getBundleStatusSetting() {
        return Nrf24i01StatusSetting.class;
    }

    private class NRF24I01ReadListener implements ReadListener {

        @Override
        public boolean canReceive(RF24Message rf24Message) {
            return rf24Message.getCommandPlugin().canReceiveGeneral();
        }

        @Override
        public void received(RF24Message rf24Message) {
            SendCommand sendCommand = rf24Message.getCommandPlugin().execute(rf24Message);
            if (sendCommand != null) {
                rf24Communicator.send(sendCommand, rf24Message, GLOBAL_WRITE_PIPE);
            }
        }

        @Override
        public void notReceived() {
            throw new IllegalStateException("Must be not called!!!");
        }

        @Override
        public String getId() {
            return "Listener";
        }
    }
}
















