package io.vertx.conduit;

import io.vertx.conduit.verticles.ArticleServiceVerticle;
import io.vertx.conduit.verticles.HttpVerticle;
import io.vertx.conduit.verticles.MorphiaServiceVerticle;
import io.vertx.conduit.verticles.UserServiceVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import logging.ContextLogger;

public class App extends AbstractVerticle {
    private static final String CONFIG_PATH = "app.json";
    private static Logger LOGGER = ContextLogger.create();

    @Override
    public void start(Promise<Void> startPromise) {

        getConfig().setHandler(ar1 -> {
            if (ar1.succeeded()) {
                LOGGER.info("Config successfully retrived: " + ar1.result().toString());
                DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(ar1.result());
                CompositeFuture.all(
                    deployVerticle(HttpVerticle.class, deploymentOptions),
                    deployVerticle(UserServiceVerticle.class, deploymentOptions),
                    deployVerticle(ArticleServiceVerticle.class, deploymentOptions),
                    deployVerticle(MorphiaServiceVerticle.class, deploymentOptions))
                .setHandler(ar2 -> {
                    if (ar2.succeeded()) {
                        LOGGER.info("Successfully deployed verticals.");
                        startPromise.complete();
                    } else {
                        LOGGER.error("Failed to deploy verticles: " + ar2.cause().getMessage());
                        startPromise.fail(ar2.cause());
                    }
                });
            } else {
                startPromise.fail(ar1.cause());
            }
        });
    }

    private Future<Void> deployVerticle(Class<? extends Verticle> clz, DeploymentOptions deploymentOptions){
        Promise<Void> deploymentFuture = Promise.promise();
        vertx.deployVerticle(clz, deploymentOptions, ar ->{
            if (ar.succeeded()) {
                deploymentFuture.complete();
            } else {
                deploymentFuture.fail(ar.cause());
            }
        });
        return deploymentFuture.future();
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
