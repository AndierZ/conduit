package io.vertx.conduit.services;

import io.vertx.conduit.entities.Article;
import io.vertx.conduit.entities.User;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClientDeleteResult;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.serviceproxy.ServiceProxyBuilder;

public class ArticleServiceImpl implements ArticleService {

    private static final String ARTICLE_COLLECTION = "articles";

    private final io.vertx.conduit.services.reactivex.MongoDbService mongoDbService;
    private final FindOptions findOptions;
    private final UpdateOptions updateOptions;

    public ArticleServiceImpl(Vertx vertx){
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(MongoDbService.ADDRESS);
        MongoDbService delegate = builder.build(MongoDbService.class);
        this.mongoDbService = new io.vertx.conduit.services.reactivex.MongoDbService(delegate);
        this.findOptions = new FindOptions();
        this.updateOptions = new UpdateOptions().setUpsert(true);
    }

    @Override
    public void create(Article article, Handler<AsyncResult<Article>> resultHandler) {
        mongoDbService.rxInsertOne(ARTICLE_COLLECTION, article.toJson())
                .subscribe((id, ex) -> {
                    if (ex == null) {
                        article.set_id(id);
                        resultHandler.handle(Future.succeededFuture(article));
                    } else {
                        resultHandler.handle(Future.failedFuture(ex));
                    }
                });
    }

    @Override
    public void update(Article article, Handler<AsyncResult<Article>> resultHandler) {
        mongoDbService.rxFindOneAndReplace(ARTICLE_COLLECTION, new JsonObject().put("slug", article.getSlug()), article.toJson(), findOptions, updateOptions)
                .subscribe((json, ex) -> handleArticle(resultHandler, json, ex));
    }

    @Override
    public void get(String slug, Handler<AsyncResult<Article>> resultHandler) {
        mongoDbService.rxFindOne(ARTICLE_COLLECTION, new JsonObject().put("slug", slug), null)
                .subscribe((json, ex) -> handleArticle(resultHandler, json, ex));
    }

    @Override
    public void delete(String slug, Handler<AsyncResult<Article>> resultHandler) {
        mongoDbService.rxDelete(ARTICLE_COLLECTION, new JsonObject().put("slug", slug))
                .subscribe((res, ex) -> {
                    if (ex == null) {
//                        resultHandler.handle(Future.succeededFuture(res.toJson()));
                    } else {
                        resultHandler.handle(Future.failedFuture(ex));
                    }
                });
    }

    private static void handleArticle(Handler<AsyncResult<Article>> resultHandler, JsonObject json, Throwable ex) {
        if (ex == null) {
            resultHandler.handle(Future.succeededFuture(new Article(json)));
        } else {
            resultHandler.handle(Future.failedFuture(ex));
        }
    }
}
