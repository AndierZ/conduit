package io.vertx.conduit.verticles;

import io.vertx.conduit.services.MongoDbService;
import io.vertx.conduit.services.MongoDbServiceImpl;
import io.vertx.core.Future;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.mongo.MongoClient;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.serviceproxy.ServiceBinder;
import logging.ContextLogger;

public class MongoDbServiceVerticle extends AbstractVerticle {

    private static Logger LOGGER = ContextLogger.create();

    private Record record;
    private ServiceBinder binder;
    private MessageConsumer<JsonObject> consumer;

    @Override
    public void start(Future<Void> startFuture) {

        final MongoClient mongoClient = MongoClient.createShared(vertx, config().getJsonObject("mongodb"));
        ServiceDiscovery.create(vertx.getDelegate(), discovery -> {
            binder = new ServiceBinder(vertx.getDelegate());
            // Create the services object
            new MongoDbServiceImpl(mongoClient, ready -> {
                if (ready.succeeded()) {
                    this.consumer = binder
                            .setAddress(MongoDbService.ADDRESS)
                            .register(MongoDbService.class, ready.result());
                    Record record = EventBusService.createRecord("mongodb", MongoDbService.ADDRESS, MongoDbService.class.getName());
                    discovery.publish(record, ar -> {
                        if (ar.succeeded()) {
                            this.record = record;
                            LOGGER.info("MongoDb service published");

                        } else {
                            LOGGER.error("Error publishing MongoDb service", ar.cause());
                        }
                    });
                } else {
                    startFuture.fail(ready.cause());
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
