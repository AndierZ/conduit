package io.vertx.conduit.entities;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.List;

@DataObject(generateConverter = true)
public class User extends Base {

    public User() {}

    public User(JsonObject json) {
        fromJson(json);
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

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        UserConverter.toJson(this, json);
        return json;
    }

    protected void fromJson(JsonObject jsonObject) {
        super.fromJson(jsonObject);
        UserConverter.fromJson(jsonObject, this);
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        String salt = BCrypt.gensalt();
        String passwordHash = BCrypt.hashpw(password, salt);
        setPasswordHash(passwordHash);
    }

    public JsonObject toAuthJson() {
        JsonObject retJson = new JsonObject();
        retJson.put("bio", bio);
        retJson.put("email", email);
        retJson.put("image", image);
        retJson.put("username", username);

        return retJson;
    }

    public JsonObject toProfileJsonFor(User user) {
        JsonObject retJson = new JsonObject();
        retJson.put("bio", bio);
        retJson.put("image", image);
        retJson.put("username", username);
        retJson.put("following", false);

        return retJson;
    }
}
