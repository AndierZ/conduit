package io.vertx.conduit.services;

import io.vertx.serviceproxy.ServiceProxyBuilder;
import logging.ContextLogger;
import io.vertx.conduit.entities.User;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import org.bson.types.ObjectId;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.Date;


public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = ContextLogger.create();
    private static final String collection = "users";

    // active user being processed
    private final MongoDbService mongoDbService;

    public UserServiceImpl(Vertx vertx) {
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(MongoDbService.ADDRESS);
        this.mongoDbService = builder.build(MongoDbService.class);
    }

    @Override
    public void register(User user, Handler<AsyncResult<JsonObject>> resultHandler) {
        this.mongoDbService.insertOne(collection, user.toJson(), ar -> {
            if (ar.succeeded()) {
                user.set_id(new ObjectId(ar.result()));
                resultHandler.handle(Future.succeededFuture(user.toAuthJson()));
            } else {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    @Override
    public void get(String id, Handler<AsyncResult<JsonObject>> resultHandler) {
        JsonObject retJson = new JsonObject();
        retJson.clear();
        retJson.put("ack", true);
        resultHandler.handle(Future.succeededFuture(retJson));
    }

    private JsonObject toJsonFor(User user) {
        return null;
    }

    private void favorite(ObjectId articleId) {

    }

    private void unfavorite(ObjectId articleId) {

    }

    private boolean isFavorite(ObjectId articleId) {
        return false;
    }

    private void follow(User user){

    }

    private void unfollow(User user) {

    }
}
