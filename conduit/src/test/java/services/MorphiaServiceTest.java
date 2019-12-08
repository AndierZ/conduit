package services;

import io.vertx.conduit.entities.Article;
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

import static org.junit.Assert.*;

@RunWith(VertxUnitRunner.class)
public class MorphiaServiceTest {

    private Vertx vertx;
    private MorphiaService morphiaService;

    @Before
    public void setup(TestContext tc) {
        this.vertx = Vertx.vertx();
        JsonObject config = new JsonObject().put("db_name", "conduit_test")
                .put("host", "localhost").put("port", 27017);
        MorphiaServiceImpl delegate = new MorphiaServiceImpl(vertx, config, tc.asyncAssertSuccess());
        this.morphiaService = new io.vertx.conduit.services.reactivex.MorphiaService(delegate);
    }

    @Test
    public void UserCRUD(TestContext tc) throws InterruptedException {
        Async async = tc.async();

        User user = new User();
        user.setPassword("123");
        user.setBio("abc");
        user.setEmail("1@2.com");
        user.setUsername("xyz");

        this.morphiaService.rxCreateUser(user)
           .flatMap(id -> {
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
                  assertEquals(1, (long) numDeleted);
                  async.complete();
              } else {
                  tc.fail(e);
              }
           });
    }

    @Test
    public void userInvalid(TestContext tc) {
        Async async1 = tc.async();
        Async async2 = tc.async();
        Async async3 = tc.async();
        Async async4 = tc.async();

        User user = new User();
        user.setPassword("123");
        user.setBio("abc");

        this.morphiaService.rxCreateUser(user)
                .subscribe((id, e) -> {
                    if (e == null) {
                        tc.fail("User should not be created");
                    } else {
                        async1.complete();
                    }
                });

        user.setUsername("xyz");
        this.morphiaService.rxCreateUser(user)
                .subscribe((id, e) -> {
                    if (e == null) {
                        tc.fail("User should not be created");
                    } else {
                        async2.complete();
                    }
                });

        user.setEmail("123");
        this.morphiaService.rxCreateUser(user)
                .subscribe((id, e) -> {
                    if (e == null) {
                        tc.fail("User should not be created");
                    } else {
                        async3.complete();
                    }
                });

        user.setEmail("123@xyz.com");
        user.setUsername("#*$");
        this.morphiaService.rxCreateUser(user)
                .subscribe((id, e) -> {
                    if (e == null) {
                        tc.fail("User should not be created");
                    } else {
                        async4.complete();
                    }
                });
    }

    @Test
    public void userDupe(TestContext tc) throws InterruptedException {
        Async async1 = tc.async();
        Async async2 = tc.async();
        Async async3 = tc.async();
        Async async4 = tc.async();

        User user = new User();
        user.setPassword("123");
        user.setBio("abc");
        user.setEmail("1@2.com");
        user.setUsername("xyz");

        User user2 = new User();
        user2.setPassword("123");
        user2.setBio("abc");
        user2.setEmail("3@4.com");
        user2.setUsername("xyz");

        User user3 = new User();
        user3.setPassword("123");
        user3.setBio("abc");
        user3.setEmail("1@2.com");
        user3.setUsername("xyz2");

        this.morphiaService.rxCreateUser(user)
        .subscribe((id, e) -> {
            if (e == null) {
                user.setId(new ObjectId(id));
                async1.complete();
            } else {
                tc.fail(e);
            }
        });

        async1.await();
        this.morphiaService.rxCreateUser(user2)
                .subscribe((id, e) -> {
                    if (e == null) {
                        tc.fail("User2 shouldn't be written");
                    }
                    async2.complete();
                });

        async2.await();

        this.morphiaService.rxCreateUser(user3)
                .subscribe((id, e) -> {
                    if (e == null) {
                        tc.fail("User3 shouldn't be written");
                    }
                    async3.complete();
                });

        async3.await();

        this.morphiaService.rxDeleteUser(new JsonObject().put("_id", user.getId().toHexString()))
                .subscribe((numDeleted, e) -> {
                    if (e == null) {
                        assertEquals(1, (long) numDeleted);
                        async4.complete();
                    } else {
                        tc.fail(e);
                    }
                });
    }

    @Test
    public void ArticleCRUD(TestContext tc) {
        Async async = tc.async();

        User user = new User();
        user.setPassword("123");
        user.setBio("abc");
        user.setEmail("1@2.com");
        user.setUsername("xyz");

        Article article = new Article();
        article.setAuthor(user);
        article.setTitle("Test article");
        article.setSlug("Test-article");
        article.setBody("No body no body");

        // have to store the user first and assign it an id for the @Reference annotation to work

        this.morphiaService.rxCreateUser(user)
        .flatMap(id -> {
            user.setId(new ObjectId(id));
            return this.morphiaService.rxCreateArticle(article);
        }).flatMap(id -> {
            article.setId(new ObjectId(id));
            return this.morphiaService.rxGetArticle(new JsonObject().put("_id", article.getId().toHexString()));
        }).flatMap(articles -> {
            assertEquals(1, articles.size());
            assertEquals(article.getSlug(), articles.get(0).getSlug());
            assertNotNull(article.getAuthor());
            assertEquals(user.getUsername(), article.getAuthor().getUsername());
            article.setBody("but you~");
            return this.morphiaService.rxUpdateArticle(new JsonObject().put("_id", article.getId().toHexString()), new JsonObject().put("body", article.getBody()));
        }).flatMap(articles -> {
            assertEquals(1, articles.size());
            assertEquals(article.getBody(), articles.get(0).getBody());
            return this.morphiaService.rxDeleteArticle(new JsonObject().put("_id", article.getId().toHexString()));
        }).flatMap(numDeleted -> {
            assertEquals(1, (long) numDeleted);
            return this.morphiaService.rxDeleteUser(new JsonObject().put("_id", user.getId().toHexString()));
        }).subscribe((numDeleted, e) -> {
            if (e == null) {
                assertEquals(1, (long) numDeleted);
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
