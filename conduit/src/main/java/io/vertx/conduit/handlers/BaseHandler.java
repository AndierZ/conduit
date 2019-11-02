package io.vertx.conduit.handlers;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public abstract class BaseHandler implements Handler<RoutingContext> {

    protected final Vertx vertx;

    protected BaseHandler(Vertx vertx) {
        this.vertx = vertx;
    }
}
