package de.dobermai.mqttbot;

import de.dobermai.mqttbot.config.IRCProperties;
import de.dobermai.mqttbot.config.MQTTProperties;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.QoS;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.Arrays;

import static com.google.common.base.Charsets.UTF_8;

/**
 * @author Dominik Obermaier
 */
public class MqttBot extends PircBot {


    private final CallbackConnection mqttConnection;
    private final MQTTProperties mqttProperties;

    @Inject
    public MqttBot(final CallbackConnection mqttConnection, final IRCProperties ircProperties, final MQTTProperties mqttProperties) throws Exception {
        this.mqttProperties = mqttProperties;

        setName(ircProperties.getNickName());
        this.mqttConnection = mqttConnection;
    }


    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
        final String messageTopic = MessageFormat.format("{0}/{1}/messages", mqttProperties.getMqttTopicPrefix(), channel);
        final String userMessage = MessageFormat.format("{0}/{1}/users/{2}", mqttProperties.getMqttTopicPrefix(), channel, sender);
        mqttConnection.publish(messageTopic, (sender + ": " + message).getBytes(UTF_8), QoS.AT_MOST_ONCE, false,new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onFailure(Throwable value) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        mqttConnection.publish(userMessage, (sender + ": " + message).getBytes(UTF_8), QoS.AT_MOST_ONCE, false,new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onFailure(Throwable value) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
    }

    @Override
    protected void onUserList(String channel, User[] users) {

        //FIXME: We should publish user lists periodically or if someone joins or leaves the channel
        final String usersTopic = MessageFormat.format("{0}/{1}/users", mqttProperties.getMqttTopicPrefix(), channel);
        mqttConnection.publish(usersTopic, (Arrays.toString(users)).getBytes(UTF_8), QoS.AT_MOST_ONCE, true,new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onFailure(Throwable value) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
    }

    @Override
    protected void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
        final String onTopicChangeTopic = MessageFormat.format("{0}/{1}/topic", mqttProperties.getMqttTopicPrefix(), channel);
        mqttConnection.publish(onTopicChangeTopic, topic.getBytes(UTF_8), QoS.AT_MOST_ONCE, true,new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onFailure(Throwable value) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
    }
}
