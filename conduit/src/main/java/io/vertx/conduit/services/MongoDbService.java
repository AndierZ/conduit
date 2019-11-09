package io.vertx.conduit.services;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.ext.mongo.UpdateOptions;

import java.util.List;

@ProxyGen
public interface MongoDbService {

    public static final String ADDRESS = MongoDbService.class.getName();

    @Fluent
    public MongoDbService findOne(final String collection, final JsonObject query, final JsonObject fields, final Handler<AsyncResult<JsonObject>> resultHandler);

    @Fluent
    public MongoDbService find(final String collection, final JsonObject query, final FindOptions options, Handler<AsyncResult<List<JsonObject>>> resultHandler);

    @Fluent
    public MongoDbService insertOne(final String collection, final JsonObject document, final Handler<AsyncResult<String>> resultHandler);

    @Fluent
    public MongoDbService upsert(final String collection, final JsonObject query, final JsonObject update, final UpdateOptions options, Handler<AsyncResult<MongoClientUpdateResult>> resultHandler);

    @Fluent
    public MongoDbService findOneAndUpdate(final String collection, final JsonObject query, final JsonObject update, final FindOptions findOptions, final UpdateOptions updateOptions, final Handler<AsyncResult<JsonObject>> resultHandler);

    @ProxyClose
    public void close();
}
