/*
 * Copyright 2013 Dominik Obermaier <dominik.obermaier@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        mqtt.setKeepAlive((short) mqttProperties.getMqttKeepAlive());
        mqtt.setUserName(mqttProperties.getMqttUsername());
        mqtt.setPassword(mqttProperties.getMqttPassword());

        return mqtt.callbackConnection();
    }
}
