package io.vertx.conduit.services;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.conduit.entities.Article;
import io.vertx.conduit.entities.Comment;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

@VertxGen
@ProxyGen
public interface CommentService {

    String ADDRESS = CommentService.class.getName();

    void create(JsonObject comment, Handler<AsyncResult<Comment>> resultHandler);

    void update(String id, JsonObject update, Handler<AsyncResult<Comment>> resultHandler);

    void get(String id, Handler<AsyncResult<Comment>> resultHandler);

    void delete(String id, Handler<AsyncResult<Integer>> resultHandler);
}
