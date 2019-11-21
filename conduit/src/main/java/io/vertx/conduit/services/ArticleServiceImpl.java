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
    public void create(JsonObject article, Handler<AsyncResult<Article>> resultHandler) {
        mongoDbService.rxInsertOne(ARTICLE_COLLECTION, article)
                .subscribe((id, ex) -> {
                    if (ex == null) {
                        article.put("_id", id);
                        resultHandler.handle(Future.succeededFuture(new Article(article)));
                    } else {
                        resultHandler.handle(Future.failedFuture(ex));
                    }
                });
    }

    @Override
    public void update(String slug, JsonObject article, Handler<AsyncResult<Article>> resultHandler) {
        mongoDbService.rxFindOneAndUpdate(ARTICLE_COLLECTION, new JsonObject().put("slug", slug), article, findOptions, updateOptions)
                .subscribe((json, ex) -> handleArticle(resultHandler, json, ex));
    }

    @Override
    public void get(String slug, Handler<AsyncResult<Article>> resultHandler) {
        mongoDbService.rxFindOne(ARTICLE_COLLECTION, new JsonObject().put("slug", slug), null)
                .subscribe((json, ex) -> handleArticle(resultHandler, json, ex));
    }

    @Override
    public void delete(String slug, Handler<AsyncResult<Void>> resultHandler) {
        mongoDbService.rxDelete(ARTICLE_COLLECTION, new JsonObject().put("slug", slug))
                .subscribe((res, ex) -> {
                    if (ex == null) {
                        resultHandler.handle(Future.succeededFuture());
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
