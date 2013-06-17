package de.dobermai.mqttbot.ioc;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import de.dobermai.mqttbot.config.MQTTProperties;
import org.fusesource.mqtt.client.CallbackConnection;
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
    public CallbackConnection provideMqttConnection(final MQTTProperties mqttProperties) throws Exception {
        MQTT mqtt = new MQTT();
        mqtt.setHost(mqttProperties.getBrokerHost(), mqttProperties.getBrokerPort());
        mqtt.setCleanSession(mqttProperties.isMqttcleanSession());
        mqtt.setClientId(mqttProperties.getMqttClientId());
        mqtt.setKeepAlive(mqtt.getKeepAlive());
        mqtt.setUserName(mqttProperties.getMqttUsername());
        mqtt.setPassword(mqttProperties.getMqttPassword());

        return mqtt.callbackConnection();
    }
}
