package io.vertx.conduit.entities;

import dev.morphia.annotations.*;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.List;

@Entity("articles")
@Indexes(@Index(fields = { @Field("slug") }, options = @IndexOptions(unique = true)))
@DataObject(generateConverter = true)
public class Article extends Base {

    public Article(){}

    public Article(JsonObject jsonObject) {
        fromJson(jsonObject);
    }

    @NotEmpty
    private String slug;

    @NotEmpty
    private String title;

    private String description;

    private String body;

    private List<String> tagList;

    private boolean favorited;

    private int favoritesCount;

    @Reference (idOnly = true, lazy = true)
    @Valid
    private User author;

    public String getSlug() {
        return slug;
    }

    @Reference (idOnly = true, lazy = true)
    @Valid
    private List<Comment> comments;

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<String> getTagList() {
        return tagList;
    }

    public void setTagList(List<String> tagList) {
        this.tagList = tagList;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
    }

    public int getFavoritesCount() {
        return favoritesCount;
    }

    public void setFavoritesCount(int favoritesCount) {
        this.favoritesCount = favoritesCount;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        ArticleConverter.toJson(this, json);
        super.toJson(json);
        // To make postman test script happy
        if (json.getJsonArray("tagList") == null) {
            json.put("tagList", new JsonArray());
        }
        return json;
    }

    protected void fromJson(JsonObject jsonObject) {
        super.fromJson(jsonObject);
        ArticleConverter.fromJson(jsonObject, this);
    }

    public JsonObject toJsonFor(User user) {
        JsonObject json = toJson();
        json.put("createdAt", DF.format(new Date(getCreatedAt())));
        json.put("updatedAt", DF.format(new Date(getCreatedAt())));
        json.put("author", author.toProfileJsonFor(user));
        json.put("favorited", user != null && user.isFavorite(getSlug()));
        return json;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}
