package io.vertx.conduit.handlers;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.conduit.entities.User;
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
import routerutils.BaseHandler;
import routerutils.Middleware;
import routerutils.RouteConfig;

@RouteConfig(path="/api", produces = "application/json")
public class UserHandler extends BaseHandler {

    public final static String TOKEN =  "Bearer";
    public final static String USER = "user";

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
        user.put(TOKEN, jwtAuth.generateToken(principal, new JWTOptions().setExpiresInMinutes(120)));
    }

    @RouteConfig(path="/users/login", method=HttpMethod.POST, authRequired=false)
    public void login(RoutingContext event) {
        JsonObject message = event.getBodyAsJson().getJsonObject(USER);
        if (message != null) {
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
        } else {
            event.fail(new RuntimeException("Invalid user credentials"));
        }

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
                .subscribe(res -> handleResponse(event, res.toAuthJson(), HttpResponseStatus.CREATED), e -> handleError(event, e));
    }

    @RouteConfig(path="/user", method = HttpMethod.POST)
    public void put(RoutingContext event) {
        JsonObject message = event.getBodyAsJson().getJsonObject(USER);
        userService.rxUpdate(event.get("userId"), message)
                .subscribe(res -> handleResponse(event, res.toAuthJson(), HttpResponseStatus.OK), e -> handleError(event, e));
    }

    @RouteConfig(path="/user")
    public void get(RoutingContext event) {
        userService.rxGetById(event.get("userId"))
                .subscribe(res -> handleResponse(event, res.toAuthJson(), HttpResponseStatus.OK), e -> handleError(event, e));
    }

    @Middleware
    public void extractProfile(RoutingContext event) {
        String username = event.request().getParam("username");
        userService.rxGet(new JsonObject().put("username", username))
                .subscribe((user, ex) -> {
                    if (ex == null) {
                        event.put("profile", user);
                        event.next();
                    } else {
                        event.fail(ex);
                    }
                });
    }

    @RouteConfig(path="/profiles/:username", authRequired = false, middlewares = "extractProfile")
    public void getProfile(RoutingContext event) {

        User profile = event.get("profile");
        String queryingUserId = event.get("userId");
        if (queryingUserId != null) {

            userService.rxGetById(queryingUserId)
                       .map(queryingUser -> new JsonObject().put("profile", profile.toProfileJsonFor(queryingUser)))
                       .subscribe(res -> handleResponse(event, res, HttpResponseStatus.OK), e -> handleError(event, e));

        } else {
            JsonObject json = new JsonObject().put("profile" ,profile.toProfileJsonFor(null));
            handleResponse(event, json, HttpResponseStatus.OK);
        }
    }

    @Middleware
    public void extractUser(RoutingContext event) {
        userService.rxGetById(event.get("userId"))
                .subscribe((user, ex) -> {
                    if (ex == null) {
                        event.put(UserHandler.USER, user);
                        event.next();
                    } else {
                        event.fail(ex);
                    }
                });
    }

    @RouteConfig(path="/profiles/:username/follow", method = HttpMethod.POST, middlewares = {"extractProfile", "extractUser"})
    public void follow(RoutingContext event) {

        User profileUser = event.get("profile");
        User queryingUser = event.get(UserHandler.USER);

        queryingUser.follow(profileUser);
        JsonObject update = new JsonObject().put("following", new JsonObject().put("$push", profileUser.toJson()));
        userService.rxUpdate(queryingUser.getId().toHexString(), update)
                   .map(user -> new JsonObject().put("profile", profileUser.toProfileJsonFor(queryingUser)))
                   .subscribe(res -> handleResponse(event, res, HttpResponseStatus.OK), e -> handleError(event, e));
    }

    @RouteConfig(path="/profiles/:username/follow", method = HttpMethod.DELETE, middlewares = {"extractProfile", "extractUser"})
    public void unfollow(RoutingContext event) {

        User profileUser = event.get("profile");
        User queryingUser = event.get(UserHandler.USER);

        queryingUser.unfollow(profileUser);
        JsonObject update = new JsonObject().put("following", new JsonObject().put("$pop", profileUser.toJson()));
        userService.rxUpdate(queryingUser.getId().toHexString(), update)
                .map(user -> new JsonObject().put("profile", profileUser.toProfileJsonFor(queryingUser)))
                .subscribe(res -> handleResponse(event, res, HttpResponseStatus.OK), e -> handleError(event, e));
    }
}
