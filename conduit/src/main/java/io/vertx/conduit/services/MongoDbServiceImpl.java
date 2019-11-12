package io.vertx.conduit.services;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.ext.mongo.UpdateOptions;

import java.util.List;

public class MongoDbServiceImpl implements MongoDbService {

    private final MongoClient mongoClient;

    public MongoDbServiceImpl(MongoClient mongoClient, Handler<AsyncResult<MongoDbService>> readyHandler) {
        this.mongoClient = mongoClient;
        this.mongoClient.getCollections(ar -> {
            if (ar.succeeded()) {
                readyHandler.handle(Future.succeededFuture(this));
            } else {
                readyHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    @Override
    public MongoDbService findOne(String collection, JsonObject query, JsonObject fields, Handler<AsyncResult<JsonObject>> resultHandler) {
        try {
            mongoClient.findOne(collection, query, fields, ar -> {
                if (ar.succeeded()) {
                    if (ar.result() != null) {
                        resultHandler.handle(Future.succeededFuture(ar.result()));
                    }  else {
                        resultHandler.handle(Future.failedFuture(new RuntimeException("Empty result set")));
                    }
                } else {
                    resultHandler.handle(Future.failedFuture(ar.cause()));
                }
            });
        } catch (Exception e) {
            resultHandler.handle(Future.failedFuture(e));
        }
        return this;
    }

    @Override
    public MongoDbService findById(String collection, String id, JsonObject fields, Handler<AsyncResult<JsonObject>> resultHandler) {
        return findOne(collection, new JsonObject().put("_id", id), fields, resultHandler);
    }

    @Override
    public MongoDbService find(String collection, JsonObject query, FindOptions options, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        try {
            mongoClient.findWithOptions(collection, query, options, ar -> {
                if (ar.succeeded()) {
                    if (ar.result() != null) {
                        resultHandler.handle(Future.succeededFuture(ar.result()));
                    } else {
                        resultHandler.handle(Future.failedFuture(new RuntimeException("Empty result set")));
                    }

                } else {
                    resultHandler.handle(Future.failedFuture(ar.cause()));
                }
            });

        } catch (Exception e) {
            resultHandler.handle(Future.failedFuture(e));
        }

        return this;
    }

    @Override
    public MongoDbService insertOne(String collection, JsonObject document, Handler<AsyncResult<String>> resultHandler) {
        try {
            mongoClient.insert(collection, document, ar -> {
                if (ar.succeeded()) {
                    resultHandler.handle(Future.succeededFuture(ar.result()));
                } else {
                    resultHandler.handle(Future.failedFuture(ar.cause()));
                }
            });
        } catch (Exception e) {
            resultHandler.handle(Future.failedFuture(e));
        }

        return this;
    }

    @Override
    public MongoDbService upsert(String collection, JsonObject query, JsonObject update, UpdateOptions options, Handler<AsyncResult<MongoClientUpdateResult>> resultHandler) {
        try {
            mongoClient.updateCollectionWithOptions(collection, query, update, options, ar -> {
                if (ar.succeeded()) {
                    resultHandler.handle(Future.succeededFuture(ar.result()));
                } else {
                    resultHandler.handle(Future.failedFuture(ar.cause()));
                }
            });
        } catch (Exception e) {
            resultHandler.handle(Future.failedFuture(e));
        }
        return this;
    }

    @Override
    public MongoDbService findOneAndUpdate(String collection, JsonObject query, JsonObject update, FindOptions findOptions, UpdateOptions updateOptions, Handler<AsyncResult<JsonObject>> resultHandler) {
        try {
            mongoClient.findOneAndUpdateWithOptions(collection, query, update, findOptions, updateOptions, ar -> {
                if (ar.succeeded()) {
                    resultHandler.handle(Future.succeededFuture(ar.result()));
                } else {
                    resultHandler.handle(Future.failedFuture(ar.cause()));
                }
            });
        } catch (Exception e) {
            resultHandler.handle(Future.failedFuture(e));
        }
        return this;
    }

    @Override
    public MongoDbService findOneAndReplace(String collection, JsonObject query, JsonObject update, FindOptions findOptions, UpdateOptions updateOptions, Handler<AsyncResult<JsonObject>> resultHandler) {
        try {
          mongoClient.findOneAndReplaceWithOptions(collection, query, update, findOptions, updateOptions, ar -> {
              if (ar.succeeded()) {
                  resultHandler.handle(Future.succeededFuture(ar.result()));
              } else {
                  resultHandler.handle(Future.failedFuture(ar.cause()));
              }
          });
        } catch (Exception e) {
            resultHandler.handle(Future.failedFuture(e));
        }
        return this;
    }

    @Override
    public void close() {

    }
}
