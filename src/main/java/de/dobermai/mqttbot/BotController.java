package de.dobermai.mqttbot;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.AbstractIdleService;
import de.dobermai.mqttbot.config.IRCProperties;
import de.dobermai.mqttbot.config.MQTTProperties;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.*;

import javax.inject.Inject;
import java.text.MessageFormat;

/**
 * @author Dominik Obermaier
 */
public class BotController extends AbstractIdleService {

    private final CallbackConnection mqttConnection;
    private final MqttBot bot;
    private final IRCProperties ircProperties;
    private final MQTTProperties mqttProperties;

    @Inject
    public BotController(final CallbackConnection mqttConnection, final MqttBot bot,
                         final IRCProperties ircProperties, final MQTTProperties mqttProperties) {
        this.mqttConnection = mqttConnection;
        this.bot = bot;
        this.ircProperties = ircProperties;
        this.mqttProperties = mqttProperties;
    }

    @Override
    protected void startUp() throws Exception {
        addShutdownHooks();

        mqttConnection.listener(new Listener() {
            @Override
            public void onConnected() {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onDisconnected() {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onPublish(UTF8Buffer topic, Buffer body, Runnable ack) {

                final String channel = Iterables.getLast(Splitter.on("/").trimResults().split(topic.toString()));

                ack.run();
                bot.sendMessage(channel, body.utf8().toString());
            }

            @Override
            public void onFailure(Throwable value) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });


        System.out.println("Connecting to MQTT Broker");
        mqttConnection.connect(new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onFailure(Throwable value) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        System.out.println("Connecting to IRC");
        bot.connect(ircProperties.getIrcHostName(), ircProperties.getPort());

        for (String channel : ircProperties.getIrcChannels()) {
            bot.joinChannel(channel);
            System.out.println("Joined " + channel);

            System.out.print("Subscribing to MQTT topic");
            final String topic = MessageFormat.format("{0}/{1}", mqttProperties.getMqttTopicPrefix(), channel);
            mqttConnection.subscribe(new Topic[]{new Topic(topic, QoS.EXACTLY_ONCE)}, new Callback<byte[]>() {
                @Override
                public void onSuccess(byte[] value) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void onFailure(Throwable value) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }
            });
        }
        System.out.println("Connected to IRC");


    }

    @Override
    protected void shutDown() throws Exception {

        System.out.println("Disconnecting from MQTT broker");
        mqttConnection.disconnect(new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onFailure(Throwable value) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        System.out.println("Disconnecting from IRC");
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
                } catch (Exception e) {
                    //log.error("Error while shutting down:", e);
                    e.printStackTrace();
                }
            }
        }));
    }


}
