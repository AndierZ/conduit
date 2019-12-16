package routes;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.conduit.handlers.Constants;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class QueryTest extends TestBase {

    @Test(timeout = 120000)
    public void testQuery(TestContext tc) {
        cleanupUser(tc);
        cleanupArticles(tc);
        registerUser(tc, user1);
        registerUser(tc, user2);
        loginUser(tc, user1);

        createArticle(tc, testArticle1);
        createArticle(tc, testArticle2);

        loginUser(tc, user2);

        Async favoriteArticle = tc.async();

        webClient.post(PORT, "localhost", "/api/articles/first-article/favorite")
                .putHeader(CONTENT_TYPE, JSON)
                .putHeader(AUTHORIZATION, getJwt(tc))
                .send(ar -> {
                    if (ar.succeeded()) {
                        tc.assertEquals(HttpResponseStatus.OK.code(), ar.result().statusCode());
                        JsonObject json = ar.result().bodyAsJsonObject().getJsonObject(Constants.ARTICLE);
                        tc.assertNotNull(json);
                        JsonObject expected = testArticle1.toJsonFor(user2);
                        expected.put("_id", json.getString("_id"));
                        expected.put("version", json.getLong("version"));
                        expected.put("favoritesCount", 1);
                        expected.put("favorited", true);
                        expected.put("createTime", json.getLong("createTime"));
                        expected.put("createUser", json.getString("createUser"));
                        expected.put("updateTime", json.getLong("updateTime"));
                        tc.assertEquals(expected, json);
                        favoriteArticle.complete();
                    } else {
                        tc.fail();
                    }
                });

        favoriteArticle.awaitSuccess();

        Async tagsQuery = tc.async();

        webClient.get(PORT, "localhost", "/api/tags/")
                .putHeader(CONTENT_TYPE, JSON)
                .putHeader(AUTHORIZATION, getJwt(tc))
                .send(
                    ar -> {
                    if (ar.succeeded()) {
                        tc.assertEquals(HttpResponseStatus.OK.code(), ar.result().statusCode());
                        JsonArray json = ar.result().bodyAsJsonObject().getJsonArray("tags");
                        JsonArray expected = new JsonArray().add("food").add("travel").add("dance").add("music");
                        tc.assertNotNull(json);
                        tc.assertEquals(expected, json);
                        tagsQuery.complete();
                    } else {
                        tc.fail();
                    }
                });

        tagsQuery.awaitSuccess();

        Async articleQuery = tc.async();

        webClient.get(PORT, "localhost", "/api/")
                .putHeader(CONTENT_TYPE, JSON)
                .putHeader(AUTHORIZATION, getJwt(tc))
                .sendJsonObject(new JsonObject().put("query", new JsonObject()
                        .put("limit", 10)
                        .put("offset", 0)
                        .put("author", "user1")
                        .put("favoriter", "user2")
                        .put("tags", new JsonArray().add("food").add("music"))
                        ),
                        ar -> {
                            if (ar.succeeded()) {
                                tc.assertEquals(HttpResponseStatus.OK.code(), ar.result().statusCode());
                                @Nullable JsonObject res = ar.result().bodyAsJsonObject();
                                int count = res.getInteger("count");
                                tc.assertEquals(1, count);

                                JsonArray articles = res.getJsonArray("articles");
                                tc.assertEquals(1, articles.size());

                                tc.assertEquals(testArticle1.getId().toHexString(), articles.getJsonObject(0).getString("_id"));
                                articleQuery.complete();
                            } else {
                                tc.fail();
                            }
                        });

        articleQuery.awaitSuccess();

        Async follow = tc.async();

        webClient.post(PORT, "localhost", "/api/profiles/user1/follow")
                .putHeader(CONTENT_TYPE, JSON)
                .putHeader(AUTHORIZATION, getJwt(tc))
                .send(
                        ar -> {
                            if (ar.succeeded()) {
                                tc.assertEquals(HttpResponseStatus.OK.code(), ar.result().statusCode());
                                JsonObject json = ar.result().bodyAsJsonObject().getJsonObject("profile");
                                JsonObject jsonExpected = user1.toProfileJsonFor(user2);
                                jsonExpected.put("following", true);

                                tc.assertEquals(jsonExpected, json);
                                follow.complete();
                            } else {
                                tc.fail(ar.cause());
                            }
                        });

        follow.awaitSuccess();

        Async feedQuery = tc.async();

        webClient.get(PORT, "localhost", "/api/feed")
                .putHeader(CONTENT_TYPE, JSON)
                .putHeader(AUTHORIZATION, getJwt(tc))
                .sendJsonObject(new JsonObject().put("query", new JsonObject()
                                .put("limit", 10)
                                .put("offset", 0)
                                .put("author", "user1")
                                .put("favoriter", "user2")
                                .put("tags", new JsonArray().add("food").add("music"))
                        ),
                        ar -> {
                            if (ar.succeeded()) {
                                tc.assertEquals(HttpResponseStatus.OK.code(), ar.result().statusCode());
                                @Nullable JsonObject res = ar.result().bodyAsJsonObject();
                                int count = res.getInteger("count");
                                tc.assertEquals(2, count);

                                JsonArray articles = res.getJsonArray("articles");
                                tc.assertEquals(2, articles.size());

                                // ordered by create time in descending order
                                tc.assertEquals(testArticle2.getId().toHexString(), articles.getJsonObject(0).getString("_id"));
                                tc.assertEquals(testArticle1.getId().toHexString(), articles.getJsonObject(1).getString("_id"));
                                feedQuery.complete();
                            } else {
                                tc.fail();
                            }
                        });

        feedQuery.awaitSuccess();

        cleanupArticles(tc);
        cleanupUser(tc);
    }
}
