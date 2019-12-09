package io.vertx.conduit.verticles;

import io.vertx.conduit.handlers.ArticleHandler;
import io.vertx.conduit.services.ArticleService;
import io.vertx.conduit.services.ArticleServiceImpl;
import io.vertx.conduit.services.UserService;
import io.vertx.conduit.services.UserServiceImpl;
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

public class ArticleServiceVerticle extends AbstractVerticle {

    private static Logger LOGGER = ContextLogger.create();

    private Record record;
    private ServiceBinder binder;
    private MessageConsumer<JsonObject> consumer;

    @Override
    public void start(Promise<Void> startPromise) {

        ServiceDiscovery.create(vertx, discovery -> {
            binder = new ServiceBinder(vertx);
            // Create the services object
            ArticleServiceImpl service = new ArticleServiceImpl(vertx);

            // Register the services proxy on the event bus
            this.consumer = binder
                    .setAddress(ArticleService.ADDRESS)
                    .register(ArticleService.class, service);

            Record record = EventBusService.createRecord(ArticleHandler.ARTICLE, ArticleService.ADDRESS, ArticleService.class.getName());
            discovery.publish(record, ar -> {
                if (ar.succeeded()) {
                    this.record = record;
                    LOGGER.info("Article service published");
                    startPromise.complete();
                } else {
                    LOGGER.error("Error publishing article service", ar.cause());
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
