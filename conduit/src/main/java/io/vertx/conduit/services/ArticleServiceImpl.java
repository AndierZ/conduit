package io.vertx.conduit.services;

import io.vertx.conduit.entities.Article;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.bson.types.ObjectId;

import java.util.List;

public class ArticleServiceImpl implements ArticleService {

    private final io.vertx.conduit.services.reactivex.MorphiaService morphiaService;

    public ArticleServiceImpl(Vertx vertx){
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(MorphiaService.ADDRESS);
        MorphiaService delegate = builder.build(MorphiaService.class);
        morphiaService = new io.vertx.conduit.services.reactivex.MorphiaService(delegate);
    }

    @Override
    public void create(JsonObject article, Handler<AsyncResult<Article>> resultHandler) {
        Article articleEntity = new Article(article);
        morphiaService.rxCreateArticle(articleEntity)
                .subscribe((id, ex) -> {
                    if (ex == null) {
                        articleEntity.setId(new ObjectId(id));
                        resultHandler.handle(Future.succeededFuture(articleEntity));
                    } else {
                        resultHandler.handle(Future.failedFuture(ex));
                    }
                });
    }

    @Override
    public void update(String slug, JsonObject article, Handler<AsyncResult<Article>> resultHandler) {
        morphiaService.rxUpdateArticle(new JsonObject().put("slug", slug), article)
                .subscribe((articles, ex) -> handleArticle(resultHandler, articles, ex));
    }

    @Override
    public void get(String slug, Handler<AsyncResult<Article>> resultHandler) {
        morphiaService.rxGetArticle(new JsonObject().put("slug", slug))
                .subscribe((articles, ex) -> handleArticle(resultHandler, articles, ex));
    }

    @Override
    public void delete(String slug, Handler<AsyncResult<Integer>> resultHandler) {
        morphiaService.rxDeleteArticle(new JsonObject().put("slug", slug))
                .subscribe((res, ex) -> {
                    if (ex == null) {
                        resultHandler.handle(Future.succeededFuture());
                    } else {
                        resultHandler.handle(Future.failedFuture(ex));
                    }
                });
    }

    private static void handleArticle(Handler<AsyncResult<Article>> resultHandler, List<Article> articles, Throwable ex) {
        if (articles.size() != 1) {
            resultHandler.handle(Future.failedFuture(new RuntimeException("Couldn't find unique article")));
        } else {
            if (ex == null) {
                resultHandler.handle(Future.succeededFuture(articles.get(0)));
            } else {
                resultHandler.handle(Future.failedFuture(ex));
            }
        }
    }
}
