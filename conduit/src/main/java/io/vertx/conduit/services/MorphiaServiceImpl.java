package io.vertx.conduit.services;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoWriteException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClientDeleteResult;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.ext.mongo.UpdateOptions;
import org.bson.BsonDocument;
import org.bson.Document;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.util.List;

public class MorphiaServiceImpl implements MongoDbService {

    private final Morphia morphia;
    private final Vertx vertx;
    private final Datastore datastore;
    private final MongoClient mongoClient;

    public MorphiaServiceImpl(final Vertx vertx, final JsonObject dbConfig, final Handler<AsyncResult<MongoDbService>> readyHandler) {
        this.vertx = vertx;
        this.mongoClient = new MongoClient(dbConfig.getString("host"), dbConfig.getInteger("port"));
        this.morphia = new Morphia();
        this.morphia.mapPackage(dbConfig.getString("entityPackage"));
        this.datastore = morphia.createDatastore(mongoClient, dbConfig.getString("db_name"));

        vertx.executeBlocking(future -> {
            try {
                mongoClient.getDatabase(dbConfig.getString("db_name")).runCommand(new Document("ping", "1"));
                datastore.ensureIndexes();
                future.complete();
            } catch (Exception e) {
                future.fail(e);
            }
        }, res -> {
            if (res.succeeded()) {
                readyHandler.handle(Future.succeededFuture(this));
            } else {
                readyHandler.handle(Future.failedFuture(res.cause()));
            }
        });
    }

    @Override
    public void findOne(final String collection, final JsonObject query, final JsonObject fields, final Handler<AsyncResult<JsonObject>> resultHandler) {
    }

    @Override
    public void findById(final String collection, final String id, final JsonObject fields, final Handler<AsyncResult<JsonObject>> resultHandler) {
        findOne(collection, new JsonObject().put("_id", id), fields, resultHandler);
    }

    @Override
    public void find(final String collection, final JsonObject query, final FindOptions options, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
    }

    @Override
    public void insertOne(final String collection, final JsonObject document, final Handler<AsyncResult<String>> resultHandler) {
    }

    @Override

    public void upsert(final String collection, final JsonObject query, final JsonObject toUpdate, final UpdateOptions options, Handler<AsyncResult<MongoClientUpdateResult>> resultHandler) {
    }

    @Override
    public void findOneAndUpdate(final String collection, final JsonObject query, final JsonObject toUpdate, final FindOptions findOptions, final UpdateOptions updateOptions, final Handler<AsyncResult<JsonObject>> resultHandler) {
    }

    @Override
    public void findOneAndReplace(final String collection, final JsonObject query, final JsonObject replacement, final FindOptions findOptions, final UpdateOptions updateOptions, final Handler<AsyncResult<JsonObject>> resultHandler) {
    }

    @Override
    public void delete(final String collection, final JsonObject query, Handler<AsyncResult<MongoClientDeleteResult>> resultHandler){
    }

    @Override
    public void close() {
        this.mongoClient.close();
    }
}
