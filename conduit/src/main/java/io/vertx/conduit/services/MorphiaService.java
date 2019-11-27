package io.vertx.conduit.services;

import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.conduit.entities.Article;
import io.vertx.conduit.entities.User;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClientDeleteResult;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.ext.mongo.UpdateOptions;

import java.util.List;

@VertxGen
@ProxyGen
public interface MorphiaService {

    String ADDRESS = MorphiaService.class.getName();

    void getUser(final JsonObject query, Handler<AsyncResult<List<User>>> resultHandler);

    void getArticle(final JsonObject query, Handler<AsyncResult<List<Article>>> resultHandler);

    void createUser(User entity, final Handler<AsyncResult<String>> resultHandler);

    void createArticle(Article entity, final Handler<AsyncResult<String>> resultHandler);

    void updateUser(final JsonObject query, final JsonObject update, final Handler<AsyncResult<List<User>>> resultHandler);

    void updateArticle(final JsonObject query, final JsonObject update, final Handler<AsyncResult<List<Article>>> resultHandler);

    void deleteUser(final JsonObject query, Handler<AsyncResult<Integer>> resultHandler);

    void deleteArticle(final JsonObject query, Handler<AsyncResult<Integer>> resultHandler);

    @ProxyClose
    void close();
}
