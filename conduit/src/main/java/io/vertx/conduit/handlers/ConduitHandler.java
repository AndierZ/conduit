package io.vertx.conduit.handlers;

import io.vertx.conduit.services.ArticleService;
import io.vertx.conduit.services.CommentService;
import io.vertx.conduit.services.MorphiaService;
import io.vertx.conduit.services.UserService;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import routerutils.BaseHandler;
import routerutils.Middleware;

public class ConduitHandler extends BaseHandler {
    public static final String ARTICLE = "article";
    public static final String COMMENT = "comment";
    public static final String TOKEN =  "Bearer";
    public static final String USER = "user";

    protected final io.vertx.conduit.services.reactivex.ArticleService articleService;
    protected final io.vertx.conduit.services.reactivex.UserService userService;
    protected final io.vertx.conduit.services.reactivex.CommentService commentService;
    protected final io.vertx.conduit.services.reactivex.MorphiaService morphiaService;

    protected ConduitHandler(Vertx vertx) {
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

        {
            ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(MorphiaService.ADDRESS);
            MorphiaService delegate = builder.build(MorphiaService.class);
            this.morphiaService = new io.vertx.conduit.services.reactivex.MorphiaService(delegate);
        }
    }


    @Middleware
    public void extractUser(RoutingContext event) {
        userService.rxGetById(event.get("userId"))
                .subscribe((user, ex) -> {
                    if (ex == null) {
                        event.put(USER, user);
                        event.next();
                    } else {
                        event.fail(ex);
                    }
                });
    }

    @Middleware
    public void extractProfile(RoutingContext event) {
        String username = event.request().getParam("username");
        userService.rxGet(new JsonObject().put("username", username))
                .subscribe((user, ex) -> {
                    if (ex == null) {
                        event.put("profile", user);
                        event.next();
                    } else {
                        event.fail(ex);
                    }
                });
    }

    @Middleware
    public void extractArticle(RoutingContext event) {
        String slug = event.request().getParam(ARTICLE);
        if (slug != null) {
            articleService.rxGet(slug)
                    .subscribe((article, ex) -> {
                        if (ex == null) {
                            event.put(ARTICLE, article);
                            event.next();
                        } else {
                            event.fail(ex);
                        }
                    });
        } else {
            event.next();
        }
    }

    @Middleware
    public void extractComment(RoutingContext event) {
        String commentId = event.request().getParam(COMMENT);
        commentService.rxGet(commentId)
                .subscribe((comment, ex) -> {
                    if (ex == null) {
                        event.put(COMMENT, comment);
                        event.next();
                    } else {
                        event.fail(ex);
                    }
                });
    }
}
