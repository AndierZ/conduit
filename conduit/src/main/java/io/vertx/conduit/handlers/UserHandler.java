package io.vertx.conduit.handlers;

import LoggingUtils.ContextLogger;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.conduit.services.UserService;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.ext.web.RoutingContext;
import io.vertx.serviceproxy.ServiceProxyBuilder;

public class UserHandler extends BaseHandler {

    private static Logger LOGGER = ContextLogger.create();

    private final UserService service;
    private final JWTAuth jwtAuth;
    private final JsonObject claimJson;

    public UserHandler(Vertx vertx, JWTAuth jwtAuth) {
        super(vertx);
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(UserService.ADDRESS);
        this.jwtAuth = jwtAuth;
        this.service = builder.build(UserService.class);
        this.claimJson = new JsonObject();
    }

    public void register(RoutingContext event) {

        JsonObject message = event.getBodyAsJson().getJsonObject("user");

        service.register(message, ar -> {
            if (ar.succeeded()) {
                JsonObject userAuthJson = ar.result();
                generateJwt(userAuthJson);

                event.response()
                        .setStatusCode(HttpResponseStatus.CREATED.code())
                        .putHeader("Content-Type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(userAuthJson));

            } else {
                event.fail(new RuntimeException(ar.cause().getMessage()));
            }
        });
    }

    private void generateJwt(JsonObject user) {
        claimJson.clear();
        claimJson.put("_id", user.getString("_id"));
        claimJson.put("username", user.getString("username"));

        user.put("Bearer", jwtAuth.generateToken(claimJson, new JWTOptions().setExpiresInMinutes(60)));
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
