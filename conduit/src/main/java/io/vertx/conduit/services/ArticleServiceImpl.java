package io.vertx.conduit.services;

import io.vertx.conduit.entities.Article;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ServiceProxyBuilder;

public class ArticleServiceImpl implements ArticleService {

    private static final String COLLECTION = "articles";

    private final Vertx vertx;

    public ArticleServiceImpl(Vertx vertx){
        this.vertx = vertx;
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(MongoDbService.ADDRESS);
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
