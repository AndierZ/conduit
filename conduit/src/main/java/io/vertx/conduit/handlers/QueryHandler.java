package io.vertx.conduit.handlers;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.conduit.services.MorphiaService;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import routerutils.BaseHandler;
import routerutils.RouteConfig;

@RouteConfig(path="/api", produces = "application/json")
public class QueryHandler extends BaseHandler {

    private final io.vertx.conduit.services.reactivex.MorphiaService morphiaService;

    public QueryHandler(Vertx vertx) {
        super(vertx);
        {
            ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(MorphiaService.ADDRESS);
            MorphiaService delegate = builder.build(MorphiaService.class);
            this.morphiaService = new io.vertx.conduit.services.reactivex.MorphiaService(delegate);
        }
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
        JsonObject query = new JsonObject();
        query.put("limit", event.pathParam("limit"));
        query.put("offset", event.pathParam("offset"));
        query.put("author", event.pathParam("author"));
        query.put("favoriter", event.pathParam("favoriter"));
        query.put("tags", event.pathParam("tags"));

        morphiaService.rxQueryArticles(query)
                .doOnError(e -> event.fail(e))
                .subscribe(json -> {
                    event.response()
                            .setStatusCode(HttpResponseStatus.OK.code())
                            .end(Json.encodePrettily(json));
                });
    }

    @RouteConfig(path="/feed", method = HttpMethod.GET)
    public void queryArticlesForFollower(RoutingContext event) {
        JsonObject query = new JsonObject();
        query.put("limit", event.pathParam("limit"));
        query.put("offset", event.pathParam("offset"));
        query.put("queryingUser", event.pathParam("queryingUser"));

        morphiaService.rxQueryArticlesFeed(query)
                .doOnError(e -> event.fail(e))
                .subscribe(json -> {
                    event.response()
                            .setStatusCode(HttpResponseStatus.OK.code())
                            .end(Json.encodePrettily(json));
                });
    }
}
