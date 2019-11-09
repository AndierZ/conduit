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
    private final User user;
    private final JsonObject retJson;
    private final MongoDbService mongoDbService;

    public UserServiceImpl(Vertx vertx) {
        this.user = new User();
        this.retJson = new JsonObject();
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(MongoDbService.ADDRESS);
        this.mongoDbService = builder.build(MongoDbService.class);
    }

    @Override
    public void register(JsonObject message, Handler<AsyncResult<JsonObject>> resultHandler) {

        this.mongoDbService.insertOne(collection, message, ar -> {
            if (ar.succeeded()) {
                retJson.clear();
                toAuthJson(message, retJson);
                resultHandler.handle(Future.succeededFuture(retJson));
            } else {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    @Override
    public void get(String id, Handler<AsyncResult<JsonObject>> resultHandler) {
        retJson.clear();
        retJson.put("ack", true);
        resultHandler.handle(Future.succeededFuture(retJson));
    }

    private void setPassword(User user, String password) {
        String salt = BCrypt.gensalt();
        String passwordHash = BCrypt.hashpw(password, salt);
        user.setPasswordHash(passwordHash);
    }

    private boolean validatePassword(User user, String password) {
        return BCrypt.checkpw(password, user.getPasswordHash());
    }



    private JsonObject toAuthJson(JsonObject user, JsonObject retJson) {
        retJson.put("bio", user.getString("bio"));
        retJson.put("email", user.getString("email"));
        retJson.put("image", user.getString("image"));
        retJson.put("username", user.getString("username"));

        return retJson;
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
