package io.vertx.conduit.entities;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Reference;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;

@DataObject(generateConverter = true)
@Entity("comments")
public class Comment extends Base {

    public Comment() {}

    public Comment(JsonObject jsonObject) {
        fromJson(jsonObject);
    }

    private String body;

    @NotNull
    @Reference(idOnly = true, lazy = true)
    private User author;

    // Not using reference to avoid circular dependency; also not necessary.
    @NotNull
    private ObjectId article;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public ObjectId getArticle() {
        return article;
    }

    public void setArticle(ObjectId article) {
        this.article = article;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        super.toJson(json);
        CommentConverter.toJson(this, json);
        json.put("article", article != null ? article.toHexString() : null);
        return json;
    }

    protected void fromJson(JsonObject jsonObject) {
        super.fromJson(jsonObject);
        CommentConverter.fromJson(jsonObject, this);
        this.article = new ObjectId(jsonObject.getString("article"));
    }

    public JsonObject toJsonFor(User user) {
        JsonObject json = toJson();
        json.put("author", author.toProfileJsonFor(user));
        return json;
    }
}
