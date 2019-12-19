package io.vertx.conduit.verticles;

import io.vertx.conduit.handlers.*;
import io.vertx.core.Promise;
import logging.ContextLogger;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import routerutils.RouteBuilder;

public class HttpVerticle extends AbstractVerticle {

    private static Logger LOGGER = ContextLogger.create();
    private JWTAuth jwtAuth;

    @Override
    public void start(Promise<Void> startPromise) {
        LOGGER.info("Starting Http Verticle");

        JsonObject config = config();

        JWTAuthOptions jwtAuthOptions = new JWTAuthOptions().addPubSecKey(
                new PubSecKeyOptions()
                        .setAlgorithm("HS256")
                        .setPublicKey(config.getString("secret"))
                        .setSymmetric(true));

        this.jwtAuth = JWTAuth.create(vertx, jwtAuthOptions);

        Router baseRouter = Router.router(vertx);

        new RouteBuilder(baseRouter)
                .addAuthHandler(new ConduitJwtAuthHandlerImpl(jwtAuth, Constants.AUTH_HEADER)) // To expect the header value defined by the Conduit api "token". Otherwise could just use JWTAuthHandler.create(jwtAuth)
                .addPreHandler(BodyHandler.create())
                .addPreHandler(new JwtOptionalHandler(jwtAuthOptions))
                .add(new UserHandler(vertx, jwtAuth))
                .add(new ArticleHandler(vertx))
                .add(new QueryHandler(vertx))
                .build();

        vertx.createHttpServer()
                .requestHandler(baseRouter)
                .listen(3000, ar -> {
                    if (ar.succeeded()) {
                        startPromise.complete();
                    } else {
                        startPromise.fail(ar.cause());
                    }
                });
    }
}
