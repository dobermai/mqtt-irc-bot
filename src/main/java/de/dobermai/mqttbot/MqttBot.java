package de.dobermai.mqttbot;

import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.QoS;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

import javax.inject.Inject;
import java.util.Arrays;

import static com.google.common.base.Charsets.UTF_8;

/**
 * @author Dominik Obermaier
 */
public class MqttBot extends PircBot {


    private final FutureConnection mqttConnection;

    @Inject
    public MqttBot(FutureConnection mqttConnection) throws Exception {

        setName("MQTT Bot Name");
        this.mqttConnection = mqttConnection;
    }


    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
        try {
            mqttConnection.publish("irc/" + channel + "/messages", (sender + ": " + message).getBytes(UTF_8), QoS.AT_MOST_ONCE, false);
            mqttConnection.publish("irc/" + channel + "/users/" + sender, (sender + ": " + message).getBytes(UTF_8), QoS.AT_MOST_ONCE, false);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    protected void onUserList(String channel, User[] users) {

        //FIXME: We should publish user lists periodically or if someone joins or leaves the channel
        mqttConnection.publish("irc/" + channel + "/users", (Arrays.toString(users)).getBytes(UTF_8), QoS.AT_MOST_ONCE, true);
    }

    @Override
    protected void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
        mqttConnection.publish("irc/" + channel + "/topic", topic.getBytes(UTF_8), QoS.AT_MOST_ONCE, true);
    }
}
