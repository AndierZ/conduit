package services;

import io.vertx.conduit.services.MorphiaServiceImpl;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MorphiaServiceTest {

    private static final String COLLECTION = "users";
    private Vertx vertx;
    private MorphiaServiceImpl mongoDbService;

    @Before
    public void setup(TestContext tc) {
        this.vertx = Vertx.vertx();
        JsonObject config = new JsonObject().put("db_name", "conduit_test")
                .put("host", "localhost").put("port", 27018).put("entityPackage", "io.vertx.conduit.entitiesÎ");
        this.mongoDbService = new MorphiaServiceImpl(vertx, config, tc.asyncAssertSuccess());
    }


    @Test
    public void TestWriteUser(TestContext tc){
        System.out.println("Test writing user");
        JsonObject json = new JsonObject();
        json.put("username", "test1")
                .put("email", "test@test.com");

//        Async async = tc.async();
//        this.mongoDbService.insertOne(COLLECTION, json, ar -> {
//            if (ar.succeeded()) {
//                async.complete();
//            } else {
//                tc.fail(ar.cause());
//            }
//        });

    }

    @After
    public void tearDown(TestContext tc) {
        vertx.setTimer(1000, t -> { System.out.println("timer complete"); });
        vertx.close(tc.asyncAssertSuccess());
    }
}
