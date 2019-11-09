package io.vertx.conduit.services;

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

    private static int userIdCounter;

    // active user being processed
    private final User user;
    private final JsonObject retJson;

    public UserServiceImpl(Vertx vertx) {
        this.user = new User();
        this.retJson = new JsonObject();
    }

    @Override
    public void register(JsonObject message, Handler<AsyncResult<JsonObject>> resultHandler) {
        user.fromJson(message);
        user.set_id(new ObjectId(new Date(), userIdCounter++));

        retJson.clear();
        toAuthJson(user, retJson);

        resultHandler.handle(Future.succeededFuture(retJson));
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



    private JsonObject toAuthJson(User user, JsonObject retJson) {
        if (user.getBio() != null) {
            retJson.put("bio", user.getBio());
        }
        if (user.getEmail() != null) {
            retJson.put("email", user.getEmail());
        }
        if (user.getImage() != null) {
            retJson.put("image", user.getImage());
        }
        if (user.getUsername() != null) {
            retJson.put("username", user.getUsername());
        }

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
