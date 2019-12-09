package io.vertx.conduit.services;

import io.vertx.conduit.entities.Comment;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.bson.types.ObjectId;

import java.util.List;

public class CommentServiceImpl implements CommentService {

    private final io.vertx.conduit.services.reactivex.MorphiaService morphiaService;

    public CommentServiceImpl(Vertx vertx){
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(MorphiaService.ADDRESS);
        MorphiaService delegate = builder.build(MorphiaService.class);
        morphiaService = new io.vertx.conduit.services.reactivex.MorphiaService(delegate);
    }

    @Override
    public void create(JsonObject comment, Handler<AsyncResult<Comment>> resultHandler) {
        Comment commentEntity = new Comment(comment);
        morphiaService.rxCreateComment(commentEntity)
                .subscribe((id, ex) -> {
                    if (ex == null) {
                        commentEntity.setId(new ObjectId(id));
                        resultHandler.handle(Future.succeededFuture(commentEntity));
                    } else {
                        resultHandler.handle(Future.failedFuture(ex));
                    }
                });
    }

    @Override
    public void update(String id, JsonObject article, Handler<AsyncResult<Comment>> resultHandler) {
        morphiaService.rxUpdateComment(new JsonObject().put("_id", id), article)
                .subscribe((comments, ex) -> handleComment(resultHandler, comments, ex));
    }

    @Override
    public void get(String id, Handler<AsyncResult<Comment>> resultHandler) {
        morphiaService.rxGetComment(new JsonObject().put("_id", id))
                .subscribe((comments, ex) -> handleComment(resultHandler, comments, ex));
    }

    @Override
    public void delete(String id, Handler<AsyncResult<Long>> resultHandler) {
        morphiaService.rxDeleteComment(new JsonObject().put("_id", id))
                .subscribe((res, ex) -> {
                    if (ex == null) {
                        resultHandler.handle(Future.succeededFuture(res));
                    } else {
                        resultHandler.handle(Future.failedFuture(ex));
                    }
                });
    }

    private static void handleComment(Handler<AsyncResult<Comment>> resultHandler, List<Comment> comments, Throwable ex) {
        if (comments == null || comments.size() != 1) {
            resultHandler.handle(Future.failedFuture(new RuntimeException("Couldn't find unique comment")));
        } else {
            if (ex == null) {
                resultHandler.handle(Future.succeededFuture(comments.get(0)));
            } else {
                resultHandler.handle(Future.failedFuture(ex));
            }
        }
    }
}
