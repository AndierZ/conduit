package io.vertx.conduit.dto;

import io.vertx.conduit.models.User;
import io.vertx.core.json.JsonObject;
import org.bson.types.ObjectId;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class UserDTO {

    private final User user;

    public UserDTO(JsonObject obj) {
        this.user = new User(obj);
    }

    public void setPassword(String password) {
        String salt = BCrypt.gensalt();
        String passwordHash = BCrypt.hashpw(password, salt);
        this.user.setSalt(salt);
        this.user.setPasswordHash(passwordHash);
    }

    public boolean validatePassword(String password) {
        return BCrypt.checkpw(password, this.user.getSalt());
    }

    public String generateJwt() {
        return null;
    }

    public JsonObject toJsonFor(User user) {
        return null;
    }

    public void favorite(ObjectId articleId) {

    }

    public void unfavorite(ObjectId articleId) {

    }

    public boolean isFavorite(ObjectId articleId) {
        return false;
    }

    public void follow(User user){

    }

    public void unfollow(User user) {

    }

}
