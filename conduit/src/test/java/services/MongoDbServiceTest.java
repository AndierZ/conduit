package services;

import io.vertx.conduit.entities.User;
import io.vertx.conduit.services.MongoDbServiceImpl;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.mongo.MongoClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(VertxUnitRunner.class)
public class MongoDbServiceTest {

    private static final String COLLECTION = "users";
    private Vertx vertx;
    private MongoDbServiceImpl mongoDbService;

    @Before
    public void setup(TestContext tc) {
        this.vertx = Vertx.vertx();
        JsonObject config = new JsonObject().put("db_name", "conduit_test")
                .put("connection_string", "mongodb://localhost:27017");
        final MongoClient mongoClient = MongoClient.createShared(vertx, config);
        this.mongoDbService = new MongoDbServiceImpl(mongoClient, tc.asyncAssertSuccess());
    }


    @Test
    public void TestWriteUser(TestContext tc){
        System.out.println("Test writing user");
        JsonObject json = new JsonObject();
        json.put("username", "test1")
                .put("email", "test@test.com");

        Async async = tc.async();
        this.mongoDbService.insertOne(COLLECTION, json, ar -> {
            if (ar.succeeded()) {
                async.complete();
            } else {
                tc.fail(ar.cause());
            }
        });

    }

    @After
    public void tearDown(TestContext tc) {
        vertx.setTimer(1000, t -> { System.out.println("timer complete"); });
        vertx.close(tc.asyncAssertSuccess());
    }
}
