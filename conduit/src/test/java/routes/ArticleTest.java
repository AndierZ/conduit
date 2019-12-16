package routes;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.conduit.handlers.Constants;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ArticleTest extends TestBase {

    @Test(timeout = TIMEOUT)
    public void testCreateArticle(TestContext tc) {
        cleanupUser(tc);
        cleanupArticles(tc);
        registerUser(tc, user1);
        loginUser(tc, user1);

        createArticle(tc, testArticle1);

        Async updateArticle = tc.async();

        // Must include the forward slash at the end

        webClient.post(PORT, "localhost", "/api/articles/first-article")
                .putHeader(CONTENT_TYPE, JSON)
                .putHeader(AUTHORIZATION, getJwt(tc))
                .sendJsonObject(new JsonObject()
                        .put(Constants.ARTICLE, new JsonObject().put("body", "updatedBody")
                        ), ar -> {
                    if (ar.succeeded()) {
                        tc.assertEquals(HttpResponseStatus.OK.code(), ar.result().statusCode());
                        JsonObject json = ar.result().bodyAsJsonObject().getJsonObject(Constants.ARTICLE);
                        tc.assertNotNull(json);
                        JsonObject expected = testArticle1.toJsonFor(user1);
                        expected.put("_id", json.getString("_id"));
                        expected.put("version", json.getLong("version"));
                        expected.put("body", "updatedBody");
                        expected.put("createTime", json.getLong("createTime"));
                        expected.put("createUser", json.getString("createUser"));
                        expected.put("updateTime", json.getLong("updateTime"));
                        expected.put("updateUser", json.getString("updateUser"));
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
                        tc.assertEquals(HttpResponseStatus.OK.code(), ar.result().statusCode());
                        JsonObject json = ar.result().bodyAsJsonObject().getJsonObject(Constants.ARTICLE);
                        tc.assertNotNull(json);
                        JsonObject expected = testArticle1.toJsonFor(user1);
                        expected.put("_id", json.getString("_id"));
                        expected.put("version", json.getLong("version"));
                        expected.put("body", "updatedBody");
                        expected.put("favoritesCount", 1);
                        expected.put("favorited", true);
                        expected.put("createTime", json.getLong("createTime"));
                        expected.put("createUser", json.getString("createUser"));
                        expected.put("updateTime", json.getLong("updateTime"));
                        expected.put("updateUser", json.getString("updateUser"));
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
                        tc.assertEquals(HttpResponseStatus.OK.code(), ar.result().statusCode());
                        JsonObject json = ar.result().bodyAsJsonObject().getJsonObject(Constants.ARTICLE);
                        tc.assertNotNull(json);
                        JsonObject expected = testArticle1.toJsonFor(user1);
                        expected.put("_id", json.getString("_id"));
                        expected.put("version", json.getLong("version"));
                        expected.put("body", "updatedBody");
                        expected.put("favoritesCount", 0);
                        expected.put("favorited", false);
                        expected.put("createTime", json.getLong("createTime"));
                        expected.put("createUser", json.getString("createUser"));
                        expected.put("updateTime", json.getLong("updateTime"));
                        expected.put("updateUser", json.getString("updateUser"));
                        tc.assertEquals(expected, json);
                        unfavoriteArticle.complete();
                    } else {
                        tc.fail();
                    }
                });

        unfavoriteArticle.awaitSuccess();

        Async createComment = tc.async();

        webClient.post(PORT, "localhost", "/api/articles/first-article/comments")
                .putHeader(CONTENT_TYPE, JSON)
                .putHeader(AUTHORIZATION, getJwt(tc))
                .sendJsonObject(new JsonObject().put("comment", new JsonObject().put("body", "a comment")),
                    ar -> {
                    if (ar.succeeded()) {
                        tc.assertEquals(HttpResponseStatus.OK.code(), ar.result().statusCode());
                        JsonObject json = ar.result().bodyAsJsonObject().getJsonObject(Constants.COMMENT);
                        tc.assertNotNull(json);
                        tc.put("commentId", json.getString("_id"));
                        tc.assertEquals(testArticle1.getId().toHexString(), json.getString("article"));
                        tc.assertEquals(testArticle1.getAuthor().getUsername(), json.getJsonObject("author").getString("username"));
                        tc.assertEquals("a comment", json.getString("body"));
                        createComment.complete();
                    } else {
                        tc.fail();
                    }
                });

        createComment.awaitSuccess();

        Async getComment = tc.async();

        webClient.get(PORT, "localhost", "/api/articles/first-article/comments")
                .putHeader(CONTENT_TYPE, JSON)
                .putHeader(AUTHORIZATION, getJwt(tc))
                .sendJsonObject(new JsonObject().put("comment", new JsonObject().put("body", "a comment")),
                        ar -> {
                            if (ar.succeeded()) {
                                tc.assertEquals(HttpResponseStatus.OK.code(), ar.result().statusCode());
                                JsonArray json = ar.result().bodyAsJsonObject().getJsonArray("comments");
                                tc.assertNotNull(json);
                                tc.assertEquals(1, json.size());
                                tc.assertEquals("a comment", json.getJsonObject(0).getString("body"));
                                getComment.complete();
                            } else {
                                tc.fail();
                            }
                        });

        getComment.awaitSuccess();

        Async deleteComment = tc.async();

        webClient.delete(PORT, "localhost", "/api/articles/first-article/comments/" + tc.get("commentId"))
                .putHeader(CONTENT_TYPE, JSON)
                .putHeader(AUTHORIZATION, getJwt(tc))
                .send(
                        ar -> {
                            if (ar.succeeded()) {
                                tc.assertEquals(HttpResponseStatus.OK.code(), ar.result().statusCode());
                                deleteComment.complete();
                            } else {
                                tc.fail();
                            }
                        });

        deleteComment.awaitSuccess();

        cleanupArticles(tc);
        cleanupUser(tc);
    }
}
