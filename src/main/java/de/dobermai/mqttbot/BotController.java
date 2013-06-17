package de.dobermai.mqttbot;

import com.google.common.util.concurrent.AbstractIdleService;
import de.dobermai.mqttbot.config.IRCProperties;
import org.fusesource.mqtt.client.FutureConnection;

import javax.inject.Inject;

/**
 * @author Dominik Obermaier
 */
public class BotController extends AbstractIdleService {

    private final FutureConnection mqttConnection;
    private final MqttBot bot;
    private final IRCProperties ircProperties;

    @Inject
    public BotController(final FutureConnection mqttConnection, final MqttBot bot, final IRCProperties ircProperties) {
        this.mqttConnection = mqttConnection;
        this.bot = bot;
        this.ircProperties = ircProperties;
    }

    @Override
    protected void startUp() throws Exception {
        addShutdownHooks();

        System.out.println("Connecting to MQTT Broker");
        mqttConnection.connect().await();

        System.out.println("Connecting to IRC");
        bot.connect(ircProperties.getIrcHostName(), ircProperties.getPort());

        for (String channel : ircProperties.getIrcChannels()) {
            bot.joinChannel(channel);

        }
        bot.sendMessage("#flapflup", "hallo!");
        System.out.println("Connected to IRC");

    }

    @Override
    protected void shutDown() throws Exception {

        System.out.println("Disconnecting from MQTT broker");
        if (mqttConnection.isConnected()) {
            mqttConnection.disconnect().await();
        }

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
