package routes;

import io.vertx.conduit.handlers.ArticleHandler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ArticleTest extends TestBase {

    @Test(timeout = 120000)
    public void testCreateArticle(TestContext tc) {
        cleanupUser(tc);
        cleanupArticles(tc);
        registerUser(tc, user1);
        loginUser(tc, user1);

        Async createArticle = tc.async();

        // Must include the forward slash at the end

        webClient.post(PORT, "localhost", "/api/articles/")
                .putHeader(CONTENT_TYPE, JSON)
                .putHeader(AUTHORIZATION, getJwt(tc))
                .sendJsonObject(new JsonObject()
                        .put(ArticleHandler.ARTICLE, testArticle1.toJson()
                        ), ar -> {
                    if (ar.succeeded()) {
                        JsonObject json = ar.result().bodyAsJsonObject().getJsonObject(ArticleHandler.ARTICLE);
                        tc.assertNotNull(json);
                        JsonObject expected = testArticle1.toJsonFor(user1);
                        expected.put("_id", json.getString("_id"));
                        tc.assertEquals(expected, json);
                        createArticle.complete();
                    } else {
                        tc.fail();
                    }
                });

        createArticle.awaitSuccess();

        Async updateArticle = tc.async();

        // Must include the forward slash at the end

        webClient.post(PORT, "localhost", "/api/articles/first-article")
                .putHeader(CONTENT_TYPE, JSON)
                .putHeader(AUTHORIZATION, getJwt(tc))
                .sendJsonObject(new JsonObject()
                        .put(ArticleHandler.ARTICLE, new JsonObject().put("body", "updatedBody")
                        ), ar -> {
                    if (ar.succeeded()) {
                        JsonObject json = ar.result().bodyAsJsonObject().getJsonObject(ArticleHandler.ARTICLE);
                        tc.assertNotNull(json);
                        JsonObject expected = testArticle1.toJsonFor(user1);
                        expected.put("_id", json.getString("_id"));
                        expected.put("version", json.getLong("version"));
                        expected.put("body", "updatedBody");
                        tc.assertEquals(expected, json);
                        updateArticle.complete();
                    } else {
                        tc.fail();
                    }
                });

        updateArticle.awaitSuccess();

        Async favoriteArticle = tc.async();

        webClient.post(PORT, "localhost", "/api/articles/first-article/favorite")
                .putHeader(CONTENT_TYPE, JSON)
                .putHeader(AUTHORIZATION, getJwt(tc))
                .send(ar -> {
                    if (ar.succeeded()) {
                        JsonObject json = ar.result().bodyAsJsonObject().getJsonObject(ArticleHandler.ARTICLE);
                        tc.assertNotNull(json);
                        JsonObject expected = testArticle1.toJsonFor(user1);
                        expected.put("_id", json.getString("_id"));
                        expected.put("version", json.getLong("version"));
                        expected.put("body", "updatedBody");
                        expected.put("favoritesCount", 1);
                        expected.put("favorited", true);
                        tc.assertEquals(expected, json);
                        favoriteArticle.complete();
                    } else {
                        tc.fail();
                    }
                });

        favoriteArticle.awaitSuccess();

        Async unfavoriteArticle = tc.async();

        webClient.delete(PORT, "localhost", "/api/articles/first-article/favorite")
                .putHeader(CONTENT_TYPE, JSON)
                .putHeader(AUTHORIZATION, getJwt(tc))
                .send(ar -> {
                    if (ar.succeeded()) {
                        JsonObject json = ar.result().bodyAsJsonObject().getJsonObject(ArticleHandler.ARTICLE);
                        tc.assertNotNull(json);
                        JsonObject expected = testArticle1.toJsonFor(user1);
                        expected.put("_id", json.getString("_id"));
                        expected.put("version", json.getLong("version"));
                        expected.put("body", "updatedBody");
                        expected.put("favoritesCount", 0);
                        expected.put("favorited", false);
                        tc.assertEquals(expected, json);
                        unfavoriteArticle.complete();
                    } else {
                        tc.fail();
                    }
                });

        unfavoriteArticle.awaitSuccess();

        cleanupArticles(tc);
        cleanupUser(tc);
    }
}
