package de.dobermai.mqttbot;

import com.google.common.util.concurrent.AbstractIdleService;
import de.dobermai.mqttbot.config.IRCProperties;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;

import javax.inject.Inject;

/**
 * @author Dominik Obermaier
 */
public class BotController extends AbstractIdleService {

    private final CallbackConnection mqttConnection;
    private final MqttBot bot;
    private final IRCProperties ircProperties;

    @Inject
    public BotController(final CallbackConnection mqttConnection, final MqttBot bot, final IRCProperties ircProperties) {
        this.mqttConnection = mqttConnection;
        this.bot = bot;
        this.ircProperties = ircProperties;
    }

    @Override
    protected void startUp() throws Exception {
        addShutdownHooks();

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
