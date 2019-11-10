package routerutils;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthHandler;

import java.util.ArrayList;
import java.util.List;

public class RouteBuilder {

    private boolean finalized;
    private final List<BaseHandler> handlers;
    private final Router baseRouter;
    private AuthHandler authHandler;
    private Handler<RoutingContext> preHandler;
    private Handler<RoutingContext> optionalAuthHandler;

    public RouteBuilder(Router baseRouter) {
        this.handlers = new ArrayList<>();
        this.baseRouter = baseRouter;
    }

    public RouteBuilder addAuthHandler(AuthHandler authHandler) {
        if (finalized) {
            throw new RuntimeException("Routes already built.");
        }
        this.authHandler = authHandler;
        return this;
    }

    public RouteBuilder addOptionalAuthHandler(Handler<RoutingContext> optionalAuthHandler) {
        if (finalized) {
            throw new RuntimeException("Routes already built.");
        }
        this.optionalAuthHandler = optionalAuthHandler;
        return this;
    }

    public RouteBuilder addPreHandler(Handler<RoutingContext> preHandler) {
        if (finalized) {
            throw new RuntimeException("Routes already built.");
        }
        this.preHandler = preHandler;
        return this;
    }

    public RouteBuilder add(BaseHandler handler){
        if (finalized) {
            throw new RuntimeException("Routes already built.");
        }

        handlers.add(handler);

        return this;
    }

    public void build() {
        if (finalized) {
            throw new RuntimeException("Routes already built.");
        }

        for(BaseHandler handler : handlers) {
            HandlerProcessor.buildHandler(baseRouter, preHandler, handler, authHandler, optionalAuthHandler);
        }

        finalized = true;
    }
}
