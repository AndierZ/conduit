package io.vertx.conduit.services;

import com.mongodb.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Key;
import dev.morphia.Morphia;
import dev.morphia.ValidationExtension;
import dev.morphia.query.*;
import io.vertx.conduit.entities.Article;
import io.vertx.conduit.entities.Base;
import io.vertx.conduit.entities.Comment;
import io.vertx.conduit.entities.User;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import javafx.util.Pair;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MorphiaServiceImpl implements MorphiaService {

    private final Morphia morphia;
    private final Vertx vertx;
    private final Datastore datastore;
    private final MongoClient mongoClient;

    public MorphiaServiceImpl(final Vertx vertx, final JsonObject dbConfig, final Handler<AsyncResult<MorphiaService>> readyHandler) {
        this.vertx = vertx;
        this.mongoClient = new MongoClient(dbConfig.getString("host"), dbConfig.getInteger("port"));
        this.morphia = new Morphia();
        this.morphia.map(User.class);
        this.morphia.map(Article.class);
        this.morphia.map(Comment.class);
        this.datastore = morphia.createDatastore(mongoClient, dbConfig.getString("db_name"));

        new ValidationExtension(morphia);

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
    public void getComment(JsonObject query, Handler<AsyncResult<List<Comment>>> resultHandler) {
        find(Comment.class, query, resultHandler);
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
    public void createComment(Comment entity, Handler<AsyncResult<String>> resultHandler) {
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
    public void updateComment(JsonObject query, JsonObject update, Handler<AsyncResult<List<Comment>>> resultHandler) {
        update(Comment.class, query, update, resultHandler);
    }

    @Override
    public void deleteUser(JsonObject query, Handler<AsyncResult<Integer>> resultHandler) {
        delete(User.class, query, resultHandler);
    }

    @Override
    public void deleteArticle(JsonObject query, Handler<AsyncResult<Integer>> resultHandler) {
        delete(Article.class, query, resultHandler);
    }

    @Override
    public void deleteComment(JsonObject query, Handler<AsyncResult<Integer>> resultHandler) {
        delete(Comment.class, query, resultHandler);
    }

    @Override
    public void queryTags(Handler<AsyncResult<List<String>>> resultHandler) {
        vertx.executeBlocking(future -> {
            future.complete(datastore.getCollection(Article.class).distinct("TagList"));
        }, res -> {
            if (res.succeeded()) {
                resultHandler.handle(Future.succeededFuture((List<String>)res.result()));
            } else {
                resultHandler.handle(Future.failedFuture(res.cause()));
            }
        });
    }

    @Override
    public void queryArticles(JsonObject json, Handler<AsyncResult<JsonObject>> resultHandler) {
        vertx.executeBlocking(future -> {
            FindOptions findOptions = new FindOptions()
                    .limit(json.getInteger("limit", 20))
                    .skip(json.getInteger("offset", 0));

            Query<Article> query = datastore.createQuery(Article.class);
            query.order(Sort.descending("createTime"));

            if (json.getString("author") != null) {
                query.field("author.$username").equal(new ObjectId(json.getString("author")));
            }

            if (json.getString("favoriter") != null) {
                Query<User> userQuery = datastore.createQuery(User.class);
                query.field("username").equal(new ObjectId(json.getString("favoriter")));
                User favoriter = userQuery.first();

                query.field("slug").in(favoriter.getFavorites());
            }

            if (json.getJsonArray("tags") != null) {
                List<Criteria> criteria = json.getJsonArray("tags").stream().map(tag -> query.criteria("tagList").in(json.getJsonArray("tags"))).collect(Collectors.toList());
                query.or(criteria.toArray(new Criteria[0]));
            }

            long count = query.count();
            List<Article> articles = query.find(findOptions).toList();
            JsonArray array = new JsonArray();
            articles.forEach(article -> array.add(article.toJson()));

            JsonObject res = new JsonObject();

            res.put("articlesCount", count);
            res.put("articles", array);

            future.complete(res);
        }, res -> {
            if (res.succeeded()) {
                resultHandler.handle(Future.succeededFuture((JsonObject)res.result()));
            } else {
                resultHandler.handle(Future.failedFuture(res.cause()));
            }
        });
    }

    @Override
    public void queryArticlesFeed(JsonObject json, Handler<AsyncResult<JsonObject>> resultHandler) {
        vertx.executeBlocking(future -> {
            FindOptions findOptions = new FindOptions()
                    .limit(json.getInteger("limit", 20))
                    .skip(json.getInteger("offset", 0));


            Query<Article> query = datastore.createQuery(Article.class);
            query.order(Sort.descending("createTime"));

            if (json.getString("queryingUser") != null) {
                Query<User> userQuery = datastore.createQuery(User.class);
                query.field("username").equal(new ObjectId(json.getString("queryingUser")));
                User queryingUser = userQuery.first();

                query.field("author").in(queryingUser.getFavorites());
            }

            long count = query.count();
            List<Article> articles = query.find(findOptions).toList();
            JsonArray array = new JsonArray();
            articles.forEach(article -> array.add(article.toJson()));

            JsonObject res = new JsonObject();

            res.put("articlesCount", count);
            res.put("articles", array);

            future.complete(res);
        }, res -> {
            if (res.succeeded()) {
                resultHandler.handle(Future.succeededFuture((JsonObject) res.result()));
            } else {
                resultHandler.handle(Future.failedFuture(res.cause()));
            }
        });
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
                if(pair.getKey().charAt(0) == '$' && pair.getValue() instanceof JsonObject) {
                    if ("$push".equals(pair.getKey())) {
                        JsonObject fields = (JsonObject) pair.getValue();
                        for(Iterator<Map.Entry<String, Object>> it2 = fields.iterator(); it.hasNext(); ) {
                            Map.Entry<String, Object> field = it2.next();
                            updateOperations.addToSet(field.getKey(), field.getValue());
                        }
                    } else if ("$pop".equals(pair.getKey())) {
                        JsonObject fields = (JsonObject) pair.getValue();
                        for(Iterator<Map.Entry<String, Object>> it2 = fields.iterator(); it.hasNext(); ) {
                            Map.Entry<String, Object> field = it2.next();
                            updateOperations.removeAll(field.getKey(), field.getValue());
                        }
                    }
                } else {
                    updateOperations.set(pair.getKey(), pair.getValue());
                }
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
