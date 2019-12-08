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
        cleanup(tc);
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
                        tc.assertEquals(testArticle1.toJson(), json);
                        createArticle.complete();
                    } else {
                        tc.fail();
                    }
                });

        createArticle.awaitSuccess();
    }
}
