package io.vertx.conduit.handlers;

import io.netty.handler.codec.http.HttpResponseStatus;
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


    @RouteConfig(path="/tags", method= HttpMethod.GET)
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

    @RouteConfig(path="/", method = HttpMethod.GET, authRequired = false)
    public void queryArticles(RoutingContext event) {
        JsonObject query = event.getBodyAsJson().getJsonObject(QUERY);
        morphiaService.rxQueryArticles(query)
                .doOnError(e -> event.fail(e))
                .subscribe(json -> {
                    event.response()
                            .setStatusCode(HttpResponseStatus.OK.code())
                            .end(Json.encodePrettily(json));
                });
    }

    @RouteConfig(path="/feed", method = HttpMethod.GET, middlewares = "extractUser")
    public void queryArticlesForFollower(RoutingContext event) {
        User queryingUser = event.get(Constants.USER);
        JsonObject query = event.getBodyAsJson().getJsonObject(QUERY);
        query.put("queryingUser", queryingUser.getUsername());

        morphiaService.rxQueryArticlesFeed(query)
                .doOnError(e -> event.fail(e))
                .subscribe(json -> {
                    event.response()
                            .setStatusCode(HttpResponseStatus.OK.code())
                            .end(Json.encodePrettily(json));
                });
    }
}
