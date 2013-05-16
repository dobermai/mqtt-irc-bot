package de.dobermai.mqttbot.ioc;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.MQTT;

import javax.inject.Singleton;

/**
 * @author Dominik Obermaier
 */
public class MqttBotModule extends AbstractModule {
    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    public FutureConnection provideMqttConnection() throws Exception {
        MQTT mqtt = new MQTT();
        mqtt.setHost("mqttdashboard.com", 1883);
        return mqtt.futureConnection();
    }
}
