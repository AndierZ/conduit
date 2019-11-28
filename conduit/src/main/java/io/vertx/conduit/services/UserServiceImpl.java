package io.vertx.conduit.services;

import io.vertx.conduit.entities.User;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import logging.ContextLogger;
import org.bson.types.ObjectId;

import java.util.List;


public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = ContextLogger.create();

    // active user being processed
    private final io.vertx.conduit.services.reactivex.MorphiaService morphiaService;

    public UserServiceImpl(Vertx vertx) {
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(MorphiaService.ADDRESS);
        MorphiaService delegate = builder.build(MorphiaService.class);
        morphiaService = new io.vertx.conduit.services.reactivex.MorphiaService(delegate);
    }

    @Override
    public void create(JsonObject user, Handler<AsyncResult<User>> resultHandler) {
        User userEntity = new User(user);
        morphiaService.rxCreateUser(userEntity)
                      .subscribe((id, ex) -> {
                          if (ex == null) {
                              userEntity.setId(new ObjectId(id));
                              resultHandler.handle(Future.succeededFuture(userEntity));
                          } else {
                              resultHandler.handle(Future.failedFuture(ex));
                          }
                      });
    }

    @Override
    public void getByEmail(String email, Handler<AsyncResult<User>> resultHandler) {
        morphiaService.rxGetUser(new JsonObject().put("email", email))
                .subscribe((users, ex) -> handleUser(resultHandler, users, ex));

    }

    @Override
    public void get(JsonObject query, Handler<AsyncResult<User>> resultHandler) {

        morphiaService.rxGetUser(query)
                .subscribe((users, ex) -> handleUser(resultHandler, users, ex));
    }

    @Override
    public void getById(String id, Handler<AsyncResult<User>> resultHandler) {

        morphiaService.rxGetUser(new JsonObject().put("_id", id))
                .subscribe((users, ex) -> handleUser(resultHandler, users, ex));
    }

    @Override
    public void update(String id, JsonObject update, Handler<AsyncResult<User>> resultHandler) {
        // Have to filter on all unique fields for mongodb to replace the document instead of inserting a new one
        morphiaService.rxUpdateUser(new JsonObject().put("_id", id), update)
                .subscribe((users, ex) -> handleUser(resultHandler, users, ex));
    }

    private static void handleUser(Handler<AsyncResult<User>> resultHandler, List<User> users, Throwable ex) {

        if (users.size() != 1) {
            resultHandler.handle(Future.failedFuture(new RuntimeException("Couldn't find unique user")));
        }
        else {
            if (ex == null) {
                resultHandler.handle(Future.succeededFuture(users.get(0)));
            } else {
                resultHandler.handle(Future.failedFuture(ex));
            }
        }
    }

    public void getFavoriteCount(String id, Handler<AsyncResult<Integer>> resultHandler){
        morphiaService.rxGetUser(new JsonObject().put("favorites", new JsonObject().put("$in", new ObjectId(id))))
                      .subscribe((users, ex) -> {
                          if (ex == null) {
                              resultHandler.handle(Future.succeededFuture(users.size()));
                          } else {
                              resultHandler.handle(Future.failedFuture(ex));
                          }
                      });
    }
}
