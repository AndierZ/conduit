package io.vertx.conduit.handlers;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.conduit.entities.User;
import io.vertx.conduit.services.MorphiaServiceOperator;
import io.vertx.conduit.services.UserService;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.ext.web.RoutingContext;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import routerutils.RouteConfig;

@RouteConfig(path="/api", produces = "application/json")
public class UserHandler extends ConduitHandler {

    private final JWTAuth jwtAuth;

    public UserHandler(Vertx vertx, JWTAuth jwtAuth) {
        super(vertx);
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(UserService.ADDRESS);
        this.jwtAuth = jwtAuth;
    }

    private void appendJwt(JsonObject user, String id) {
        JsonObject principal = new JsonObject();
        principal.put("id", id);
        principal.put("username", user.getString("username"));
        user.put(Constants.AUTH_KEY, jwtAuth.generateToken(principal, new JWTOptions().setExpiresInMinutes(120)));
    }

    @RouteConfig(path="/users/login", method=HttpMethod.POST, authRequired=false)
    public void login(RoutingContext event) {
        JsonObject message = event.getBodyAsJson().getJsonObject(Constants.USER);
        if (message != null) {
            userService.rxGetByEmail(message.getString("email"))
                    .subscribe((res, ex) -> {
                        if (ex != null) {
                            event.fail(new RuntimeException("Invalid user credentials"));
                        } else {
                            String hashed = res.getPassword();
                            if (BCrypt.checkpw(message.getString("password"), hashed)) {
                                JsonObject authJson = createAuthJson(res);
                                event.response()
                                        .setStatusCode(HttpResponseStatus.CREATED.code())
                                        .end(Json.encodePrettily(authJson));
                            } else {
                                event.fail(new RuntimeException("Invalid user credentials"));
                            }
                        }
                    });
        } else {
            event.fail(new RuntimeException("Invalid user credentials"));
        }

    }

    private static String setPassword(String password) {
        String salt = BCrypt.gensalt();
        return BCrypt.hashpw(password, salt);
    }

    private JsonObject createAuthJson(User user) {
        if (user.getId() != null) {
            JsonObject authJson = user.toAuthJson();
            appendJwt(authJson, user.getId().toHexString());
            return new JsonObject().put("user", authJson);
        }
        return new JsonObject();
    }

    @RouteConfig(path="/users", method=HttpMethod.POST, authRequired=false)
    public void register(RoutingContext event) {
        JsonObject message = event.getBodyAsJson().getJsonObject(Constants.USER);
        setCreateFields(event, message);
        message.put("password", setPassword(message.getString("password")));
        userService.rxCreate(message)
                .subscribe(res -> handleResponse(event, createAuthJson(res), HttpResponseStatus.CREATED), e -> handleError(event, e));
    }



    @RouteConfig(path="/user", method = HttpMethod.PUT)
    public void put(RoutingContext event) {
        JsonObject message = event.getBodyAsJson().getJsonObject(Constants.USER);
        setUpdateFields(event, message);
        userService.rxUpdate(event.get(Constants.USER_ID), message)
                .subscribe(res -> handleResponse(event, createAuthJson(res), HttpResponseStatus.OK), e -> handleError(event, e));
    }

    @RouteConfig(path="/user")
    public void get(RoutingContext event) {
        userService.rxGetById(event.get(Constants.USER_ID))
                .subscribe(res -> handleResponse(event, createAuthJson(res), HttpResponseStatus.OK), e -> handleError(event, e));
    }

    @RouteConfig(path="/profiles/:username", authRequired = false, middlewares = {"extractProfile", "extractUser"})
    public void getProfile(RoutingContext event) {
        User profile = event.get("profile");
        User queryingUser = event.get(Constants.USER);
        JsonObject json = new JsonObject().put("profile" ,profile.toProfileJsonFor(queryingUser));
        handleResponse(event, json, HttpResponseStatus.OK);
    }

    @RouteConfig(path="/profiles/:username/follow", method = HttpMethod.POST, middlewares = {"extractProfile", "extractUser"})
    public void follow(RoutingContext event) {

        User profileUser = event.get("profile");
        User queryingUser = event.get(Constants.USER);

        queryingUser.follow(profileUser);

        // following is a list of dbrefs. probably this is not the right way to append to it
        JsonObject update = new JsonObject().put("following", new JsonObject().put(MorphiaServiceOperator.PUSH, new JsonObject().put("_id", profileUser.getId().toHexString())));
        userService.rxUpdate(queryingUser.getId().toHexString(), update)
                   .map(user -> new JsonObject().put("profile", profileUser.toProfileJsonFor(queryingUser)))
                   .subscribe(res -> handleResponse(event, res, HttpResponseStatus.OK), e -> handleError(event, e));
    }

    @RouteConfig(path="/profiles/:username/follow", method = HttpMethod.DELETE, middlewares = {"extractProfile", "extractUser"})
    public void unfollow(RoutingContext event) {

        User profileUser = event.get("profile");
        User queryingUser = event.get(Constants.USER);

        queryingUser.unfollow(profileUser);
        JsonObject update = new JsonObject().put("following", new JsonObject().put(MorphiaServiceOperator.POP, new JsonObject().put("_id", profileUser.getId().toHexString())));
        userService.rxUpdate(queryingUser.getId().toHexString(), update)
                .map(user -> new JsonObject().put("profile", profileUser.toProfileJsonFor(queryingUser)))
                .subscribe(res -> handleResponse(event, res, HttpResponseStatus.OK), e -> handleError(event, e));
    }
}
