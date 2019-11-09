package io.vertx.conduit;

import io.vertx.conduit.services.MongoDbService;
import io.vertx.conduit.verticles.MongoDbServiceVerticle;
import logging.ContextLogger;
import io.vertx.conduit.verticles.HttpVerticle;
import io.vertx.conduit.verticles.UserServiceVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;

public class App extends AbstractVerticle {
    private static final String CONFIG_PATH = "app.json";
    private static Logger LOGGER = ContextLogger.create();

    @Override
    public void start(Future<Void> startFuture) {

        getConfig().setHandler(ar1 -> {
            if (ar1.succeeded()) {
                LOGGER.info("Config successfully retrived: " + ar1.result().toString());
                DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(ar1.result());
                CompositeFuture.all(
                    deployVerticle(HttpVerticle.class, deploymentOptions),
                    deployVerticle(UserServiceVerticle.class, deploymentOptions),
                        deployVerticle(MongoDbServiceVerticle.class, deploymentOptions))
                .setHandler(ar2 -> {
                    if (ar2.succeeded()) {
                        LOGGER.info("Successfully deployed verticals.");
                        startFuture.complete();
                    } else {
                        LOGGER.error("Failed to deploy verticles: " + ar2.cause().getMessage());
                        startFuture.fail(ar2.cause());
                    }
                });
            } else {
                startFuture.fail(ar1.cause());
            }
        });
    }

    private Future<Void> deployVerticle(Class clz, DeploymentOptions deploymentOptions){
        Future<Void> deploymentFuture = Future.future();
        vertx.deployVerticle(clz, deploymentOptions, ar ->{
            if (ar.succeeded()) {
                deploymentFuture.complete();
            } else {
                deploymentFuture.fail(ar.cause());
            }
        });
        return deploymentFuture;
    }

    private Future<JsonObject> getConfig() {
        ConfigStoreOptions co = new ConfigStoreOptions().
                setType("file").
                setFormat("json").
                setConfig(new JsonObject().put("path", CONFIG_PATH));

        ConfigRetrieverOptions cro = new ConfigRetrieverOptions().
                addStore(co);

        ConfigRetriever cr = ConfigRetriever.create(vertx, cro);

        return ConfigRetriever.getConfigAsFuture(cr);
    }
}
