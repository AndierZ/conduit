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

    void findOne(final String collection, final JsonObject query, final JsonObject fields, final Handler<AsyncResult<JsonObject>> resultHandler);

    void findById(final String collection, String id, final JsonObject fields, final Handler<AsyncResult<JsonObject>> resultHandler);

    void find(final String collection, final JsonObject query, final FindOptions options, Handler<AsyncResult<List<JsonObject>>> resultHandler);

    void findUser(final JsonObject query, Handler<AsyncResult<List<User>>> resultHandler);

    void findArticle(final JsonObject query, Handler<AsyncResult<List<Article>>> resultHandler);

    void insertUser(User entity, final Handler<AsyncResult<String>> resultHandler);

    void upsert(final String collection, final JsonObject query, final JsonObject update, final UpdateOptions options, Handler<AsyncResult<MongoClientUpdateResult>> resultHandler);

    void findOneAndUpdate(final String collection, final JsonObject query, final JsonObject update, final FindOptions findOptions, final UpdateOptions updateOptions, final Handler<AsyncResult<JsonObject>> resultHandler);

    void findOneAndReplace(String collection, JsonObject query, JsonObject update, FindOptions findOptions, UpdateOptions updateOptions, Handler<AsyncResult<JsonObject>> resultHandler);

    void delete(final String collection, final JsonObject query, Handler<AsyncResult<MongoClientDeleteResult>> resultHandler);

    @ProxyClose
    void close();
}
