package io.vertx.conduit.verticles;

import LoggingUtils.ContextLogger;
import io.vertx.conduit.handlers.UserHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class HttpVerticle extends AbstractVerticle {

    private static Logger LOGGER = ContextLogger.create();

    @Override
    public void start(Future<Void> startFuture) {
        LOGGER.info("Starting Http Verticle with config {}", "test");

        Router baseRouter = Router.router(vertx);

        baseRouter.route("/").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.putHeader("content-type", "text/plain").end("Hello Vert.x!");
        });

        Router apiRouter = Router.router(vertx);
        UserHandler userHandler = new UserHandler(vertx);

        apiRouter.route().handler(BodyHandler.create());
        apiRouter.route(HttpMethod.POST, "/users").produces("application/json").handler(userHandler::register);
        apiRouter.route(HttpMethod.GET, "/user").produces("application/json").handler(userHandler::get);

        baseRouter.mountSubRouter("/api", apiRouter);

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
