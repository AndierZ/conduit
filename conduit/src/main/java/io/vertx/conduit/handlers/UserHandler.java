package io.vertx.conduit.handlers;

import io.vertx.conduit.entities.User;
import logging.ContextLogger;
import routerutils.RouteConfig;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.conduit.services.UserService;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.ext.web.RoutingContext;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import routerutils.BaseHandler;

@RouteConfig(path="/api", produces = "application/json")
public class UserHandler extends BaseHandler {

    private static Logger LOGGER = ContextLogger.create();

    private final UserService userService;
    private final JWTAuth jwtAuth;

    public UserHandler(Vertx vertx, JWTAuth jwtAuth) {
        super(vertx);
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(UserService.ADDRESS);
        this.jwtAuth = jwtAuth;
        this.userService = builder.build(UserService.class);
    }

    @RouteConfig(path="/users", method= HttpMethod.POST, authRequired = false)
    public void register(RoutingContext event) {

        JsonObject message = event.getBodyAsJson().getJsonObject("user");
        User user = new User(message);

        userService.register(user, ar -> {
            if (ar.succeeded()) {
                JsonObject userAuthJson = ar.result();
                generateJwt(userAuthJson);

                event.response()
                        .setStatusCode(HttpResponseStatus.CREATED.code())
                        .end(Json.encodePrettily(userAuthJson));

            } else {
                event.fail(new RuntimeException(ar.cause().getMessage()));
            }
        });
    }

    private void generateJwt(JsonObject user) {
        JsonObject claimJson = new JsonObject();
        claimJson.put("_id", user.getString("_id"));
        claimJson.put("username", user.getString("username"));

        user.put("Bearer", jwtAuth.generateToken(claimJson, new JWTOptions().setExpiresInMinutes(120)));
    }

    @RouteConfig(path="/user")
    public void get(RoutingContext event) {

        JsonObject message = event.getBodyAsJson().getJsonObject("user");

        userService.get(message.getString("username"), ar -> {
            if (ar.succeeded()) {
                JsonObject userAuthJson = ar.result();

                event.response()
                        .setStatusCode(HttpResponseStatus.CREATED.code())
                        .end(Json.encodePrettily(userAuthJson));

            } else {
                event.fail(new RuntimeException(ar.cause().getMessage()));
            }
        });
    }
}
