package routerutils;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public abstract class BaseHandler {

    protected final Vertx vertx;

    protected BaseHandler(Vertx vertx) {
        this.vertx = vertx;
    }

    protected static void handleComplete(RoutingContext event, JsonObject res, Throwable ex, HttpResponseStatus successStatus) {
        if (ex != null) {
            event.fail(ex);
        } else {
            event.response()
                    .setStatusCode(successStatus.code())
                    .end(Json.encodePrettily(res));
        }
    }
}
