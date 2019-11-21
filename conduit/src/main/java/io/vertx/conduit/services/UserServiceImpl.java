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
    private final io.vertx.conduit.services.reactivex.MongoDbService mongoDbService;
    private final FindOptions findOptions;
    private final UpdateOptions updateOptions;

    public UserServiceImpl(Vertx vertx) {
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(MongoDbService.ADDRESS);
        MongoDbService delegate = builder.build(MongoDbService.class);
        mongoDbService = new io.vertx.conduit.services.reactivex.MongoDbService(delegate);
        this.findOptions = new FindOptions();
        this.updateOptions = new UpdateOptions().setUpsert(true);
    }

    @Override
    public void create(JsonObject user, Handler<AsyncResult<User>> resultHandler) {
        mongoDbService.rxInsertOne(USER_COLLECTION, user)
                      .subscribe((id, ex) -> {
                          if (ex == null) {
                              user.put("_id", id);
                              resultHandler.handle(Future.succeededFuture(new User(user)));
                          } else {
                              resultHandler.handle(Future.failedFuture(ex));
                          }
                      });
    }

    @Override
    public void getByEmail(String email, Handler<AsyncResult<User>> resultHandler) {
        mongoDbService.rxFindOne(USER_COLLECTION, new JsonObject().put("email", email), null)
                .subscribe((json, ex) -> handleUser(resultHandler, json, ex));

    }

    @Override
    public void get(JsonObject query, Handler<AsyncResult<User>> resultHandler) {

        mongoDbService.rxFindOne(USER_COLLECTION, query, null)
                .subscribe((json, ex) -> handleUser(resultHandler, json, ex));
    }

    @Override
    public void getById(String id, Handler<AsyncResult<User>> resultHandler) {

        mongoDbService.rxFindById(USER_COLLECTION, id, null)
                .subscribe((json, ex) -> handleUser(resultHandler, json, ex));
    }

    @Override
    public void update(String id, JsonObject update, Handler<AsyncResult<User>> resultHandler) {
        // Have to filter on all unique fields for mongodb to replace the document instead of inserting a new one
        mongoDbService.rxFindOneAndUpdate(USER_COLLECTION, new JsonObject().put("_id", id), update, findOptions, updateOptions)
                .subscribe((json, ex) -> handleUser(resultHandler, json, ex));
    }


    private static void handleUser(Handler<AsyncResult<User>> resultHandler, JsonObject json, Throwable ex) {
        if (ex == null) {
            resultHandler.handle(Future.succeededFuture(new User(json)));
        } else {
            resultHandler.handle(Future.failedFuture(ex));
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
