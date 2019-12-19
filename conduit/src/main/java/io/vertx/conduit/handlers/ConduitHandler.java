package io.vertx.conduit.handlers;

import io.vertx.conduit.entities.User;
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
        if (event.get(Constants.USER_ID) == null) {
            event.next();
            return;
        }
        userService.rxGetById(event.get(Constants.USER_ID))
                .subscribe((user, ex) -> {
                    if (ex == null) {
                        event.put(Constants.USER, user);
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
        String slug = event.request().getParam(Constants.ARTICLE);
        if (slug != null) {
            articleService.rxGet(slug)
                    .subscribe((article, ex) -> {
                        if (ex == null) {
                            event.put(Constants.ARTICLE, article);
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
        String commentId = event.request().getParam(Constants.COMMENT);
        commentService.rxGet(commentId)
                .subscribe((comment, ex) -> {
                    if (ex == null) {
                        event.put(Constants.COMMENT, comment);
                        event.next();
                    } else {
                        event.fail(ex);
                    }
                });
    }

    protected void setCreateFields(RoutingContext event, JsonObject jsonObject) {
        jsonObject.put("createdAt", System.currentTimeMillis());
        User user = event.get(Constants.USER);
        if (user != null) {
            jsonObject.put("createUser", user.getUsername());
        }
    }

    protected void setUpdateFields(RoutingContext event, JsonObject jsonObject) {
        jsonObject.put("updatedAt", System.currentTimeMillis());
        User user = event.get(Constants.USER);
        if (user != null) {
            jsonObject.put("updateUser", user.getUsername());
        }
    }
}
