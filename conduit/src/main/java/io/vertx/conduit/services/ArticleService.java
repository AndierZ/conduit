package io.vertx.conduit.services;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.conduit.entities.Article;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.List;

@VertxGen
@ProxyGen
public interface ArticleService {

    String ADDRESS = ArticleService.class.getName();

    void create(JsonObject article, Handler<AsyncResult<Article>> resultHandler);

    void update(String slug, JsonObject update, Handler<AsyncResult<Article>> resultHandler);

    void get(String slug, Handler<AsyncResult<Article>> resultHandler);

    void delete(String slug, Handler<AsyncResult<Long>> resultHandler);
}
