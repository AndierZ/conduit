package io.vertx.conduit.handlers;

import LoggingUtils.ContextLogger;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.conduit.services.UserService;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.ext.web.RoutingContext;
import io.vertx.serviceproxy.ServiceProxyBuilder;

public class UserHandler extends BaseHandler {

    private static Logger LOGGER = ContextLogger.create();

    private final UserService service;

    public UserHandler(Vertx vertx) {
        super(vertx);
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(UserService.ADDRESS);
        this.service = builder.build(UserService.class);
    }

    public void register(RoutingContext event) {

        JsonObject message = event.getBodyAsJson().getJsonObject("user");

        service.register(message, ar -> {
            if (ar.succeeded()) {
                JsonObject userAuthJson = ar.result();

                event.response()
                        .setStatusCode(HttpResponseStatus.CREATED.code())
                        .putHeader("Content-Type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(userAuthJson));

            } else {
                event.fail(new RuntimeException(ar.cause().getMessage()));
            }
        });
    }

    public void get(RoutingContext event) {

        JsonObject message = event.getBodyAsJson().getJsonObject("user");

        service.get(message.getString("username"), ar -> {
            if (ar.succeeded()) {
                JsonObject userAuthJson = ar.result();

                event.response()
                        .setStatusCode(HttpResponseStatus.CREATED.code())
                        .putHeader("Content-Type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(userAuthJson));

            } else {
                event.fail(new RuntimeException(ar.cause().getMessage()));
            }
        });
    }
}
