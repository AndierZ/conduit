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
    public void register(User user, Handler<AsyncResult<User>> resultHandler) {
        mongoDbService.insertOne(USER_COLLECTION, user.toJson(), ar -> {
            if (ar.succeeded()) {
                user.set_id(ar.result());
                resultHandler.handle(Future.succeededFuture(user));
            } else {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    @Override
    public void login(String email, Handler<AsyncResult<User>> resultHandler) {
        mongoDbService.findOne(USER_COLLECTION, new JsonObject().put("email", email), null, ar -> handleUser(resultHandler, ar));
    }

    @Override
    public void get(JsonObject query, Handler<AsyncResult<User>> resultHandler) {

        mongoDbService.findOne(USER_COLLECTION, query, null, ar -> handleUser(resultHandler, ar));
    }

    @Override
    public void put(String id, User user, Handler<AsyncResult<User>> resultHandler) {
        // Have to filter on all unique fields for mongodb to replace the document instead of inserting a new one
        mongoDbService.findOneAndReplace(USER_COLLECTION, new JsonObject().put("id", id).put("email", user.getEmail()), user.toJson(), findOptions, updateOptions, ar -> handleUser(resultHandler, ar));
    }

    private void handleUser(Handler<AsyncResult<User>> resultHandler, AsyncResult<JsonObject> ar) {
        if (ar.succeeded()) {
            User user = new User(ar.result());
            resultHandler.handle(Future.succeededFuture(user));
        } else {
            resultHandler.handle(Future.failedFuture(ar.cause()));
        }
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
