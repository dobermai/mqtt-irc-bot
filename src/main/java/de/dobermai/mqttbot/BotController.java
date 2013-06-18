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

package de.dobermai.mqttbot;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.AbstractIdleService;
import com.netflix.governator.lifecycle.LifecycleManager;
import de.dobermai.mqttbot.config.IRCProperties;
import de.dobermai.mqttbot.config.MQTTProperties;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.text.MessageFormat;

/**
 * @author Dominik Obermaier
 */
public class BotController extends AbstractIdleService {


    private static final Logger log = LoggerFactory.getLogger(BotController.class);

    private final CallbackConnection mqttConnection;
    private final MqttBot bot;
    private final IRCProperties ircProperties;
    private final MQTTProperties mqttProperties;
    private final LifecycleManager lifecycleManager;

    @Inject
    public BotController(final CallbackConnection mqttConnection, final MqttBot bot,
                         final IRCProperties ircProperties, final MQTTProperties mqttProperties,
                         final LifecycleManager lifecycleManager) {
        this.mqttConnection = mqttConnection;
        this.bot = bot;
        this.ircProperties = ircProperties;
        this.mqttProperties = mqttProperties;
        this.lifecycleManager = lifecycleManager;
    }

    @Override
    protected void startUp() throws Exception {
        addShutdownHooks();

        mqttConnection.listener(new Listener() {
            @Override
            public void onConnected() {
                log.info("Connected to MQTT broker successfully");
            }

            @Override
            public void onDisconnected() {
                log.info("Disconnected from MQTT broker");
            }

            @Override
            public void onPublish(UTF8Buffer topic, Buffer body, Runnable ack) {

                final String channel = Iterables.getLast(Splitter.on("/").trimResults().split(topic.toString())).replace(mqttProperties.getMqttIrcChannelPrefix(),"#");

                ack.run();
                log.debug("Received message on topic {} with payload {}. Writing to IRC channel {}", topic.toString(), body.utf8().toString(), channel);
                bot.sendMessage(channel, body.utf8().toString());
            }

            @Override
            public void onFailure(Throwable value) {
                log.error("An error occured", value);
            }
        });


        log.info("Connecting to MQTT Broker");
        mqttConnection.connect(new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                log.debug("Connected to MQTT broker successfully");
            }

            @Override
            public void onFailure(Throwable value) {
                log.error("Could not connect to MQTT broker.", value);
            }
        });

        log.info("Connecting to IRC {} on port {}", ircProperties.getIrcHostName(), ircProperties.getPort());
        bot.connect(ircProperties.getIrcHostName(), ircProperties.getPort());

        for (String channel : ircProperties.getIrcChannels()) {
            bot.joinChannel(channel);
            log.info("Joined channel {}", channel);

            final String topic = replaceChannelPrefixes(MessageFormat.format("{0}/{1}", mqttProperties.getMqttTopicPrefix(), channel));
            log.info("Subscribing to MQTT topic {}", topic);
            mqttConnection.subscribe(new Topic[]{new Topic(topic, QoS.EXACTLY_ONCE)}, new Callback<byte[]>() {
                @Override
                public void onSuccess(byte[] value) {
                    log.debug("Successfully subscribed to {}", topic);
                }

                @Override
                public void onFailure(Throwable value) {
                    log.error("There was an error connecting to topic {}", topic, value);
                }
            });
        }
        log.info("Connected to IRC {} successfully", ircProperties.getIrcHostName());


    }

    @Override
    protected void shutDown() throws Exception {

        log.info("Disconnecting from MQTT broker");
        mqttConnection.disconnect(new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                log.info("Disconnected from MQTT broker successfully");
            }

            @Override
            public void onFailure(Throwable value) {
                log.warn("There was an error while disconnecting from the MQTT broker", value);
            }
        });

        log.info("Disconnecting from IRC");
        if (bot.isConnected()) {
            bot.disconnect();
        }
    }

    private void addShutdownHooks() {

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    shutDown();
                    log.debug("Shutting down LifecycleManager");
                    lifecycleManager.close();

                } catch (Exception e) {
                    //log.error("Error while shutting down:", e);
                    e.printStackTrace();
                }
            }
        }));
    }

    private String replaceChannelPrefixes(final String channel) {
        return channel.replace("#", mqttProperties.getMqttIrcChannelPrefix());
    }

}
