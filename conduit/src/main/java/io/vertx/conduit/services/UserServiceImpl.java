package io.vertx.conduit.services;

import LoggingUtils.ContextLogger;
import io.vertx.conduit.entities.User;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.auth.jwt.JWTOptions;
import org.bson.types.ObjectId;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.Date;


public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = ContextLogger.create();

    private static int userIdCounter;

    // active user being processed
    private final User user;
    private final JsonObject claimJson;
    private final JsonObject retJson;

    private JWTAuth jwtAuth;

    public UserServiceImpl(Vertx vertx) {
        this.user = new User();
        this.claimJson = new JsonObject();
        this.retJson = new JsonObject();

        this.jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions().addPubSecKey(
                new PubSecKeyOptions()
                        .setAlgorithm("HS256")
                        .setPublicKey("vertxuserservice11258")
                        .setSymmetric(true)));
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

    private String generateJwt(User user) {
        claimJson.clear();
        claimJson.put("_id", user.get_id().toHexString());
        claimJson.put("username", user.getUsername());

        return jwtAuth.generateToken(claimJson, new JWTOptions().setExpiresInMinutes(60));
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
        retJson.put("token", generateJwt(user));

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
