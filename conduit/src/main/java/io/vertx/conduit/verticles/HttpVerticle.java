package io.vertx.conduit.verticles;

import LoggingUtils.ContextLogger;
import annotation.HandlerProcessor;
import io.vertx.conduit.handlers.BaseHandler;
import io.vertx.conduit.handlers.UserHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;

import java.util.ArrayList;
import java.util.List;

public class HttpVerticle extends AbstractVerticle {

    private static Logger LOGGER = ContextLogger.create();
    private JWTAuth jwtAuth;

    @Override
    public void start(Future<Void> startFuture) {
        LOGGER.info("Starting Http Verticle with config {}", "test");

        JsonObject config = config();

        this.jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions().addPubSecKey(
                new PubSecKeyOptions()
                        .setAlgorithm("HS256")
                        .setPublicKey(config.getString("secret"))
                        .setSymmetric(true)));

        Router baseRouter = Router.router(vertx);

        baseRouter.route("/").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.putHeader("content-type", "text/plain").end("Hello Vert.x!");
        });

        List<BaseHandler> handlers = new ArrayList<>();
        handlers.add(new UserHandler(vertx, jwtAuth));

        for(BaseHandler handler : handlers) {
            HandlerProcessor.buildHandler(baseRouter, BodyHandler.create(), handler, JWTAuthHandler.create(jwtAuth));
        }

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
