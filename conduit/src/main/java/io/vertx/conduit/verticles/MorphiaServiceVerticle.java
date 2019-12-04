package io.vertx.conduit.verticles;

import io.vertx.conduit.services.MorphiaService;
import io.vertx.conduit.services.MorphiaServiceImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.serviceproxy.ServiceBinder;
import logging.ContextLogger;

public class MorphiaServiceVerticle extends AbstractVerticle {

    private static Logger LOGGER = ContextLogger.create();

    private Record record;
    private ServiceBinder binder;
    private MessageConsumer<JsonObject> consumer;

    @Override
    public void start(Promise<Void> startPromise) {

        ServiceDiscovery.create(vertx, discovery -> {
            binder = new ServiceBinder(vertx);
            // Create the services object
            new MorphiaServiceImpl(vertx, config().getJsonObject("mongodb"), ready -> {
                if (ready.succeeded()) {
                    this.consumer = binder
                            .setAddress(MorphiaService.ADDRESS)
                            .register(MorphiaService.class, ready.result());
                    Record record = EventBusService.createRecord("morphia", MorphiaService.ADDRESS, MorphiaService.class.getName());
                    discovery.publish(record, ar -> {
                        if (ar.succeeded()) {
                            this.record = record;
                            LOGGER.info("Morphia service published");
                            startPromise.complete();
                        } else {
                            LOGGER.error("Error publishing Morphia service", ar.cause());
                            startPromise.fail(ar.cause());
                        }
                    });
                } else {
                    startPromise.fail(ready.cause());
                }
            });
        });


    }

    @Override
    public void stop() {
        if (record != null) {
            binder.unregister(consumer);
        }
    }
}
