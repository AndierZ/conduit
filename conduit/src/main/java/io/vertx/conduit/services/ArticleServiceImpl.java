package io.vertx.conduit.services;

import io.vertx.conduit.entities.Article;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.serviceproxy.ServiceProxyBuilder;

public class ArticleServiceImpl implements ArticleService {

    private static final String COLLECTION = "articles";

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
