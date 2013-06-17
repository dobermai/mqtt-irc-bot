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

package de.dobermai.mqttbot.config;

import com.netflix.governator.annotations.Configuration;

import javax.inject.Singleton;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * @author Dominik Obermaier
 */
@Singleton
public class MQTTProperties {

    MQTTProperties() {
        //Do not instantiate by your onw. Inject it!
    }

    @Configuration("broker.host")
    private String brokerHost = "broker.mqttdashboard.com";

    @Configuration("broker.port")
    @Min(1)
    @Max(65535)
    private int brokerPort = 1883;

    @Configuration("mqtt.keepAlive")
    @Min(3)
    private int mqttKeepAlive = 60;

    @Configuration("mqtt.clientId")
    private String mqttClientId = "mqtt-bot";

    @Configuration(value = "mqtt.cleanSession", ignoreTypeMismatch = true)
    private boolean mqttcleanSession = true;

    @Configuration("mqtt.username")
    private String mqttUsername = "";

    @Configuration("mqtt.password")
    private String mqttPassword = "";

    @Configuration("mqtt.topicPrefix")
    private String mqttTopicPrefix = "irc";

    public String getBrokerHost() {
        return brokerHost;
    }

    public int getBrokerPort() {
        return brokerPort;
    }

    public int getMqttKeepAlive() {
        return mqttKeepAlive;
    }

    public String getMqttClientId() {
        return mqttClientId;
    }

    public boolean isMqttcleanSession() {
        return mqttcleanSession;
    }

    public String getMqttUsername() {
        return mqttUsername;
    }

    public String getMqttPassword() {
        return mqttPassword;
    }

    public String getMqttTopicPrefix() {
        return mqttTopicPrefix;
    }
}
