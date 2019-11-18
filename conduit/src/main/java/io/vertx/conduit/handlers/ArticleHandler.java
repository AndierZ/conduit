package io.vertx.conduit.handlers;

import com.github.slugify.Slugify;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.conduit.entities.Article;
import io.vertx.conduit.entities.User;
import io.vertx.conduit.services.ArticleService;
import io.vertx.conduit.services.UserService;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.ext.web.RoutingContext;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import logging.ContextLogger;
import routerutils.BaseHandler;
import routerutils.RouteConfig;

@RouteConfig(path="/api/articles", produces = "application/json")
public class ArticleHandler extends BaseHandler {

    private static final String ARTICLE = "article";
    private static Logger LOGGER = ContextLogger.create();

    private final io.vertx.conduit.services.reactivex.ArticleService articleService;
    private final Slugify slugify;
    private final io.vertx.conduit.services.reactivex.UserService userService;

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

        this.slugify = new Slugify();
    }

    @RouteConfig(path="/", method= HttpMethod.POST)
    public void create(RoutingContext event) {
        String userId = event.get("userId");

        if (userId != null) {
            JsonObject message = event.getBodyAsJson().getJsonObject(ARTICLE);
            message.put("author", userId);
            message.put("slug", slugify.slugify(message.getString("title")));
            // TODO what's the use of setting the user field here.
            // see how this will get written in the database
            // why don't we just keep a user id as reference?
            userService.rxGetById(userId)
                       .doOnSuccess(user -> {
                           articleService.rxCreate(new Article(message.put("author", user.toJson())))
                                         .subscribe((article, e) -> {
                                             if (e == null) {
                                                 event.response()
                                                         .setStatusCode(HttpResponseStatus.CREATED.code())
                                                         .end(Json.encodePrettily(article.toJsonFor(user)));
                                             } else {
                                                 event.fail(e);
                                             }
                                         });
                       })
                       .doOnError(e -> {
                           event.fail(e);
                       })
                       .subscribe();
        } else {
            event.fail(new RuntimeException("Unauthorized"));
        }
    }

    public void extractArticle(RoutingContext event) {
        String slug = event.request().getParam("article");
        articleService.rxGet(slug)
                .subscribe((res, e) -> {
                    if (e == null) {
                        event.put("article", res);
                        event.next();
                    } else {
                        event.fail(e);
                    }
                });

        userService.rxGet(event.get("userId"))
                   .subscribe((res, e) -> {
                        if (e == null) {
                            event.put("user", res);
                            event.next();
                        } else {
                            event.fail(e);
                        }
                   });
    }

    @RouteConfig(path="/:article", method=HttpMethod.GET, middlewares = "extractArticle")
    public void get(RoutingContext event){

        Article article = event.get("article");
        event.response()
                .setStatusCode(HttpResponseStatus.OK.code())
                .end(Json.encodePrettily(article.toJsonFor(event.get("user"))));

    }

    @RouteConfig(path="/:article", method=HttpMethod.POST, middlewares = "extractArticle")
    public void update(RoutingContext event){
        Article article = event.get("article");
        User user = event.get("user");

        if (user.get_id() != article.getAuthor().get_id()) {
            event.fail(new RuntimeException("Invalid User"));
        }

        JsonObject message = event.getBodyAsJson().getJsonObject(ARTICLE);
        if (message.getJsonObject("article").getString("title") != null) {
            article.setTitle(message.getJsonObject("article").getString("title"));
        }
        if (message.getJsonObject("article").getString("description") != null) {
            article.setDescription(message.getJsonObject("article").getString("description"));
        }
        if (message.getJsonObject("article").getString("body") != null) {
            article.setBody(message.getJsonObject("article").getString("body"));
        }

        articleService.rxUpdate(article)
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

    @RouteConfig(path="/:article", method=HttpMethod.DELETE, middlewares = "extractArticle")
    public void delete(RoutingContext event){
        Article article = event.get("article");
        if (event.get("userId") == article.getAuthor().get_id()) {
            articleService.rxDelete(article.getSlug());
        } else {
            event.fail(new RuntimeException("Invalid user"));
        }
    }
}
