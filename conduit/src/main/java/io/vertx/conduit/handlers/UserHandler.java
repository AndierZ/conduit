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

import java.util.List;

@RouteConfig(path="/api", produces = "application/json")
public class UserHandler extends BaseHandler {

    private static Logger LOGGER = ContextLogger.create();
    private static String USER = "user";

    private final io.vertx.conduit.services.reactivex.UserService userService;
    private final JWTAuth jwtAuth;

    public UserHandler(Vertx vertx, JWTAuth jwtAuth) {
        super(vertx);
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(UserService.ADDRESS);
        this.jwtAuth = jwtAuth;
        UserService delegate = builder.build(UserService.class);
        userService = new io.vertx.conduit.services.reactivex.UserService(delegate);
    }

    private void appendJwt(JsonObject user, String id) {
        JsonObject principal = new JsonObject();
        principal.put("_id", id);
        principal.put("username", user.getString("username"));
        user.put("Bearer", jwtAuth.generateToken(principal, new JWTOptions().setExpiresInMinutes(120)));
    }

    @RouteConfig(path="/users/getByEmail", method=HttpMethod.POST, authRequired=false)
    public void login(RoutingContext event) {
        JsonObject message = event.getBodyAsJson().getJsonObject(USER);
        userService.getByEmail(message.getString("email"), ar -> {
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

        userService.rxGetByEmail(message.getString("email"))
                .subscribe((res, ex) -> {
                    if (ex != null) {
                        event.fail(new RuntimeException("Invalid user credentials"));
                    } else {
                        String hashed = res.getPasswordHash();
                        if (BCrypt.checkpw(message.getString("password"), hashed)) {
                            JsonObject userAuthJson = res.toAuthJson();
                            appendJwt(userAuthJson, res.get_id());
                            event.response()
                                    .setStatusCode(HttpResponseStatus.CREATED.code())
                                    .end(Json.encodePrettily(userAuthJson));
                        } else {
                            event.fail(new RuntimeException("Invalid user credentials"));
                        }
                    }
                });
    }

    @RouteConfig(path="/users", method=HttpMethod.POST, authRequired=false)
    public void register(RoutingContext event) {
        JsonObject message = event.getBodyAsJson().getJsonObject(USER);
        User user = new User(message);
        user.setPassword(message.getString("password"));
        userService.rxRegister(user)
                .subscribe((res, ex) -> handleResponse(event, res.toAuthJson(), ex, HttpResponseStatus.CREATED));
    }

    @RouteConfig(path="/user", method = HttpMethod.POST)
    public void put(RoutingContext event) {
        JsonObject message = event.getBodyAsJson().getJsonObject(USER);
        User user = new User(message);
        userService.rxPut(event.get("userId"), user)
                .subscribe((res, ex) -> handleResponse(event, res.toAuthJson(), ex, HttpResponseStatus.OK));
    }

    @RouteConfig(path="/user")
    public void get(RoutingContext event) {
        userService.rxGetById(event.get("userId"))
                .subscribe((res, ex) -> handleResponse(event, res.toAuthJson(), ex, HttpResponseStatus.OK));
    }

    @RouteConfig(path="/:username", authRequired = false)
    public void getProfile(RoutingContext event) {

        String username = event.request().getParam("username");
        String queryingUserId = event.get("userId");
        if (queryingUserId != null) {
            userService.rxGetById(queryingUserId)
                    .mergeWith(userService.rxGet(new JsonObject().put("username", username)))
                    .toList()
                    .map(list -> {
                        User queryingUser = list.get(0);
                        User user = list.get(1);
                        handleResponse(event, user.toProfileJsonFor(queryingUser), null, HttpResponseStatus.OK);
                        return 0;
                    });
        } else {
            userService.rxGet(new JsonObject().put("username", username))
                    .subscribe((res, ex) -> handleResponse(event, res.toAuthJson(), ex, HttpResponseStatus.OK));
        }
    }
}
