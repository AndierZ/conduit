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
    private final AuthHandler authHandler;
    private final Handler<RoutingContext> preHandler;

    public RouteBuilder(Router baseRouter, AuthHandler authHandler, Handler<RoutingContext> preHandler) {
        this.handlers = new ArrayList<>();
        this.baseRouter = baseRouter;
        this.authHandler = authHandler;
        this.preHandler = preHandler;
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
            HandlerProcessor.buildHandler(baseRouter, preHandler, handler, authHandler);
        }

        finalized = true;
    }
}
