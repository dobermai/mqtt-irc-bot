package de.dobermai.mqttbot;

import com.google.inject.Injector;
import com.netflix.governator.configuration.ArchaiusConfigurationProvider;
import com.netflix.governator.guice.BootstrapBinder;
import com.netflix.governator.guice.BootstrapModule;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;
import de.dobermai.mqttbot.ioc.MqttBotModule;

/**
 * @author Dominik Obermaier
 */
public class Main {

    public static void main(String[] args) throws Exception {


        final Injector injector = LifecycleInjector.builder()
                .withBootstrapModule(new BootstrapModule() {
                    @Override
                    public void configure(BootstrapBinder binder) {
                        binder.bindConfigurationProvider().to(ArchaiusConfigurationProvider.class);
                    }
                })
                .withModules(new MqttBotModule()).createInjector();

        LifecycleManager manager = injector.getInstance(LifecycleManager.class);
        manager.start();

        final BotController instance = injector.getInstance(BotController.class);

        instance.startAndWait();

    }

}
