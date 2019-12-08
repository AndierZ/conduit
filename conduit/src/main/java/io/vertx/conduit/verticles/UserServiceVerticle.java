package io.vertx.conduit.verticles;

import io.vertx.conduit.handlers.UserHandler;
import io.vertx.core.Promise;
import logging.ContextLogger;
import io.vertx.conduit.services.UserService;
import io.vertx.conduit.services.UserServiceImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.serviceproxy.ServiceBinder;

public class UserServiceVerticle extends AbstractVerticle {

    private static Logger LOGGER = ContextLogger.create();

    private Record record;
    private ServiceBinder binder;
    private MessageConsumer<JsonObject> consumer;

    @Override
    public void start(Promise<Void> startPromise) {

        ServiceDiscovery.create(vertx, discovery -> {
            binder = new ServiceBinder(vertx);
            // Create the services object
            UserServiceImpl service = new UserServiceImpl(vertx);

            // Register the services proxy on the event bus
            this.consumer = binder
                    .setAddress(UserService.ADDRESS)
                    .register(UserService.class, service);

            Record record = EventBusService.createRecord(UserHandler.USER, UserService.ADDRESS, UserService.class.getName());
            discovery.publish(record, ar -> {
                if (ar.succeeded()) {
                    this.record = record;
                    LOGGER.info("User service published");
                    startPromise.complete();
                } else {
                    LOGGER.error("Error publishing user service", ar.cause());
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
