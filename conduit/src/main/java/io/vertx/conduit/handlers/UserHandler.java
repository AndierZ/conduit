package io.vertx.conduit.handlers;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.conduit.entities.User;
import io.vertx.conduit.services.UserService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.ext.web.RoutingContext;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import logging.ContextLogger;
import org.springframework.security.crypto.bcrypt.BCrypt;
import routerutils.BaseHandler;
import routerutils.RouteConfig;

@RouteConfig(path="/api", produces = "application/json")
public class UserHandler extends BaseHandler {

    private static Logger LOGGER = ContextLogger.create();
    private static String USER = "user";

    private final UserService userService;
    private final JWTAuth jwtAuth;

    public UserHandler(Vertx vertx, JWTAuth jwtAuth) {
        super(vertx);
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(UserService.ADDRESS);
        this.jwtAuth = jwtAuth;
        this.userService = builder.build(UserService.class);
    }

    private void appendJwt(JsonObject user, String id) {
        JsonObject principal = new JsonObject();
        principal.put("_id", id);
        principal.put("username", user.getString("username"));
        user.put("Bearer", jwtAuth.generateToken(principal, new JWTOptions().setExpiresInMinutes(120)));
    }

    @RouteConfig(path="/users/login", method=HttpMethod.POST, authRequired=false)
    public void login(RoutingContext event) {
        JsonObject message = event.getBodyAsJson().getJsonObject(USER);
        userService.login(message.getString("email"), ar -> {
            if (ar.succeeded()) {
                String hashed = ar.result().getPasswordHash();
                if (BCrypt.checkpw(message.getString("password"), hashed)) {
                    JsonObject userAuthJson = ar.result().toAuthJson();
                    appendJwt(userAuthJson, ar.result().get_id());
                    event.response()
                            .setStatusCode(HttpResponseStatus.CREATED.code())
                            .end(Json.encodePrettily(userAuthJson));
                } else {
                    event.fail(new RuntimeException("Invalid user credentials"));
                }
            } else {
                event.fail(ar.cause());
            }
        });
    }

    @RouteConfig(path="/users", method=HttpMethod.POST, authRequired=false)
    public void register(RoutingContext event) {
        JsonObject message = event.getBodyAsJson().getJsonObject(USER);
        User user = new User(message);
        user.setPassword(message.getString("password"));
        userService.register(user, ar -> handle(event, ar));
    }

    @RouteConfig(path="/user", method = HttpMethod.POST)
    public void put(RoutingContext event) {
        JsonObject message = event.getBodyAsJson().getJsonObject(USER);
        User user = new User(message);
        userService.put(event.get("userId"), user, ar -> handle(event, ar));
    }

    @RouteConfig(path="/user")
    public void get(RoutingContext event) {
        userService.getById(event.get("userId"), ar -> handle(event, ar));
    }

    @RouteConfig(path="/:username", authRequired = false)
    public void getProfile(RoutingContext event) {

        String username = event.request().getParam("username");
        String queryingUserId = event.get("userId");

        if (queryingUserId != null) {
            userService.get(new JsonObject().put("_id", queryingUserId), ar -> {
                if (ar.succeeded()) {
                    User queryingUser = ar.result();
                    userService.get(new JsonObject().put("username", username), ar2 -> {
                        if (ar2.succeeded()) {
                            event.response()
                                    .setStatusCode(HttpResponseStatus.OK.code())
                                    .end(Json.encodePrettily(ar2.result().toProfileJsonFor(queryingUser)));
                        } else {
                            event.fail(ar2.cause());
                        }
                    });
                } else {
                    event.fail(ar.cause());
                }
            });
        } else {
            userService.get(new JsonObject().put("username", username), ar2 -> {
                if (ar2.succeeded()) {
                    event.response()
                            .setStatusCode(HttpResponseStatus.OK.code())
                            .end(Json.encodePrettily(ar2.result().toProfileJsonFor(null)));
                } else {
                    event.fail(ar2.cause());
                }
            });
        }
    }

    private void handle(RoutingContext event, AsyncResult<User> ar) {
        if (ar.succeeded()) {
            JsonObject userAuthJson = ar.result().toAuthJson();
            event.response()
                    .setStatusCode(HttpResponseStatus.CREATED.code())
                    .end(Json.encodePrettily(userAuthJson));

        } else {
            event.fail(ar.cause());
        }
    }
}
