package io.vertx.conduit.services;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@ProxyGen
public interface UserService {
    String ADDRESS = UserService.class.getName();

    static UserService create(Vertx vertx) {
        return new UserServiceImpl(vertx);
    }

    void register(JsonObject message, Handler<AsyncResult<JsonObject>> resultHandler);

    void get(String id, Handler<AsyncResult<JsonObject>> resultHandler);
}
