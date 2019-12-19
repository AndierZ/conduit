package io.vertx.conduit.handlers;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.AuthHandlerImpl;
import io.vertx.ext.web.handler.impl.HttpStatusException;

import java.util.List;
import java.util.Objects;

public class ConduitJwtAuthHandlerImpl extends AuthHandlerImpl {

    private static final HttpStatusException UNAUTHORIZED = new HttpStatusException(401);
    private static final HttpStatusException BAD_REQUEST = new HttpStatusException(400);

    private final JsonObject options = new JsonObject();
    private final String type;

    public ConduitJwtAuthHandlerImpl(JWTAuth authProvider, String type) {
        super(authProvider, null);
        this.type = type;
    }

    public ConduitJwtAuthHandlerImpl setAudience(List<String> audience) {
        options.put("audience", new JsonArray(audience));
        return this;
    }

    public ConduitJwtAuthHandlerImpl setIssuer(String issuer) {
        options.put("issuer", issuer);
        return this;
    }

    public ConduitJwtAuthHandlerImpl setIgnoreExpiration(boolean ignoreExpiration) {
        options.put("ignoreExpiration", ignoreExpiration);
        return this;
    }

    @Override
    protected String authenticateHeader(RoutingContext context) {
        return Constants.AUTH_KEY;
    }

    @Override
    public void parseCredentials(RoutingContext context, Handler<AsyncResult<JsonObject>> handler) {

        parseAuthorizationToken(context, false, parseAuthorization -> {
            if (parseAuthorization.failed()) {
                handler.handle(Future.failedFuture(parseAuthorization.cause()));
                return;
            }

            handler.handle(Future.succeededFuture(new JsonObject().put("jwt", parseAuthorization.result()).put("options", options)));
        });
    }

    private void parseAuthorizationToken(RoutingContext ctx, boolean optional, Handler<AsyncResult<String>> handler) {

        final HttpServerRequest request = ctx.request();
        final String authorization = request.headers().get(HttpHeaders.AUTHORIZATION);

        if (authorization == null) {
            if (optional) {
                // this is allowed
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(UNAUTHORIZED));
            }
            return;
        }

        try {
            int idx = authorization.indexOf(' ');

            if (idx <= 0) {
                handler.handle(Future.failedFuture(BAD_REQUEST));
                return;
            }

            if (!Objects.equals(type, (authorization.substring(0, idx)))) {
                handler.handle(Future.failedFuture(UNAUTHORIZED));
                return;
            }

            handler.handle(Future.succeededFuture(authorization.substring(idx + 1)));
        } catch (RuntimeException e) {
            handler.handle(Future.failedFuture(e));
        }
    }
}
