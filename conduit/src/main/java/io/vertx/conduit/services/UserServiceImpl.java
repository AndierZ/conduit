package io.vertx.conduit.services;

import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.UpdateOptions;
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


public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = ContextLogger.create();
    private static final String USER_COLLECTION = "users";

    // active user being processed
    private final MongoDbService mongoDbService;
    private final FindOptions findOptions;
    private final UpdateOptions updateOptions;

    public UserServiceImpl(Vertx vertx) {
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(MongoDbService.ADDRESS);
        this.mongoDbService = builder.build(MongoDbService.class);
        this.findOptions = new FindOptions();
        this.updateOptions = new UpdateOptions().setUpsert(true);
    }

    @Override
    public void register(User user, Handler<AsyncResult<JsonObject>> resultHandler) {
        mongoDbService.insertOne(USER_COLLECTION, user.toJson(), ar -> {
            if (ar.succeeded()) {
                user.setId(new ObjectId(ar.result()));
                resultHandler.handle(Future.succeededFuture(user.toAuthJson()));
            } else {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    @Override
    public void login(String email, Handler<AsyncResult<JsonObject>> resultHandler) {
        mongoDbService.findOne(USER_COLLECTION, new JsonObject().put("email", email), null, ar -> {
            if (ar.succeeded()) {
                User user = new User(ar.result());
                JsonObject res = new JsonObject();
                res.put("passwordHash", ar.result().getString("passwordHash"));
                res.put("authJson", user.toAuthJson());
                resultHandler.handle(Future.succeededFuture(res));
            } else {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    @Override
    public void get(String id, Handler<AsyncResult<JsonObject>> resultHandler) {

        mongoDbService.findById(USER_COLLECTION, id, null, ar ->{
            if (ar.succeeded()) {
                User user = new User(ar.result());
                resultHandler.handle(Future.succeededFuture(user.toAuthJson()));
            } else {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    @Override
    public void put(String id, User user, Handler<AsyncResult<JsonObject>> resultHandler) {
        // Have to filter on all unique fields for mongodb to replace the document instead of inserting a new one
        mongoDbService.findOneAndReplace(USER_COLLECTION, new JsonObject().put("id", id).put("email", user.getEmail()), user.toJson(), findOptions, updateOptions, ar -> {
            if (ar.succeeded()) {
                user.fromJson(ar.result());
                resultHandler.handle(Future.succeededFuture(user.toAuthJson()));
            } else {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
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
