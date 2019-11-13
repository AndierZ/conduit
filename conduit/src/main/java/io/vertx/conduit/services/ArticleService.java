package io.vertx.conduit.services;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.conduit.entities.Article;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import me.escoffier.vertx.completablefuture.VertxCompletableFuture;

@ProxyGen
public interface ArticleService {

    public String ADDRESS = ArticleService.class.getName();

    void create(Article article, Handler<AsyncResult<Article>> resultHandler);

    void update(Article article, Handler<AsyncResult<Article>> resultHandler);

    VertxCompletableFuture<Article> get(String slug);

    void delete(String slug, Handler<AsyncResult> resultHandler);
}
