package io.vertx.conduit.services;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import me.escoffier.vertx.completablefuture.VertxCompletableFuture;

import java.util.List;

public class MongoDbServiceWrapper {

    private final MongoDbService delegate;
    private final Vertx vertx;

    public MongoDbServiceWrapper(Vertx vertx) {
        this.vertx = vertx;
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(MongoDbService.ADDRESS);
        this.delegate = builder.build(MongoDbService.class);
    }

    public VertxCompletableFuture<JsonObject> findOne(final String collection, final JsonObject query, final JsonObject fields){
        Future<JsonObject> future = Future.future();
        this.delegate.findOne(collection, query, fields, future);
        return VertxCompletableFuture.from(vertx, future);
    }

    public VertxCompletableFuture<JsonObject> findById(final String collection, String id, final JsonObject fields){
        Future<JsonObject> future = Future.future();
        this.delegate.findById(collection, id, fields, future);
        return VertxCompletableFuture.from(vertx, future);
    }

    public VertxCompletableFuture<List<JsonObject>> find(final String collection, final JsonObject query, final FindOptions options){
        Future<List<JsonObject>> future = Future.future();
        this.delegate.find(collection, query, options, future);
        return VertxCompletableFuture.from(vertx, future);
    }

    public VertxCompletableFuture<String> insertOne(final String collection, final JsonObject document){
        Future<String> future = Future.future();
        this.delegate.insertOne(collection, document, future);
        return VertxCompletableFuture.from(vertx, future);
    }

    public VertxCompletableFuture<MongoClientUpdateResult> upsert(final String collection, final JsonObject query, final JsonObject update, final UpdateOptions options){
        Future<MongoClientUpdateResult> future = Future.future();
        this.delegate.upsert(collection, query, update, options, future);
        return VertxCompletableFuture.from(vertx, future);
    }

    public VertxCompletableFuture<JsonObject> findOneAndUpdate(final String collection, final JsonObject query, final JsonObject update, final FindOptions findOptions, final UpdateOptions updateOptions){
        Future<JsonObject> future = Future.future();
        this.delegate.findOneAndUpdate(collection, query, update, findOptions, updateOptions, future);
        return VertxCompletableFuture.from(vertx, future);
    }

    public VertxCompletableFuture<JsonObject> findOneAndReplace(String collection, JsonObject query, JsonObject update, FindOptions findOptions, UpdateOptions updateOptions){
        Future<JsonObject> future = Future.future();
        this.delegate.findOneAndReplace(collection, query, update, findOptions, updateOptions, future);
        return VertxCompletableFuture.from(vertx, future);
    }
}
