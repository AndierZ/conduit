package io.vertx.conduit.verticles;

import io.vertx.conduit.handlers.ArticleHandler;
import io.vertx.conduit.handlers.JwtOptionalHandler;
import logging.ContextLogger;
import io.vertx.conduit.handlers.UserHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import routerutils.RouteBuilder;

public class HttpVerticle extends AbstractVerticle {

    private static Logger LOGGER = ContextLogger.create();
    private JWTAuth jwtAuth;

    @Override
    public void start(Future<Void> startFuture) {
        LOGGER.info("Starting Http Verticle with config {}", "test");

        JsonObject config = config();

        JWTAuthOptions jwtAuthOptions = new JWTAuthOptions().addPubSecKey(
                new PubSecKeyOptions()
                        .setAlgorithm("HS256")
                        .setPublicKey(config.getString("secret"))
                        .setSymmetric(true));

        this.jwtAuth = JWTAuth.create(vertx, jwtAuthOptions);

        Router baseRouter = Router.router(vertx);

        new RouteBuilder(baseRouter)
                .addAuthHandler(JWTAuthHandler.create(jwtAuth))
                .addPreHandler(BodyHandler.create())
                .addPreHandler(new JwtOptionalHandler(jwtAuthOptions))
                .add(new UserHandler(vertx, jwtAuth))
                .add(new ArticleHandler(vertx))
                .build();

        vertx.createHttpServer()
                .requestHandler(baseRouter)
                .listen(3000, ar -> {
                    if (ar.succeeded()) {
                        startFuture.complete();
                    } else {
                        startFuture.fail(ar.cause());
                    }
                });
    }
}
