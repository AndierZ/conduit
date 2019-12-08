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
                        JsonObject json = ar.result().bodyAsJsonObject().getJsonObject("article");
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
                        JsonObject json = ar.result().bodyAsJsonObject().getJsonObject("article");
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


        cleanupArticles(tc);
        cleanupUser(tc);
    }
}
