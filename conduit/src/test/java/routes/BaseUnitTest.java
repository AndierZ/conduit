package routes;


import io.vertx.conduit.verticles.ArticleServiceVerticle;
import io.vertx.conduit.verticles.HttpVerticle;
import io.vertx.conduit.verticles.MongoDbServiceVerticle;
import io.vertx.conduit.verticles.UserServiceVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.reactivex.core.Vertx;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class BaseUnitTest {

    protected Vertx vertx;

    @Before
    public void setUp(TestContext tc) {

        vertx = Vertx.vertx();

        DeploymentOptions options = new DeploymentOptions()
                .setConfig(new JsonObject()
                        .put("secret", "testsecret")
                        .put("mongodb", new JsonObject().put("db_name", "conduit_test")
                                .put("connection_string", "mongodb://localhost:27017"))
                );

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

