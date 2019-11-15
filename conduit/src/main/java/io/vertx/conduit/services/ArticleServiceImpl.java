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
import me.escoffier.vertx.completablefuture.VertxCompletableFuture;

public class ArticleServiceImpl implements ArticleService {

    private static final String COLLECTION = "articles";

    private final MongoDbService mongoDbService;
    private final FindOptions findOptions;
    private final UpdateOptions updateOptions;
    private final Vertx vertx;

    public ArticleServiceImpl(Vertx vertx){
        this.vertx = vertx;
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(MongoDbService.ADDRESS);
        this.mongoDbService = builder.build(MongoDbService.class);
        this.findOptions = new FindOptions();
        this.updateOptions = new UpdateOptions().setUpsert(true);
    }

    @Override
    public void create(Article article, Handler<AsyncResult<Article>> resultHandler) {

    }

    @Override
    public void update(Article article, Handler<AsyncResult<Article>> resultHandler) {

    }

    @Override
    public void get(String slug, Handler<AsyncResult<Article>> resultHandler) {
    }

    @Override
    public void delete(String slug, Handler<AsyncResult<Article>> resultHandler) {

    }


}
