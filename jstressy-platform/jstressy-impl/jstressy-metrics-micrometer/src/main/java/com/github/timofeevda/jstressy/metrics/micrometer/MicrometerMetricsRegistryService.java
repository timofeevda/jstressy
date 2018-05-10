package com.github.timofeevda.jstressy.metrics.micrometer;

import com.github.timofeevda.jstressy.api.config.ConfigurationService;
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry;
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistryService;
import com.github.timofeevda.jstressy.api.vertx.VertxService;
import io.prometheus.client.exporter.common.TextFormat;
import io.vertx.reactivex.ext.web.Router;

public class MicrometerMetricsRegistryService implements MetricsRegistryService {

    private final MicrometerMetricsRegistry metricsRegistry;

    public MicrometerMetricsRegistryService() {
        this.metricsRegistry = new MicrometerMetricsRegistry();
    }

    @Override
    public MetricsRegistry get() {
        return metricsRegistry;
    }

    public void publishMetrics(VertxService vertxService, ConfigurationService configurationService) {
        Router router = Router.router(vertxService.getVertx());
        String metricsPath = configurationService.getConfiguration().getGlobals().getStressyMetricsPath();
        router.get(metricsPath).handler(event -> {
            try {
                String metricsData = metricsRegistry.getPrometheusRegistry().scrape();
                event.response()
                        .setStatusCode(200)
                        .putHeader("Content-Type", TextFormat.CONTENT_TYPE_004)
                        .end(metricsData);
            } catch (Exception e) {
                event.fail(e);
            }
        });

        int port = configurationService.getConfiguration().getGlobals().getStressyMetricsPort();
        vertxService.getVertx().createHttpServer().requestHandler(router::accept).listen(port);
    }
}
