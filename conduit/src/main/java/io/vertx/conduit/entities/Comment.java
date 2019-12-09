package io.vertx.conduit.entities;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Reference;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

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

    @NotNull
    @Reference (idOnly = true, lazy = true)
    private Article article;

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

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        super.toJson(json);
        CommentConverter.toJson(this, json);
        return json;
    }

    protected void fromJson(JsonObject jsonObject) {
        super.fromJson(jsonObject);
        CommentConverter.fromJson(jsonObject, this);
    }

    public JsonObject toJsonFor(User user) {
        JsonObject json = toJson();
        json.put("author", author.toProfileJsonFor(user));
        return json;
    }
}
