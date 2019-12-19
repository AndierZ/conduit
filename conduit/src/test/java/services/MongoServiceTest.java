package services;

import io.vertx.conduit.entities.User;
import io.vertx.conduit.services.MongoDbServiceImpl;
import io.vertx.conduit.services.reactivex.MongoDbService;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.mongo.MongoClient;
import logging.ContextLogger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(VertxUnitRunner.class)
public class MongoServiceTest {

    private static final String USER_COLLETION = "users";
    private static Logger LOGGER = ContextLogger.create();

    private Vertx vertx;
    private MongoDbService mongoDbService;

    @Before
    public void setup(TestContext tc) {
        this.vertx = Vertx.vertx();
        JsonObject config = new JsonObject().put("db_name", "conduit_test")
                .put("connection_string", "mongodb://localhost:27017").put("useObjectId", true);
        MongoClient mongoClient = MongoClient.createShared(vertx, config);
        MongoDbServiceImpl delegate = new MongoDbServiceImpl(mongoClient, tc.asyncAssertSuccess());
        this.mongoDbService = new io.vertx.conduit.services.reactivex.MongoDbService(delegate);
    }

    @Test
    public void TestWriteUser(TestContext tc) {
        Async async = tc.async();

        System.out.println("Test writing user");

        JsonObject user = new JsonObject();
        user.put("password", "123");
        user.put("bio", "abc");
        user.put("email", "1@2.com");
        user.put("username", "xyz");

        this.mongoDbService.rxInsertOne(USER_COLLETION, user)
                           .map(id -> {
                              LOGGER.info("Creating document");
                               user.put("id", user.getString("_id"));
                               user.remove("_id");
                              User userEntity = new User(user);
                              return userEntity;
                           }).flatMap(userEntity -> {
                               LOGGER.info("Reading document");
                               return this.mongoDbService.rxFindById(USER_COLLETION, userEntity.getId().toHexString(), null);
                           }).flatMap(json -> {
                               LOGGER.info("Updating document");
                               json.put("id", json.getString("_id"));
                               json.remove("_id");
                               assertTrue(user.equals(json));
                               JsonObject updateJson = new JsonObject();
                               updateJson.put("email", "3@4.com");
                               user.put("email", "3@4.com");
                               return this.mongoDbService.rxFindOneAndUpdate(USER_COLLETION, new JsonObject().put("_id", user.getString("id")), updateJson, new FindOptions(), new UpdateOptions());
                           }).flatMap(json -> {
                               LOGGER.info("Reading updated document");
                               // FIXME why does this return a nested object id?
                               return this.mongoDbService.rxFindById(USER_COLLETION, json.getJsonObject("_id").getString("$oid"), null);
                           }).flatMap(json -> {
                                json.put("id", json.getString("_id"));
                                json.remove("_id");
                                assertTrue(user.equals(json));
                                LOGGER.info("Deleting document: " + json);
                                return this.mongoDbService.rxDelete(USER_COLLETION, new JsonObject().put("_id", user.getString("id")));
                    }).subscribe((res, e) -> {
                        if (e == null) {
                            async.complete();
                        } else {
                            tc.fail(e);
                        }
                    });
    }

    @After
    public void tearDown(TestContext tc) {
        vertx.setTimer(1000, t -> { System.out.println("timer complete"); });
        vertx.close(tc.asyncAssertSuccess());
    }
}
