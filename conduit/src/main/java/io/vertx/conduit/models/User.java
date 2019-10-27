package io.vertx.conduit.models;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.List;

@DataObject(generateConverter = true)
public class User extends Base {

    public User(JsonObject jsonObject) {
        // TODO validate json object. ensure correct format for certain fields (email)
        // TODO how to enforce uniqueness based on username or id in the database?
        fromBaseJson(jsonObject, this);
        UserConverter.fromJson(jsonObject, this);
    }

    public String getUsername() {
        return username;
    }

    String username;

    String email;

    String passwordHash;

    String token;

    String bio;

    String image;

    List<User> following;

    List<Article> favorites;

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
