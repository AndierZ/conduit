package io.vertx.conduit.verticles;

import io.vertx.conduit.handlers.Constants;
import io.vertx.conduit.services.CommentService;
import io.vertx.conduit.services.CommentServiceImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.serviceproxy.ServiceBinder;
import logging.ContextLogger;

public class CommentServiceVerticle extends AbstractVerticle {

    private static Logger LOGGER = ContextLogger.create();

    private Record record;
    private ServiceBinder binder;
    private MessageConsumer<JsonObject> consumer;

    @Override
    public void start(Promise<Void> startPromise) {

        ServiceDiscovery.create(vertx, discovery -> {
            binder = new ServiceBinder(vertx);
            // Create the services object
            CommentServiceImpl service = new CommentServiceImpl(vertx);

            // Register the services proxy on the event bus
            this.consumer = binder
                    .setAddress(CommentService.ADDRESS)
                    .register(CommentService.class, service);

            Record record = EventBusService.createRecord(Constants.COMMENT, CommentService.ADDRESS, CommentService.class.getName());
            discovery.publish(record, ar -> {
                if (ar.succeeded()) {
                    this.record = record;
                    LOGGER.info("Comment service published");
                    startPromise.complete();
                } else {
                    LOGGER.error("Error publishing comment service", ar.cause());
                    startPromise.fail(ar.cause());
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
