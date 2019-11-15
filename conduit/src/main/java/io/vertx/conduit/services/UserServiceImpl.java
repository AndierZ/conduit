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
    private final MongoDbServiceWrapper mongoDbService;
    private final FindOptions findOptions;
    private final UpdateOptions updateOptions;

    public UserServiceImpl(Vertx vertx) {
        this.mongoDbService = new MongoDbServiceWrapper(vertx);
        this.findOptions = new FindOptions();
        this.updateOptions = new UpdateOptions().setUpsert(true);
    }

    @Override
    public void register(User user, Handler<AsyncResult<User>> resultHandler) {
        mongoDbService.insertOne(USER_COLLECTION, user.toJson())
                .whenComplete((res, ex) -> {
                   if (ex == null) {
                       user.set_id(res);
                       resultHandler.handle(Future.succeededFuture(user));
                   } else {
                       resultHandler.handle(Future.failedFuture(ex));
                   }
                });
    }

    @Override
    public void getByEmail(String email, Handler<AsyncResult<User>> resultHandler) {
        mongoDbService.findOne(USER_COLLECTION, new JsonObject().put("email", email), null)
                      .whenComplete((res, ex) -> handleComplete(resultHandler, res, ex));
    }

    private static void handleComplete(Handler<AsyncResult<User>> resultHandler, JsonObject res, Throwable ex) {
        if (ex == null) {
            resultHandler.handle(Future.succeededFuture(new User(res)));
        } else {
            resultHandler.handle(Future.failedFuture(ex));
        }
    }

    @Override
    public void get(JsonObject query, Handler<AsyncResult<User>> resultHandler) {
        mongoDbService.findOne(USER_COLLECTION, query, null)
                      .whenComplete((res, ex) -> handleComplete(resultHandler, res, ex));;
    }

    @Override
    public void getById(String id, Handler<AsyncResult<User>> resultHandler) {
        mongoDbService.findById(USER_COLLECTION, id, null)
                      .whenComplete((res, ex) -> handleComplete(resultHandler, res, ex));
    }

    @Override
    public void put(String id, User user, Handler<AsyncResult<User>> resultHandler) {
        // Have to filter on all unique fields for mongodb to replace the document instead of inserting a new one
        mongoDbService.findOneAndReplace(USER_COLLECTION, new JsonObject().put("_id", id).put("email", user.getEmail()).put("username", user.getUsername()), user.toJson(), findOptions, updateOptions)
                      .whenComplete((res, ex) -> handleComplete(resultHandler, res, ex));
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
