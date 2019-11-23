package io.vertx.conduit.handlers;

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
import logging.ContextLogger;
import org.springframework.security.crypto.bcrypt.BCrypt;
import routerutils.BaseHandler;
import routerutils.RouteConfig;

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

    @RouteConfig(path="/users/login", method=HttpMethod.POST, authRequired=false)
    public void login(RoutingContext event) {
        JsonObject message = event.getBodyAsJson().getJsonObject(USER);
        userService.rxGetByEmail(message.getString("email"))
                .subscribe((res, ex) -> {
                    if (ex != null) {
                        event.fail(new RuntimeException("Invalid user credentials"));
                    } else {
                        String hashed = res.getPassword();
                        if (BCrypt.checkpw(message.getString("password"), hashed)) {
                            JsonObject userAuthJson = res.toAuthJson();
                            appendJwt(userAuthJson, res.getId().toHexString());
                            event.response()
                                    .setStatusCode(HttpResponseStatus.CREATED.code())
                                    .end(Json.encodePrettily(userAuthJson));
                        } else {
                            event.fail(new RuntimeException("Invalid user credentials"));
                        }
                    }
                });
    }

    private static String setPassword(String password) {
        String salt = BCrypt.gensalt();
        return BCrypt.hashpw(password, salt);
    }

    @RouteConfig(path="/users", method=HttpMethod.POST, authRequired=false)
    public void register(RoutingContext event) {
        JsonObject message = event.getBodyAsJson().getJsonObject(USER);
        message.put("password", setPassword(message.getString("password")));
        userService.rxCreate(message)
                .subscribe((res, ex) -> handleResponse(event, res.toAuthJson(), ex, HttpResponseStatus.OK));
    }

    @RouteConfig(path="/user", method = HttpMethod.POST)
    public void put(RoutingContext event) {
        JsonObject message = event.getBodyAsJson().getJsonObject(USER);
        userService.rxUpdate(event.get("userId"), message)
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
                       .zipWith(userService.rxGet(new JsonObject().put("username", username)), (queryingUser, user) -> user.toProfileJsonFor(queryingUser))
                       .subscribe((json, ex) -> handleResponse(event, json, ex, HttpResponseStatus.OK));

        } else {
            userService.rxGet(new JsonObject().put("username", username))
                    .map(user -> user.toProfileJsonFor(null))
                    .subscribe((json, ex) -> handleResponse(event, json, ex, HttpResponseStatus.OK));
        }
    }
}
