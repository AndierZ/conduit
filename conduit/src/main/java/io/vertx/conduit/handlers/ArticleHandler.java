package io.vertx.conduit.handlers;

import com.github.slugify.Slugify;
import io.netty.handler.codec.http.HttpResponseStatus;
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

    private final ArticleService articleService;
    private final Slugify slugify;
    private final UserService userService;

    protected ArticleHandler(Vertx vertx) {
        super(vertx);
        ServiceProxyBuilder articleServiceBuilder = new ServiceProxyBuilder(vertx).setAddress(ArticleService.ADDRESS);
        this.articleService = articleServiceBuilder.build(ArticleService.class);

        ServiceProxyBuilder userServiceBuilder = new ServiceProxyBuilder(vertx).setAddress(ArticleService.ADDRESS);
        this.userService = userServiceBuilder.build(UserService.class);
        this.slugify = new Slugify();
    }

    @RouteConfig(path="/", method= HttpMethod.POST)
    public void create(RoutingContext event) {
        JsonObject message = event.getBodyAsJson().getJsonObject(ARTICLE);
        message.put("slug", slugify.slugify(message.getString("title")));
        String userId = event.get("userId");
        message.put("author", userId);

        if (userId != null) {
            userService.getById(event.get("userId"), ar -> {
                if (ar.succeeded()) {
                    User user = ar.result();
                    message.put("author", user.toJson());
                    // TODO what's the use of setting the user field here.
                    // see how this will get written in the database
                    // why don't we just keep a user id as reference?
                    Article article = new Article(message);
                    articleService.create(article, ar2 -> {
                        if (ar2.succeeded()) {
                            event.response()
                                 .setStatusCode(HttpResponseStatus.CREATED.code())
                                 .end(Json.encodePrettily(ar2.result().toJsonForUser(user)));
                        } else {
                            event.fail(ar.cause());
                        }
                    });
                } else {
                    event.fail(ar.cause());
                }
            });
        } else {
            event.fail(new RuntimeException("Unauthorized"));
        }
    }

    public void extractArticle(RoutingContext event) {
        String slug = event.request().getParam("slug");

    }

    @RouteConfig(path="/:slug", method=HttpMethod.GET, middlewares = {"extractArticle"})
    public void get(RoutingContext event){

    }

    public void update(){

    }

    public void delete(){

    }
}
