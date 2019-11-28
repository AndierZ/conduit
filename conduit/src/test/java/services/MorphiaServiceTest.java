package services;

import io.vertx.conduit.entities.User;
import io.vertx.conduit.services.MorphiaServiceImpl;
import io.vertx.conduit.services.reactivex.MorphiaService;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import logging.ContextLogger;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(VertxUnitRunner.class)
public class MorphiaServiceTest {

    private static Logger LOGGER = ContextLogger.create();

    private Vertx vertx;
    private MorphiaService morphiaService;

    @Before
    public void setup(TestContext tc) {
        this.vertx = Vertx.vertx();
        JsonObject config = new JsonObject().put("db_name", "conduit_test")
                .put("host", "localhost").put("port", 27017).put("entityPackage", "io.vertx.conduit.entities");
        MorphiaServiceImpl delegate = new MorphiaServiceImpl(vertx, config, tc.asyncAssertSuccess());
        this.morphiaService = new io.vertx.conduit.services.reactivex.MorphiaService(delegate);
    }

    @Test
    public void TestWriteUser(TestContext tc) throws InterruptedException {
        Async async = tc.async();

        System.out.println("Test writing user");

        User user = new User();
        user.setPassword("123");
        user.setBio("abc");
        user.setEmail("1@2.com");
        user.setUsername("xyz");

        this.morphiaService.rxCreateUser(user)
                           .flatMap(id -> {
                              LOGGER.info("Successfully created document with id " + id);
                              user.setId(new ObjectId(id));
                              return this.morphiaService.rxGetUser(new JsonObject().put("_id", user.getId().toHexString()));
                           }).flatMap(users -> {
                              assertEquals(1, users.size());
                              assertEquals(user.getUsername(), users.get(0).getUsername());
                              user.setBio("bio_updated");
                              return this.morphiaService.rxUpdateUser(new JsonObject().put("_id", user.getId().toHexString()), new JsonObject().put("bio", user.getBio()));
                           }).flatMap(users -> {
                              assertEquals(1, users.size());
                              assertEquals(user.getBio(), users.get(0).getBio());
                              return this.morphiaService.rxDeleteUser(new JsonObject().put("_id", user.getId().toHexString()));
                           }).subscribe((numDeleted, e) -> {
                              if (e == null) {
                                  assertEquals(1, (int) numDeleted);
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
