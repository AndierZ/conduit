package io.vertx.conduit.handlers;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.auth.jwt.impl.JWTUser;
import io.vertx.ext.jwt.JWK;
import io.vertx.ext.jwt.JWT;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

import static io.vertx.conduit.handlers.Constants.USER_ID;

public class JwtOptionalHandler implements Handler<RoutingContext> {

    private final JWT jwt;
    private final String permissionsClaimKey;

    public JwtOptionalHandler(JWTAuthOptions config) {
        this.jwt = new JWT();
        this.permissionsClaimKey = config.getPermissionsClaimKey();

        final List<PubSecKeyOptions> keys = config.getPubSecKeys();

        if (keys != null) {
            for (PubSecKeyOptions pubSecKey : config.getPubSecKeys()) {
                if (pubSecKey.isSymmetric()) {
                    jwt.addJWK(new JWK(pubSecKey.getAlgorithm(), pubSecKey.getPublicKey()));
                } else {
                    jwt.addJWK(new JWK(pubSecKey.getAlgorithm(), pubSecKey.isCertificate(), pubSecKey.getPublicKey(), pubSecKey.getSecretKey()));
                }
            }
        }
    }

    @Override
    public void handle(RoutingContext event) {
        if (event.user() == null) {
            final String authorization = event.request().headers().get(HttpHeaders.AUTHORIZATION);
            if (authorization != null) {
                String[] tokens = authorization.split(" ");
                if (tokens.length == 2) {
                    JsonObject payload = jwt.decode(tokens[1]);
                    event.setUser(new JWTUser(payload, permissionsClaimKey));
                }
            }
        }

        if (event.user() != null && event.user().principal() != null) {
            event.put(USER_ID, event.user().principal().getString("id"));
        } else {
            event.put(USER_ID, null);
        }

        event.next();
    }
}
