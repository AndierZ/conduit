package routes;


import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.conduit.entities.Article;
import io.vertx.conduit.entities.User;
import io.vertx.conduit.handlers.ConduitHandler;
import io.vertx.conduit.services.ArticleService;
import io.vertx.conduit.services.CommentService;
import io.vertx.conduit.services.UserService;
import io.vertx.conduit.verticles.*;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class TestBase {

    static {
        System.getProperties().setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.Log4j2LogDelegateFactory");
    }

    protected final static int TIMEOUT = 5000;
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

    protected io.vertx.conduit.services.reactivex.UserService userService;

    protected io.vertx.conduit.services.reactivex.ArticleService articleService;

    protected io.vertx.conduit.services.reactivex.CommentService commentService;

    @Before
    public void setup(TestContext tc) {

        vertx = Vertx.vertx();

        webClient = WebClient.create(vertx);

        DeploymentOptions options = new DeploymentOptions()
                .setConfig(new JsonObject()
                        .put("secret", "testsecret")
                        .put("mongodb", new JsonObject().put("db_name", "conduit_test")
                                                        .put("host", "localhost")
                                                        .put("port", 27017))
                );

        JsonObject user1Json = new JsonObject().put("username", "user1").put("password", "123").put("email", "user1@t.com").put("bio", "I am not a robot");
        JsonObject user2Json = new JsonObject().put("username", "user2").put("password", "456").put("email", "user2@t.com").put("bio", "I work 24/7");

        user1 = new User(user1Json);
        user2 = new User(user2Json);

        JsonObject article1Json = new JsonObject().put("title", "first article").put("slug", "first-article").put("description", "We have to test it out.").put("body", "Not much to say").put("comments", new JsonArray()).put("tagsList", new JsonArray().add("food").add("travel"));
        JsonObject article2Json = new JsonObject().put("title", "second article").put("slug", "second-article").put("description", "Last article is really funny").put("body", "It's not over").put("comments", new JsonArray()).put("tagsList", new JsonArray().add("music").add("dance"));

        testArticle1 = new Article(article1Json);
        testArticle2 = new Article(article2Json);

        testArticle1.setAuthor(user1);
        testArticle2.setAuthor(user1);

        vertx.deployVerticle(ArticleServiceVerticle.class.getName(), tc.asyncAssertSuccess());
        vertx.deployVerticle(HttpVerticle.class.getName(), options, tc.asyncAssertSuccess());
        vertx.deployVerticle(MorphiaServiceVerticle.class.getName(), options, tc.asyncAssertSuccess());
        vertx.deployVerticle(UserServiceVerticle.class.getName(), options, tc.asyncAssertSuccess());
        vertx.deployVerticle(CommentServiceVerticle.class.getName(), options, tc.asyncAssertSuccess());

        {
            ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(UserService.ADDRESS);
            UserService delegate = builder.build(UserService.class);
            this.userService = new io.vertx.conduit.services.reactivex.UserService(delegate);
        }

        {
            ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(ArticleService.ADDRESS);
            ArticleService delegate = builder.build(ArticleService.class);
            this.articleService = new io.vertx.conduit.services.reactivex.ArticleService(delegate);
        }

        {
            ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(CommentService.ADDRESS);
            CommentService delegate = builder.build(CommentService.class);
            this.commentService = new io.vertx.conduit.services.reactivex.CommentService(delegate);
        }
    }

    protected void cleanupUser(TestContext tc) {
        Async cleanup = tc.async();
        userService.rxDeleteByUsername(user1.getUsername())
                .flatMap(ignored -> userService.rxDeleteByUsername(user2.getUsername()))
                .subscribe((deleteCount, ex) -> {
                    cleanup.complete();
                });

        cleanup.awaitSuccess();
    }

    protected void cleanupArticles(TestContext tc) {
        Async cleanup = tc.async();
        articleService.rxDelete(testArticle1.getSlug())
                .flatMap(ignored -> articleService.rxDelete(testArticle2.getSlug()))
                .subscribe((deleteCount, ex) -> {
                    cleanup.complete();
                });

        cleanup.awaitSuccess();
    }

    protected void registerUser(TestContext tc, User user) {
        Async create = tc.async();

        webClient.post(PORT, "localhost", "/api/users")
                .putHeader(CONTENT_TYPE, JSON)
                .sendJsonObject(new JsonObject()
                        .put(ConduitHandler.USER, user.toJson()
                        ), ar -> {
                    if (ar.succeeded()) {
                        tc.assertEquals(HttpResponseStatus.CREATED.code(), ar.result().statusCode());
                        JsonObject returnedUser = ar.result().bodyAsJsonObject();
                        tc.assertEquals(user.getUsername(), returnedUser.getString("username"));
                        tc.assertEquals(user.getEmail(), returnedUser.getString("email"));
                        tc.assertEquals(user.getBio(), returnedUser.getString("bio"));
                        tc.assertNull(returnedUser.getString("image"));
                        user.setId(user.getId());
                        create.complete();
                    } else {
                        tc.fail(ar.cause());
                    }
                });

        create.awaitSuccess();
    }

    protected void loginUser(TestContext tc, User user) {
        Async login = tc.async();

        webClient.post(PORT, "localhost", "/api/users/login")
                .putHeader(CONTENT_TYPE, JSON)
                .sendJsonObject(new JsonObject().put(ConduitHandler.USER, new JsonObject()
                                .put("email", user.getEmail())
                                .put("password", user.getPassword())),
                        ar -> {
                            if (ar.succeeded()) {
                                tc.assertEquals(HttpResponseStatus.CREATED.code(), ar.result().statusCode());
                                JsonObject returnedUser = ar.result().bodyAsJsonObject();
                                tc.assertNotNull(returnedUser.getString(ConduitHandler.TOKEN));
                                tc.put("jwt", returnedUser.getString(ConduitHandler.TOKEN));
                                login.complete();
                            } else {
                                tc.fail(ar.cause());
                            }
                        });

        login.awaitSuccess();
    }

    protected void createArticle(TestContext tc, Article article) {
        Async createArticle = tc.async();

        // Must include the forward slash at the end

        webClient.post(PORT, "localhost", "/api/articles/")
                .putHeader(CONTENT_TYPE, JSON)
                .putHeader(AUTHORIZATION, getJwt(tc))
                .sendJsonObject(new JsonObject()
                        .put(ConduitHandler.ARTICLE, article.toJson()
                        ), ar -> {
                    if (ar.succeeded()) {
                        tc.assertEquals(HttpResponseStatus.CREATED.code(), ar.result().statusCode());
                        JsonObject json = ar.result().bodyAsJsonObject().getJsonObject(ConduitHandler.ARTICLE);
                        tc.assertNotNull(json);
                        JsonObject expected = article.toJsonFor(user1);
                        expected.put("_id", json.getString("_id"));
                        tc.assertEquals(expected, json);
                        article.setId(new ObjectId(json.getString("_id")));
                        createArticle.complete();
                    } else {
                        tc.fail();
                    }
                });

        createArticle.awaitSuccess();
    }


    protected static String getJwt(TestContext tc) {
        return ConduitHandler.TOKEN + " " + tc.get("jwt").toString();
    }

    @Test
    public void testSetup(TestContext testContext){
        testContext.assertTrue(true);
    }

    @After
    public void tearDown(TestContext tc) {
        vertx.setTimer(1000, t -> { System.out.println("timer complete"); });
        vertx.close(tc.asyncAssertSuccess());
    }


}

