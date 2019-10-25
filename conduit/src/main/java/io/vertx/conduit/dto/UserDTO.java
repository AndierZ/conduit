package io.vertx.conduit.dto;

import io.vertx.conduit.models.User;
import io.vertx.core.json.JsonObject;
import org.bson.types.ObjectId;

public class UserDTO {

    private final User user;

    public UserDTO(User user) {
        this.user = user;
    }

    public void setPassword(String password) {

    }

    public boolean validatePassword(String password) {
        return false;
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
