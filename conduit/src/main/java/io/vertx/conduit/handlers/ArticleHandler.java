package io.vertx.conduit.handlers;

import com.github.slugify.Slugify;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.conduit.entities.Article;
import io.vertx.conduit.entities.Comment;
import io.vertx.conduit.entities.User;
import io.vertx.conduit.services.ArticleService;
import io.vertx.conduit.services.CommentService;
import io.vertx.conduit.services.UserService;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import routerutils.BaseHandler;
import routerutils.RouteConfig;

import java.util.Objects;

@RouteConfig(path="/api/articles", produces = "application/json")
public class ArticleHandler extends BaseHandler {

    private static final String ARTICLE = "article";
    private static final String COMMENT = "comment";

    private final io.vertx.conduit.services.reactivex.ArticleService articleService;
    private final Slugify slugify;
    private final io.vertx.conduit.services.reactivex.UserService userService;
    private final io.vertx.conduit.services.reactivex.CommentService commentService;

    public ArticleHandler(Vertx vertx) {
        super(vertx);
        {
            ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(ArticleService.ADDRESS);
            ArticleService delegate = builder.build(ArticleService.class);
            this.articleService = new io.vertx.conduit.services.reactivex.ArticleService(delegate);
        }

        {
            ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(UserService.ADDRESS);
            UserService delegate = builder.build(UserService.class);
            this.userService = new io.vertx.conduit.services.reactivex.UserService(delegate);
        }

        {
            ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(CommentService.ADDRESS);
            CommentService delegate = builder.build(CommentService.class);
            this.commentService = new io.vertx.conduit.services.reactivex.CommentService(delegate);
        }

        this.slugify = new Slugify();
    }

    public void extractArticle(RoutingContext event) {
        String slug = event.request().getParam("article");
        if (slug != null) {
            articleService.rxGet(slug)
                    .subscribe((article, ex) -> {
                        if (ex == null) {
                            event.put("article", article);
                            event.next();
                        } else {
                            event.fail(ex);
                        }
                    });
        } else {
            event.next();
        }
    }

    public void extractUser(RoutingContext event) {
        userService.rxGetById(event.get("userId"))
                   .subscribe((user, ex) -> {
                       if (ex == null) {
                           event.put("user", user);
                           event.next();
                       } else {
                           event.fail(ex);
                       }
                   });
    }

    public void extractComment(RoutingContext event) {
        String commentId = event.request().getParam("comment");
        commentService.rxGet(commentId)
                .subscribe((comment, ex) -> {
                    if (ex == null) {
                        event.put("comment", comment);
                        event.next();
                    } else {
                        event.fail(ex);
                    }
                });
    }

    @RouteConfig(path="/", method= HttpMethod.POST, middlewares = "extractUser")
    public void create(RoutingContext event) {
        JsonObject message = event.getBodyAsJson().getJsonObject(ARTICLE);
        User user = event.get("user");
        message.put("slug", slugify.slugify(message.getString("title")));
        message.put("author", user.toJson());

        articleService.rxCreate(message)
                      .subscribe((article, e) -> {
                          if (e == null) {
                              event.response()
                                      .setStatusCode(HttpResponseStatus.CREATED.code())
                                      .end(Json.encodePrettily(article.toJsonFor(user)));
                          } else {
                              event.fail(e);
                          }
                      });
    }

    @RouteConfig(path="/:article", method=HttpMethod.GET, middlewares = {"extractArticle", "extractUser"})
    public void get(RoutingContext event){

        Article article = event.get("article");
        event.response()
                .setStatusCode(HttpResponseStatus.OK.code())
                .end(Json.encodePrettily(article.toJsonFor(event.get("user"))));

    }

    @RouteConfig(path="/:article", method=HttpMethod.POST, middlewares = {"extractArticle", "extractUser"})
    public void update(RoutingContext event){
        Article article = event.get("article");
        User user = event.get("user");

        if (!Objects.equals(user.getId(), article.getAuthor().getId())) {
            event.fail(new RuntimeException("Invalid User"));
        }

        JsonObject message = event.getBodyAsJson().getJsonObject(ARTICLE);

        articleService.rxUpdate(article.getSlug(), message)
                   .subscribe((res, e) -> {
                       if (e == null) {
                           event.response()
                                   .setStatusCode(HttpResponseStatus.OK.code())
                                   .end(Json.encodePrettily(res.toJsonFor(user)));
                       } else {
                           event.fail(e);
                       }
                   });
    }

    @RouteConfig(path="/:article", method=HttpMethod.DELETE, middlewares = {"extractArticle", "extractUser"})
    public void delete(RoutingContext event){
        Article article = event.get("article");
        if (Objects.equals(event.get("userId"), article.getAuthor().getId())) {
            articleService.rxDelete(article.getSlug());
        } else {
            event.fail(new RuntimeException("Invalid user"));
        }
    }

    @RouteConfig(path="/:article/favorite", method=HttpMethod.POST, middlewares = {"extractArticle", "extractUser"})
    private void favorite(RoutingContext event) {
        Article article = event.get("article");
        User user = event.get("user");
        if (!user.isFavorite(article.getSlug())) {
            user.addFavorite(article.getSlug());
        } else {
            event.response()
                    .setStatusCode(HttpResponseStatus.OK.code())
                    .end(Json.encodePrettily(article.toJsonFor(user)));
            return;
        }
        JsonObject update = new JsonObject().put("$push", new JsonObject().put("favorites", article.getSlug()));

        userService.rxUpdate(user.getId().toHexString(), update)
                   .flatMap(ignored -> {
                       return userService.rxGetFavoriteCount(article.getId().toHexString());
                   })
                   .flatMap(count -> {
                       article.setFavoritesCount(count);
                       return articleService.rxUpdate(article.getSlug(), new JsonObject().put("favoritesCount", count));
                   }).subscribe((updatedArticle, ex) -> {
                       if (ex == null) {
                           event.response()
                                   .setStatusCode(HttpResponseStatus.OK.code())
                                   .end(Json.encodePrettily(updatedArticle.toJsonFor(user)));
                       } else {
                           event.fail(ex);
                       }
        });
    }

    @RouteConfig(path="/:article/favorite", method=HttpMethod.DELETE, middlewares = {"extractArticle", "extractUser"})
    private void unfavorite(RoutingContext event) {
        Article article = event.get("article");
        User user = event.get("user");
        if (user.isFavorite(article.getSlug())) {
            user.removeFavorite(article.getSlug());
        } else {
            event.response()
                    .setStatusCode(HttpResponseStatus.OK.code())
                    .end(Json.encodePrettily(article.toJsonFor(user)));
            return;
        }
        JsonObject update = new JsonObject().put("$pop", new JsonObject().put("favorites", article.getSlug()));

        userService.rxUpdate(user.getId().toHexString(), update)
                .flatMap(ignored -> {
                    return userService.rxGetFavoriteCount(article.getId().toHexString());
                })
                .flatMap(count -> {
                    article.setFavoritesCount(count);
                    return articleService.rxUpdate(article.getSlug(), new JsonObject().put("favoritesCount", count));
                }).subscribe((updatedArticle, ex) -> {
            if (ex == null) {
                event.response()
                        .setStatusCode(HttpResponseStatus.OK.code())
                        .end(Json.encodePrettily(updatedArticle.toJsonFor(user)));
            } else {
                event.fail(ex);
            }
        });
    }

    @RouteConfig(path="/:article/comments", method= HttpMethod.POST, middlewares = {"extractUser", "extractArticle"})
    public void createComment(RoutingContext event) {
        User user = event.get("user");
        Article article = event.get("article");

        JsonObject message = event.getBodyAsJson().getJsonObject(COMMENT);
        message.put("author", user.toJson());
        message.put("article", article.toJson());

        JsonObject update = new JsonObject().put("$push", new JsonObject().put("comments", message));


        commentService.rxCreate(message)
                      .zipWith(articleService.rxUpdate(article.getSlug(), update), (comment, updatedArticle) -> {
                          return comment;
                      }).subscribe((comment, ex) -> {
                            if (ex == null) {
                                event.response()
                                        .setStatusCode(HttpResponseStatus.OK.code())
                                        .end(Json.encodePrettily(comment.toJsonFor(user)));
                            } else {
                                event.fail(ex);
                            }
                      });
    }

    @RouteConfig(path="/:article/comments", method= HttpMethod.GET, middlewares = {"extractUser", "extractArticle"})
    public void getComments(RoutingContext event) {
        User user = event.get("user");
        Article article = event.get("article");

        JsonObject comments = new JsonObject();
        JsonArray array = new JsonArray();
        article.getComments().forEach(x -> array.add(x.toJsonFor(user)));
        comments.put("comments", array);

        event.response()
                .setStatusCode(HttpResponseStatus.OK.code())
                .end(Json.encodePrettily(comments));
    }

    @RouteConfig(path="/:article/comments/:comment", method= HttpMethod.DELETE, middlewares = {"extractUser", "extractComment", "extractArticle"})
    public void deleteComment(RoutingContext event) {
        User user = event.get("user");
        Article article = event.get("article");
        Comment comment = event.get("comment");

        if (comment.getAuthor().getId().equals(user.getId())) {
            JsonObject update = new JsonObject().put("$pop", new JsonObject().put("comments", comment.toJson()));
            articleService.rxUpdate(article.getSlug(), update)
            .flatMap(ignored -> {
                return commentService.rxDelete(comment.getId().toHexString());
            })
            .subscribe((ignored, ex) -> {
                if (ex == null) {
                    event.response()
                            .setStatusCode(HttpResponseStatus.NO_CONTENT.code())
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
