package io.vertx.conduit.services;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.conduit.entities.User;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

@VertxGen
@ProxyGen
public interface UserService {
    String ADDRESS = UserService.class.getName();

    void create(User user, Handler<AsyncResult<User>> resultHandler);

    void get(JsonObject query, Handler<AsyncResult<User>> resultHandler);

    void getById(String id, Handler<AsyncResult<User>> resultHandler);

    void getByEmail(String email, Handler<AsyncResult<User>> resultHandler);

    void update(String id, User user, Handler<AsyncResult<User>> resultHandler);
}
