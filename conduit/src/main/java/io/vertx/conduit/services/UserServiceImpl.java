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

public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = ContextLogger.create();

    // active user being processed
    private final User user;
    private final JsonObject json;
    private final Vertx vertx;

    private JWTAuth jwtAuth;

    public UserServiceImpl(Vertx vertx) {
        this.vertx = vertx;
        this.user = new User();
        this.json = new JsonObject();

        this.jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions().addPubSecKey(
                new PubSecKeyOptions()
                        .setAlgorithm("HS256")
                        .setPublicKey("vertxuserservice11258")
                        .setSymmetric(true)));
    }

    @Override
    public void register(JsonObject message, Handler<AsyncResult<JsonObject>> resultHandler) {
        message.put("ack", "true");
        resultHandler.handle(Future.succeededFuture(message));
    }

    private void setPassword(String password) {
        String salt = BCrypt.gensalt();
        String passwordHash = BCrypt.hashpw(password, salt);
        this.user.setPasswordHash(passwordHash);
    }

    private boolean validatePassword(String password) {
        return BCrypt.checkpw(password, this.user.getPasswordHash());
    }

    private String generateJwt() {
        return jwtAuth.generateToken(generatePrincipal(user), new JWTOptions().setExpiresInMinutes(60));
    }

    private JsonObject generatePrincipal(User user) {
        json.clear();
        json.put("_id", user.get_id());
        json.put("username", user.getUsername());
        return json;
    }

    private JsonObject toAuthJson() {
        json.clear();
        if (user.getBio() != null) {
            json.put("bio", user.getBio());
        }
        if (user.getEmail() != null) {
            json.put("email", user.getEmail());
        }
        if (user.getImage() != null) {
            json.put("image", user.getImage());
        }
        if (user.getUsername() != null) {
            json.put("username", user.getUsername());
        }
        json.put("jwt", generateJwt());
        return json;
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
