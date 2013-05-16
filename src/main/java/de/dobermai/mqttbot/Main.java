package de.dobermai.mqttbot;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Injector;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;
import de.dobermai.mqttbot.ioc.MqttBotModule;
import org.fusesource.mqtt.client.FutureConnection;

import javax.inject.Inject;

/**
 * @author Dominik Obermaier
 */
public class Main extends AbstractIdleService {

    private static final String IRC_FREENODE_NET = "irc.freenode.net";
    private final FutureConnection mqttConnection;
    private final MqttBot bot;
    private static LifecycleManager manager;

    @Inject
    public Main(FutureConnection mqttConnection, MqttBot bot) {
        this.mqttConnection = mqttConnection;
        this.bot = bot;
    }

    @Override
    protected void startUp() throws Exception {
        addShutdownHooks();

        System.out.println("Connecting to MQTT Broker");
        mqttConnection.connect().await();

        System.out.println("Connecting to IRC");
        bot.connect(IRC_FREENODE_NET);
        bot.joinChannel("#flapflup");
        bot.sendMessage("#flapflup", "hallo!");
        System.out.println("Connected to IRC");

    }

    @Override
    protected void shutDown() throws Exception {

        manager.close();

        System.out.println("Disconnecting from MQTT broker");
        if (mqttConnection.isConnected()) {
            mqttConnection.disconnect().await();
        }

        System.out.println("Disconnecting from IRC");
        if (bot.isConnected()) {
            bot.disconnect();
        }
    }

    public static void main(String[] args) throws Exception {


        final Injector injector = LifecycleInjector.builder()
                .withModules(new MqttBotModule()).createInjector();

        manager = injector.getInstance(LifecycleManager.class);
        manager.start();

        final Main instance = injector.getInstance(Main.class);

        instance.startAndWait();
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
