package io.vertx.conduit.services;

import com.mongodb.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Key;
import dev.morphia.Morphia;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;
import dev.morphia.query.UpdateResults;
import dev.morphia.query.internal.MorphiaCursor;
import io.vertx.conduit.entities.Article;
import io.vertx.conduit.entities.Base;
import io.vertx.conduit.entities.User;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClientDeleteResult;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MorphiaServiceImpl implements MorphiaService {

    private final Morphia morphia;
    private final Vertx vertx;
    private final Datastore datastore;
    private final MongoClient mongoClient;

    public MorphiaServiceImpl(final Vertx vertx, final JsonObject dbConfig, final Handler<AsyncResult<MorphiaService>> readyHandler) {
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
    public void getUser(final JsonObject query, Handler<AsyncResult<List<User>>> resultHandler) {
        find(User.class, query, resultHandler);
    }

    @Override
    public void getArticle(final JsonObject query, Handler<AsyncResult<List<Article>>> resultHandler) {
        find(Article.class, query, resultHandler);
    }

    @Override
    public void createUser(User entity, final Handler<AsyncResult<String>> resultHandler) {
        save(entity, resultHandler);
    }

    @Override
    public void createArticle(Article entity, Handler<AsyncResult<String>> resultHandler) {
        save(entity, resultHandler);
    }

    @Override
    public void updateUser(JsonObject query, JsonObject update, Handler<AsyncResult<List<User>>> resultHandler) {
        update(User.class, query, update, resultHandler);
    }

    @Override
    public void updateArticle(JsonObject query, JsonObject update, Handler<AsyncResult<List<Article>>> resultHandler) {
        update(Article.class, query, update, resultHandler);
    }

    @Override
    public void deleteUser(JsonObject query, Handler<AsyncResult<Integer>> resultHandler) {
        delete(User.class, query, resultHandler);
    }

    @Override
    public void deleteArticle(JsonObject query, Handler<AsyncResult<Integer>> resultHandler) {
        delete(Article.class, query, resultHandler);
    }

    public <T extends Base> void update(final Class<T> clazz, final JsonObject query, final JsonObject update, final Handler<AsyncResult<List<T>>> resultHandler) {
        vertx.executeBlocking(future -> {
            Query<T> updateQuery = datastore.createQuery(clazz);

            for(Iterator<Map.Entry<String, Object>> it = query.iterator(); it.hasNext();) {
                Map.Entry<String, Object> pair = it.next();
                if ("_id".equals(pair.getKey())) {
                    updateQuery.filter("_id", new ObjectId(pair.getValue().toString()));
                } else {
                    updateQuery.filter(pair.getKey(), pair.getValue());
                }
            }

            final UpdateOperations<T> updateOperations = datastore.createUpdateOperations(clazz);
            for(Iterator<Map.Entry<String, Object>> it = update.iterator(); it.hasNext();) {
                Map.Entry<String, Object> pair = it.next();
                updateOperations.set(pair.getKey(), pair.getValue());
            }

            datastore.update(updateQuery, updateOperations);

            future.complete(updateQuery.find().toList());
        }, res -> {
            if (res.succeeded()) {
                resultHandler.handle(Future.succeededFuture((List<T>) res.result()));
            } else {
                resultHandler.handle(Future.failedFuture(res.cause()));
            }
        });
    }

    public <T extends Base> void find(final Class<T> clazz, final JsonObject query, final Handler<AsyncResult<List<T>>> resultHandler) {

        vertx.executeBlocking(future -> {
            Query<T> findQuery = datastore.createQuery(clazz);

            for(Iterator<Map.Entry<String, Object>> it = query.iterator(); it.hasNext();) {
                Map.Entry<String, Object> pair = it.next();
                if ("_id".equals(pair.getKey())) {
                    findQuery.filter("_id", new ObjectId(pair.getValue().toString()));
                } else {
                    findQuery.filter(pair.getKey(), pair.getValue());
                }
            }

            future.complete(findQuery.find().toList());
        }, res -> {
            if (res.succeeded()) {
                resultHandler.handle(Future.succeededFuture((List<T>) res.result()));
            } else {
                resultHandler.handle(Future.failedFuture(res.cause()));
            }
        });
    }

    public <T extends Base> void delete(final Class<T> clazz, final JsonObject query, final Handler<AsyncResult<Integer>> resultHandler) {
        vertx.executeBlocking(future -> {
            final Query<T> deleteQuery = datastore.createQuery(clazz);
            for(Iterator<Map.Entry<String, Object>> it = query.iterator(); it.hasNext();) {
                Map.Entry<String, Object> pair = it.next();
                if ("_id".equals(pair.getKey())) {
                    deleteQuery.filter("_id", new ObjectId(pair.getValue().toString()));
                } else {
                    deleteQuery.filter(pair.getKey(), pair.getValue());
                }
            }
            int numDeleted = deleteQuery.find().toList().size();
            datastore.delete(deleteQuery);
            future.complete(numDeleted);
        }, res -> {
            if (res.succeeded()) {
                resultHandler.handle(Future.succeededFuture( (Integer) res.result()));
            } else {
                resultHandler.handle(Future.failedFuture(res.cause()));
            }
        });
    }

    private <T extends Base> void save(final T entity, final Handler<AsyncResult<String>> resultHandler) {
        vertx.executeBlocking(future -> {
            Key<T> key = this.datastore.save(entity);
            future.complete(key.getId().toString());
        },res -> {
            if (res.succeeded()) {
                resultHandler.handle(Future.succeededFuture(res.result().toString()));
            } else {
                resultHandler.handle(Future.failedFuture(res.cause()));
            }
        });
    }


    @Override
    public void close() {
        this.mongoClient.close();
    }
}
