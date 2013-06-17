package de.dobermai.mqttbot;

import com.google.inject.Injector;
import com.netflix.governator.configuration.PropertiesConfigurationProvider;
import com.netflix.governator.guice.BootstrapBinder;
import com.netflix.governator.guice.BootstrapModule;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;
import de.dobermai.mqttbot.ioc.MqttBotModule;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

/**
 * @author Dominik Obermaier
 */
public class Main {

    public static void main(String[] args) throws Exception {


        final Injector injector = LifecycleInjector.builder()
                .withBootstrapModule(new BootstrapModule() {
                    @Override
                    public void configure(BootstrapBinder binder) {
                        try {
                            binder.bindConfigurationProvider().toInstance(getConfigurationProvider());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                })
                .withModules(new MqttBotModule()).createInjector();

        LifecycleManager manager = injector.getInstance(LifecycleManager.class);
        manager.start();

        final BotController instance = injector.getInstance(BotController.class);

        instance.startAndWait();

    }

    private static PropertiesConfigurationProvider getConfigurationProvider() throws Exception {

        final Properties properties = new Properties();
        properties.load(new FileReader(new File(getExecutionPath(), "config.properties")));

        return new PropertiesConfigurationProvider(properties);
    }

    private static File getExecutionPath() {
        String absolutePath = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        absolutePath = absolutePath.substring(0, absolutePath.lastIndexOf("/"));
        return new File(absolutePath);
    }

}
