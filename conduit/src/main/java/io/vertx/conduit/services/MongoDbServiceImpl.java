package io.vertx.conduit.services;

import com.mongodb.MongoWriteException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.reactivex.ext.mongo.MongoClient;

import java.util.List;

public class MongoDbServiceImpl implements MongoDbService {

    private final MongoClient client;

    public MongoDbServiceImpl(final MongoClient mongoClient, final Handler<AsyncResult<MongoDbService>> readyHandler) {
        this.client = mongoClient;
        this.client.rxGetCollections().subscribe(resp -> {
            readyHandler.handle(Future.succeededFuture(this));
        }, cause -> {
            readyHandler.handle(Future.failedFuture(cause));
        });
    }

    @Override
    public void findOne(final String collection, final JsonObject query, final JsonObject fields, final Handler<AsyncResult<JsonObject>> resultHandler) {
        try {
            client.rxFindOne(collection, query, fields).subscribe(resp -> {
                resultHandler.handle(Future.succeededFuture(resp));
            }, cause -> {
                resultHandler.handle(Future.failedFuture(cause));
            });
        } catch (Exception ex) {
            resultHandler.handle(Future.failedFuture(ex));
        }
    }

    @Override
    public void findById(final String collection, final String id, final JsonObject fields, final Handler<AsyncResult<JsonObject>> resultHandler) {
        findOne(collection, new JsonObject().put("_id", id), fields, resultHandler);
    }

    @Override
    public void find(final String collection, final JsonObject query, final FindOptions options, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        try {
            client.rxFindWithOptions(collection, query, options).subscribe(resp -> {
                resultHandler.handle(Future.succeededFuture(resp));
            }, cause -> {
                resultHandler.handle(Future.failedFuture(cause));
            });
        } catch (Exception ex) {
            resultHandler.handle(Future.failedFuture(ex));
        }
    }

    @Override
    public void insertOne(final String collection, final JsonObject document, final Handler<AsyncResult<String>> resultHandler) {
        try {
            // make sure _id field doesn't exist to force database create one
            document.remove("_id");
            client.rxInsert(collection, document).subscribe(resp -> {
                resultHandler.handle(Future.succeededFuture(resp));
            }, cause -> {
                final MongoWriteException mwx = (MongoWriteException) cause;
                if (mwx.getCode() == 11000) {
                    resultHandler.handle(Future.failedFuture(new RuntimeException("DuplicateEntity")));
                } else {
                    resultHandler.handle(Future.failedFuture(cause));
                }
            });
        } catch (Exception ex) {
            resultHandler.handle(Future.failedFuture(ex));
        }
    }

    @Override

    public void upsert(final String collection, final JsonObject query, final JsonObject toUpdate, final UpdateOptions options, Handler<AsyncResult<MongoClientUpdateResult>> resultHandler) {
        try {
            client.rxUpdateCollectionWithOptions(collection, query, toUpdate, options).subscribe(resp -> {
                resultHandler.handle(Future.succeededFuture(resp));
            }, cause -> {
                resultHandler.handle(Future.failedFuture(cause));
            });
        } catch (Exception ex) {
            resultHandler.handle(Future.failedFuture(ex));
        }
    }

    @Override
    public void findOneAndUpdate(final String collection, final JsonObject query, final JsonObject toUpdate, final FindOptions findOptions, final UpdateOptions updateOptions, final Handler<AsyncResult<JsonObject>> resultHandler) {
        try {
            client.rxFindOneAndUpdateWithOptions(collection, query, toUpdate, findOptions, updateOptions).subscribe(resp -> {
                resultHandler.handle(Future.succeededFuture(resp));
            }, cause -> {
                resultHandler.handle(Future.failedFuture(cause));
            });
        } catch (Exception ex) {
            resultHandler.handle(Future.failedFuture(ex));
        }
    }

    @Override
    public void findOneAndReplace(final String collection, final JsonObject query, final JsonObject toUpdate, final FindOptions findOptions, final UpdateOptions updateOptions, final Handler<AsyncResult<JsonObject>> resultHandler) {
        try {
            client.rxFindOneAndReplaceWithOptions(collection, query, toUpdate, findOptions, updateOptions).subscribe(resp -> {
                resultHandler.handle(Future.succeededFuture(resp));
            }, cause -> {
                resultHandler.handle(Future.failedFuture(cause));
            });
        } catch (Exception ex) {
            resultHandler.handle(Future.failedFuture(ex));
        }
    }

    @Override
    public void close() {
        client.close();
    }
}
