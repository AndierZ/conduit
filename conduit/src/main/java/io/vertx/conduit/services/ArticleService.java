package io.vertx.conduit.services;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.conduit.entities.Article;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

@ProxyGen
public interface ArticleService {

    public String ADDRESS = ArticleService.class.getName();

    void create(Article article, Handler<AsyncResult<Article>> resultHandler);

    void update(Article article, Handler<AsyncResult<Article>> resultHandler);

    void get(Article article, Handler<AsyncResult<Article>> resultHandler);

    void delete(Article article, Handler<AsyncResult> resultHandler);
}
