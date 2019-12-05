package routerutils;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import javax.xml.ws.http.HTTPBinding;

public abstract class BaseHandler {

    protected final Vertx vertx;

    protected BaseHandler(Vertx vertx) {
        this.vertx = vertx;
    }

    protected static void handleError(RoutingContext event, Throwable ex) {
        if (ex != null) {
            event.response()
                 .setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                 .end(Json.encodePrettily(ex.getMessage()));
//            event.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), ex);
        }
    }

    protected static void handleResponse(RoutingContext event, JsonObject res, HttpResponseStatus successStatus) {
        event.response()
                .setStatusCode(successStatus.code())
                .end(Json.encodePrettily(res));
    }
}
