package io.vertx.conduit.services;

import io.vertx.conduit.entities.User;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import me.escoffier.vertx.completablefuture.VertxCompletableFuture;

public class UserServiceWrapper {

    private final UserService delegate;
    private final Vertx vertx;

    public UserServiceWrapper(Vertx vertx) {
        this.vertx = vertx;
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(UserService.ADDRESS);
        this.delegate = builder.build(UserService.class);
    }

    public VertxCompletableFuture<User> register(User user) {
        Future<User> future = Future.future();
        this.delegate.register(user, future);
        return VertxCompletableFuture.from(vertx, future);
    }

    public VertxCompletableFuture<User> get(JsonObject query) {
        Future<User> future = Future.future();
        this.delegate.get(query, future);
        return VertxCompletableFuture.from(vertx, future);
    }

    public VertxCompletableFuture<User> getById(String id) {
        Future<User> future = Future.future();
        this.delegate.getById(id, future);
        return VertxCompletableFuture.from(vertx, future);
    }

    public VertxCompletableFuture<User> getByEmail(String email) {
        Future<User> future = Future.future();
        this.delegate.getByEmail(email, future);
        return VertxCompletableFuture.from(vertx, future);
    }

    public VertxCompletableFuture<User> put(String id, User user) {
        Future<User> future = Future.future();
        this.delegate.put(id, user, future);
        return VertxCompletableFuture.from(vertx, future);
    }
}
