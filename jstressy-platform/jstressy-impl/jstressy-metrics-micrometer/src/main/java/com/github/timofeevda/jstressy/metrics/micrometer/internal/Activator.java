package com.github.timofeevda.jstressy.metrics.micrometer.internal;

import com.github.timofeevda.jstressy.api.config.ConfigurationService;
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistryService;
import com.github.timofeevda.jstressy.api.vertx.VertxService;
import com.github.timofeevda.jstressy.metrics.micrometer.MicrometerMetricsRegistryService;
import com.github.timofeevda.jstressy.utils.ServiceObserver;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

public class Activator implements BundleActivator {
    private static Logger logger = LoggerFactory.getLogger(Activator.class);

    @Override
    public void start(BundleContext context) {
        logger.info("Starting metrics registry service activator");

        Single<VertxService> vertxService = ServiceObserver.observeService(VertxService.class.getName(), context);
        Single<ConfigurationService> configurationService = ServiceObserver.observeService(ConfigurationService.class.getName(), context);

        Observable.combineLatest(
                vertxService.toObservable(),
                configurationService.toObservable(),
                this::toMetricsRegistryService)
                .subscribe(metricsRegistryService -> {
                    logger.info("Registering metrics registry service");
                    context.registerService(MetricsRegistryService.class.getName(), metricsRegistryService, new Hashtable());
                });
    }

    @Override
    public void stop(BundleContext context) {

    }

    private MetricsRegistryService toMetricsRegistryService(VertxService vertxService, ConfigurationService configurationService) {
        MicrometerMetricsRegistryService metricsRegistryService = new MicrometerMetricsRegistryService();
        metricsRegistryService.publishMetrics(vertxService, configurationService);
        return metricsRegistryService;
    }

}
