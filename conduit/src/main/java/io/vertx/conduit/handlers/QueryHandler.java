package io.vertx.conduit.handlers;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.conduit.entities.User;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import routerutils.RouteConfig;

@RouteConfig(path="/api", produces = "application/json")
public class QueryHandler extends ConduitHandler {

    private static final String QUERY = "query";

    public QueryHandler(Vertx vertx) {
        super(vertx);
    }

    @RouteConfig(path="/tags", method= HttpMethod.GET, authRequired = false)
    public void getTags(RoutingContext event){
        morphiaService.rxQueryTags()
                      .subscribe((tags, ex) -> {
                          if (ex == null) {
                              JsonArray array = new JsonArray();
                              tags.forEach(array::add);
                              event.response()
                                      .setStatusCode(HttpResponseStatus.OK.code())
                                      .end(Json.encodePrettily(new JsonObject().put("tags", array)));
                          } else {
                              event.fail(ex);
                          }
                      });
    }
}
