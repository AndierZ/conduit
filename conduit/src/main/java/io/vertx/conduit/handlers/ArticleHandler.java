package io.vertx.conduit.handlers;

import com.github.slugify.Slugify;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.conduit.entities.Article;
import io.vertx.conduit.entities.Comment;
import io.vertx.conduit.entities.User;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import routerutils.RouteConfig;

import java.util.Objects;

@RouteConfig(path="/api/articles", produces = "application/json")
public class ArticleHandler extends ConduitHandler {

    private final Slugify slugify;

    public ArticleHandler(Vertx vertx) {
        super(vertx);
        this.slugify = new Slugify();
    }

    @RouteConfig(path="/", method= HttpMethod.POST, middlewares = "extractUser")
    public void create(RoutingContext event) {
        JsonObject message = event.getBodyAsJson().getJsonObject(ARTICLE);
        User user = event.get(ConduitHandler.USER);
        message.put("slug", slugify.slugify(message.getString("title")));
        message.put("author", user.toJson());

        articleService.rxCreate(message)
                      .map(article -> new JsonObject().put(ARTICLE, article.toJsonFor(user)))
                      .subscribe(res -> handleResponse(event, res, HttpResponseStatus.CREATED), e -> handleError(event, e));
    }

    @RouteConfig(path="/:article", method=HttpMethod.GET, middlewares = {"extractArticle", "extractUser"})
    public void get(RoutingContext event){

        Article article = event.get(ARTICLE);
        event.response()
                .setStatusCode(HttpResponseStatus.OK.code())
                .end(Json.encodePrettily(new JsonObject().put(ARTICLE, article.toJsonFor(event.get(ConduitHandler.USER)))));

    }

    @RouteConfig(path="/:article", method=HttpMethod.POST, middlewares = {"extractArticle", "extractUser"})
    public void update(RoutingContext event){
        Article article = event.get(ARTICLE);
        User user = event.get(ConduitHandler.USER);

        if (!Objects.equals(user.getId(), article.getAuthor().getId())) {
            event.fail(new RuntimeException("Invalid User"));
        }

        JsonObject message = event.getBodyAsJson().getJsonObject(ARTICLE);

        articleService.rxUpdate(article.getSlug(), message)
                      .map(updatedArticle -> new JsonObject().put(ARTICLE, updatedArticle.toJsonFor(user)))
                      .subscribe(res -> handleResponse(event, res, HttpResponseStatus.OK), e -> handleError(event, e));
    }

    @RouteConfig(path="/:article", method=HttpMethod.DELETE, middlewares = {"extractArticle", "extractUser"})
    public void delete(RoutingContext event){
        Article article = event.get(ARTICLE);
        if (Objects.equals(event.get("userId"), article.getAuthor().getId())) {
            articleService.rxDelete(article.getSlug());
        } else {
            event.fail(new RuntimeException("Invalid user"));
        }
    }

    @RouteConfig(path="/:article/favorite", method=HttpMethod.POST, middlewares = {"extractArticle", "extractUser"})
    public void favorite(RoutingContext event) {
        Article article = event.get(ARTICLE);
        User user = event.get(ConduitHandler.USER);
        if (!user.isFavorite(article.getSlug())) {
            user.addFavorite(article.getSlug());
        } else {
            event.response()
                    .setStatusCode(HttpResponseStatus.OK.code())
                    .end(Json.encodePrettily(new JsonObject().put(ARTICLE, article.toJsonFor(user))));
            return;
        }
        JsonObject update = new JsonObject().put("favorites", new JsonObject().put("$push", article.getSlug()));

        userService.rxUpdate(user.getId().toHexString(), update)
                   .flatMap(ignored -> userService.rxGetFavoriteCount(article.getSlug()))
                   .flatMap(count -> {
                       article.setFavoritesCount(count);
                       return articleService.rxUpdate(article.getSlug(), new JsonObject().put("favoritesCount", count));
                   })
                   .map(updatedArticle -> new JsonObject().put(ARTICLE, updatedArticle.toJsonFor(user)))
                   .subscribe(res -> handleResponse(event, res, HttpResponseStatus.OK), e -> handleError(event, e));
    }

    @RouteConfig(path="/:article/favorite", method=HttpMethod.DELETE, middlewares = {"extractArticle", "extractUser"})
    public void unfavorite(RoutingContext event) {
        Article article = event.get(ARTICLE);
        User user = event.get(ConduitHandler.USER);
        if (user.isFavorite(article.getSlug())) {
            user.removeFavorite(article.getSlug());
        } else {
            event.response()
                    .setStatusCode(HttpResponseStatus.OK.code())
                    .end(Json.encodePrettily(article.toJsonFor(user)));
            return;
        }
        JsonObject update = new JsonObject().put("favorites", new JsonObject().put("$pop", article.getSlug()));

        userService.rxUpdate(user.getId().toHexString(), update)
                .flatMap(ignored -> {
                    return userService.rxGetFavoriteCount(article.getSlug());
                })
                .flatMap(count -> {
                    article.setFavoritesCount(count);
                    return articleService.rxUpdate(article.getSlug(), new JsonObject().put("favoritesCount", count));
                })
                .map(updatedArticle -> new JsonObject().put(ARTICLE, updatedArticle.toJsonFor(user)))
                .subscribe(res -> handleResponse(event, res, HttpResponseStatus.OK), e -> handleError(event, e));
    }

    @RouteConfig(path="/:article/comments", method= HttpMethod.POST, middlewares = {"extractUser", "extractArticle"})
    public void createComment(RoutingContext event) {
        User user = event.get(ConduitHandler.USER);
        Article article = event.get(ARTICLE);

        JsonObject message = event.getBodyAsJson().getJsonObject(COMMENT);
        message.put("author", user.toJson());
        message.put(ARTICLE, article.getId().toHexString());

        commentService.rxCreate(message)
                      .map(comment -> {
                          JsonObject update = new JsonObject().put("comments", new JsonObject().put("$push", new JsonObject().put("_id", comment.getId().toHexString())));
                          // FIXME how can we avoid this?
                          articleService.rxUpdate(article.getSlug(), update).subscribe();
                          return comment;
                      })
                      .map(comment -> new JsonObject().put("comment", comment.toJsonFor(user)))
                      .subscribe(res -> handleResponse(event, res, HttpResponseStatus.OK), e -> handleError(event, e));
    }

    @RouteConfig(path="/:article/comments", method= HttpMethod.GET, middlewares = {"extractUser", "extractArticle"})
    public void getComments(RoutingContext event) {
        User user = event.get(ConduitHandler.USER);
        Article article = event.get(ARTICLE);

        JsonObject comments = new JsonObject();
        JsonArray array = new JsonArray();
        if (article.getComments() != null) {
            article.getComments().forEach(x -> array.add(x.toJsonFor(user)));
        }
        comments.put("comments", array);

        event.response()
                .setStatusCode(HttpResponseStatus.OK.code())
                .end(Json.encodePrettily(comments));
    }

    @RouteConfig(path="/:article/comments/:comment", method= HttpMethod.DELETE, middlewares = {"extractUser", "extractComment", "extractArticle"})
    public void deleteComment(RoutingContext event) {
        User user = event.get(ConduitHandler.USER);
        Article article = event.get(ARTICLE);
        Comment comment = event.get(COMMENT);

        if (comment.getAuthor().getId().equals(user.getId())) {
            JsonObject update = new JsonObject().put("comments", new JsonObject().put("$pop", new JsonObject().put("_id", comment.getId().toHexString())));
            articleService.rxUpdate(article.getSlug(), update)
                          .flatMap(ignored -> commentService.rxDelete(comment.getId().toHexString()))
                          .subscribe((ignored, ex) -> {
                                if (ex == null) {
                                    event.response()
                                            .setStatusCode(HttpResponseStatus.OK.code())
                                            .end();
                                } else {
                                    event.fail(ex);
                              }
                          });

        } else {
            event.fail(new RuntimeException("Invalid user"));
        }
    }
}
