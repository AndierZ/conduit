package io.vertx.conduit.entities;

import dev.morphia.annotations.*;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;


@Entity("users")
@Indexes({@Index(fields = { @Field("username") }, options = @IndexOptions(unique = true)),
          @Index(fields = { @Field("email") }, options = @IndexOptions(unique = true))})
@DataObject(generateConverter = true)
public class User extends Base {

    public User() {}

    public User(JsonObject json) {
        fromJson(json);
    }

    @NotEmpty
    @Pattern(regexp="[a-zA-Z0-9]+")
    private String username;

    @NotEmpty
    @Email
    private String email;

    @NotEmpty
    private String password;

    private String bio;

    private String image;

    private final List<String> favorites = new ArrayList<>();

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
        super.toJson(json);
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

    public List<String> getFavorites() {
        return favorites;
    }

    public boolean isFavorite(String id) {
        return this.favorites.contains(id);
    }

}
