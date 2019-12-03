package routes;


import io.vertx.conduit.entities.Article;
import io.vertx.conduit.entities.User;
import io.vertx.conduit.verticles.ArticleServiceVerticle;
import io.vertx.conduit.verticles.HttpVerticle;
import io.vertx.conduit.verticles.MongoDbServiceVerticle;
import io.vertx.conduit.verticles.UserServiceVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

@RunWith(VertxUnitRunner.class)
public class BaseUnitTest {

    protected static final String CONTENT_TYPE = "Content-Type";
    protected static final String JSON = "application/json";
    protected static final String AUTHORIZATION = "Authorization";
    protected static int PORT = 3000;

    protected Vertx vertx;

    protected WebClient webClient;

    protected User user1;

    protected User user2;

    protected Article testArticle1;

    protected Article testArticle2;

    @Before
    public void setup(TestContext tc) {

        vertx = Vertx.vertx();

        webClient = WebClient.create(vertx);

        DeploymentOptions options = new DeploymentOptions()
                .setConfig(new JsonObject()
                        .put("secret", "testsecret")
                        .put("mongodb", new JsonObject().put("db_name", "conduit_test")
                                .put("connection_string", "mongodb://localhost:27017"))
                );

        JsonObject user1Json = new JsonObject().put("username", "user1").put("password", "123").put("email", "user1@t.com").put("bio", "I am not a robot");
        JsonObject user2Json = new JsonObject().put("username", "user2").put("password", "456").put("email", "user2@t.com").put("bio", "I work 24/7");

        user1 = new User(user1Json);
        user2 = new User(user2Json);

        JsonObject article1Json = new JsonObject().put("title", "first article").put("description", "We have to test it out.").put("body", "Not much to say");
        JsonObject article2Json = new JsonObject().put("title", "second article").put("description", "Last article is really funny").put("body", "It's not over");

        testArticle1 = new Article(article1Json);
        testArticle2 = new Article(article2Json);

        vertx.deployVerticle(ArticleServiceVerticle.class.getName(), tc.asyncAssertSuccess());
        vertx.deployVerticle(HttpVerticle.class.getName(), options, tc.asyncAssertSuccess());
        vertx.deployVerticle(MongoDbServiceVerticle.class.getName(), options, tc.asyncAssertSuccess());
        vertx.deployVerticle(UserServiceVerticle.class.getName(), options, tc.asyncAssertSuccess());
    }

    @Test
    public void testSetup(TestContext testContext){
        System.out.println("base setup complete");
        testContext.assertTrue(true);
    }

    @After
    public void tearDown(TestContext tc) {
        vertx.setTimer(1000, t -> { System.out.println("timer complete"); });
        vertx.close(tc.asyncAssertSuccess());
    }


}

