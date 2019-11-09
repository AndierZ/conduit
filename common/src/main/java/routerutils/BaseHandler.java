package routerutils;

import io.vertx.core.Vertx;

public abstract class BaseHandler {

    protected final Vertx vertx;

    protected BaseHandler(Vertx vertx) {
        this.vertx = vertx;
    }
}
