package de.dobermai.mqttbot;

import de.dobermai.mqttbot.config.IRCProperties;
import de.dobermai.mqttbot.config.MQTTProperties;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.QoS;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.Arrays;

import static com.google.common.base.Charsets.UTF_8;

/**
 * @author Dominik Obermaier
 */
public class MqttBot extends PircBot {

    private static final Logger log = LoggerFactory.getLogger(MqttBot.class);


    private final CallbackConnection mqttConnection;
    private final MQTTProperties mqttProperties;

    @Inject
    public MqttBot(final CallbackConnection mqttConnection, final IRCProperties ircProperties, final MQTTProperties mqttProperties) throws Exception {
        this.mqttProperties = mqttProperties;

        log.info("Using IRC Nickname {}", ircProperties.getNickName());
        setName(ircProperties.getNickName());
        this.mqttConnection = mqttConnection;
    }


    @Override
    protected void onMessage(final String channel, final String sender, final String login, final String hostname, final String message) {
        final String messageTopic = MessageFormat.format("{0}/{1}/messages", mqttProperties.getMqttTopicPrefix(), channel);
        final String userMessage = MessageFormat.format("{0}/{1}/users/{2}", mqttProperties.getMqttTopicPrefix(), channel, sender);
        mqttConnection.publish(messageTopic, (sender + ": " + message).getBytes(UTF_8), QoS.AT_MOST_ONCE, false, new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                log.debug("Published message \"{}\" to {}", message, messageTopic);
            }

            @Override
            public void onFailure(Throwable value) {
                log.error("An error occured while publishing:", value);
            }
        });
        mqttConnection.publish(userMessage, (sender + ": " + message).getBytes(UTF_8), QoS.AT_MOST_ONCE, false, new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                log.debug("Published message \"{}\" to {}", message, userMessage);
            }

            @Override
            public void onFailure(Throwable value) {
                log.error("An error occured while publishing:", value);
            }
        });
    }

    @Override
    protected void onUserList(String channel, User[] users) {

        //FIXME: We should publish user lists periodically or if someone joins or leaves the channel
        final String usersTopic = MessageFormat.format("{0}/{1}/users", mqttProperties.getMqttTopicPrefix(), channel);
        final String message = Arrays.toString(users);
        mqttConnection.publish(usersTopic, message.getBytes(UTF_8), QoS.AT_MOST_ONCE, true, new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                log.debug("Published message \"{}\" to {}", message, usersTopic);
            }

            @Override
            public void onFailure(Throwable value) {
                log.error("An error occured while publishing:", value);
            }
        });
    }

    @Override
    protected void onTopic(final String channel, final String topic, final String setBy, final long date, final boolean changed) {
        final String onTopicChangeTopic = MessageFormat.format("{0}/{1}/topic", mqttProperties.getMqttTopicPrefix(), channel);
        mqttConnection.publish(onTopicChangeTopic, topic.getBytes(UTF_8), QoS.AT_MOST_ONCE, true, new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                log.debug("Published message \"{}\" to {}", topic, onTopicChangeTopic);
            }

            @Override
            public void onFailure(Throwable value) {
                log.error("An error occured while publishing:", value);
            }
        });
    }
}
