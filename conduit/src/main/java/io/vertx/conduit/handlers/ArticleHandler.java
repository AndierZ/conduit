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

import java.util.Objects;

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

        if (!Objects.equals(user.get_id(), article.getAuthor().get_id())) {
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
        if (Objects.equals(event.get("userId"), article.getAuthor().get_id())) {
            articleService.rxDelete(article.getSlug());
        } else {
            event.fail(new RuntimeException("Invalid user"));
        }
    }
}
