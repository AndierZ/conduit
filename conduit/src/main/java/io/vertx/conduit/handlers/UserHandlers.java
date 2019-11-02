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

public class UserHandlers {

    private static Logger LOGGER = ContextLogger.create();

    public static abstract class UserHandler extends BaseHandler {

        protected final UserService service;
        protected UserHandler(Vertx vertx) {
            super(vertx);
            ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(UserService.ADDRESS);
            this.service = builder.build(UserService.class);
        }
    }


    public static class RegistrationHandler extends UserHandler {

        public RegistrationHandler(Vertx vertx) {
            super(vertx);
        }

        @Override
        public void handle(RoutingContext event) {

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
    }

    public static class GetHandler extends UserHandler {

        public GetHandler(Vertx vertx) {
            super(vertx);
        }

        @Override
        public void handle(RoutingContext event) {

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
}
