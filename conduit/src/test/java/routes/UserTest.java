package routes;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.conduit.entities.User;
import io.vertx.conduit.handlers.UserHandler;
import io.vertx.conduit.services.UserService;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class UserTest extends BaseUnitTest {

    private io.vertx.conduit.services.reactivex.UserService userService;
    private static final int TIMEOUT = 3000;

    @Before
    public void setup(TestContext tc) {
        super.setup(tc);
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(UserService.ADDRESS);
        UserService delegate = builder.build(UserService.class);
        this.userService = new io.vertx.conduit.services.reactivex.UserService(delegate);
    }

    private void cleanupUser(TestContext tc) {
        Async cleanup = tc.async();
        userService.rxDeleteByUsername(user1.getUsername())
                .flatMap(ignored -> userService.rxDeleteByUsername(user2.getUsername()))
                .subscribe((deleteCount, ex) -> {
                    cleanup.complete();
                });

        cleanup.awaitSuccess();
    }

    private void registerUser(TestContext tc, User user) {
        Async create = tc.async();

        webClient.post(PORT, "localhost", "/api/users")
                .putHeader(CONTENT_TYPE, JSON)
                .sendJsonObject(new JsonObject()
                        .put(UserHandler.USER, user.toJson()
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

    @Test(timeout = TIMEOUT)
    public void testRegisterUser(TestContext tc) {
        cleanupUser(tc);

        registerUser(tc, user1);

        Async createDupe = tc.async();

        webClient.post(PORT, "localhost", "/api/users")
                .putHeader(CONTENT_TYPE, JSON)
                .sendJsonObject(new JsonObject()
                        .put(UserHandler.USER, user1.toJson()
                        ), ar -> {
                    if (ar.succeeded()) {
                        String body = ar.result().bodyAsString();
                        tc.assertEquals("Internal Server Error", body);
                        createDupe.complete();
                    } else {
                        tc.fail();
                    }
                });

        createDupe.awaitSuccess();
    }

    private void loginUser(TestContext tc, User user) {
        Async login = tc.async();

        webClient.post(PORT, "localhost", "/api/users/login")
                .putHeader(CONTENT_TYPE, JSON)
                .sendJsonObject(new JsonObject().put(UserHandler.USER, new JsonObject()
                        .put("email", user.getEmail())
                        .put("password", user.getPassword())),
                        ar -> {
                    if (ar.succeeded()) {
                        tc.assertEquals(HttpResponseStatus.CREATED.code(), ar.result().statusCode());
                        JsonObject returnedUser = ar.result().bodyAsJsonObject();
                        tc.assertNotNull(returnedUser.getString(UserHandler.TOKEN));
                        tc.put("jwt", returnedUser.getString(UserHandler.TOKEN));
                        login.complete();
                    } else {
                        tc.fail(ar.cause());
                    }
                });

        login.awaitSuccess();
    }

    @Test(timeout = TIMEOUT)
    public void testUpdateUser(TestContext tc) {
        cleanupUser(tc);
        registerUser(tc, user1);
        loginUser(tc, user1);

        Async update = tc.async();

        webClient.post(PORT, "localhost", "/api/user")
                .putHeader(CONTENT_TYPE, JSON)
                .putHeader(AUTHORIZATION, getJwt(tc))
                .sendJsonObject(new JsonObject().put(UserHandler.USER, new JsonObject()
                        .put("bio", "updatedBio")),
                        ar -> {
                    if (ar.succeeded()) {
                        tc.assertEquals(HttpResponseStatus.OK.code(), ar.result().statusCode());
                        JsonObject returnedUser = ar.result().bodyAsJsonObject();
                        tc.assertEquals(user1.getUsername(), returnedUser.getString("username"));
                        tc.assertEquals(user1.getEmail(), returnedUser.getString("email"));
                        tc.assertEquals("updatedBio", returnedUser.getString("bio"));
                        tc.assertNull(returnedUser.getString("image"));
                        update.complete();
                    } else {
                        tc.fail(ar.cause());
                    }
                });
    }

    @Test(timeout = TIMEOUT)
    public void testGetUser(TestContext tc) {
        cleanupUser(tc);
        registerUser(tc, user1);
        loginUser(tc, user1);

        Async get = tc.async();

        webClient.get(PORT, "localhost", "/api/user")
                .putHeader(CONTENT_TYPE, JSON)
                .putHeader(AUTHORIZATION, getJwt(tc))
                .send(
                      ar -> {
                            if (ar.succeeded()) {
                                tc.assertEquals(HttpResponseStatus.OK.code(), ar.result().statusCode());
                                JsonObject returnedUser = ar.result().bodyAsJsonObject();
                                tc.assertEquals(user1.toAuthJson(), returnedUser);
                                get.complete();
                            } else {
                                tc.fail(ar.cause());
                            }
                        });
    }

    @Test(timeout = TIMEOUT)
    public void testGetProfile(TestContext tc) {
        cleanupUser(tc);
        registerUser(tc, user1);

        Async getProfileNoLogin = tc.async();

        webClient.get(PORT, "localhost", "/api/profiles/user1")
                .putHeader(CONTENT_TYPE, JSON)
                .send(
                        ar -> {
                            if (ar.succeeded()) {
                                tc.assertEquals(HttpResponseStatus.OK.code(), ar.result().statusCode());
                                JsonObject json = ar.result().bodyAsJsonObject().getJsonObject("profile");
                                tc.assertEquals(user1.toProfileJsonFor(user1), json);
                                getProfileNoLogin.complete();
                            } else {
                                tc.fail(ar.cause());
                            }
                        });

        getProfileNoLogin.awaitSuccess();

        loginUser(tc, user1);

        Async getProfileWithLogin = tc.async();

        webClient.get(PORT, "localhost", "/api/profiles/user1")
                .putHeader(CONTENT_TYPE, JSON)
                .putHeader(AUTHORIZATION, getJwt(tc))
                .send(
                        ar -> {
                            if (ar.succeeded()) {
                                tc.assertEquals(HttpResponseStatus.OK.code(), ar.result().statusCode());
                                JsonObject json = ar.result().bodyAsJsonObject().getJsonObject("profile");
                                tc.assertEquals(user1.toProfileJsonFor(user1), json);
                                getProfileWithLogin.complete();
                            } else {
                                tc.fail(ar.cause());
                            }
                        });
    }

    @Test(timeout = TIMEOUT)
    public void testFollow(TestContext tc) {
        cleanupUser(tc);
        registerUser(tc, user1);
        registerUser(tc, user2);
        loginUser(tc, user1);

        Async follow = tc.async();

        webClient.post(PORT, "localhost", "/api/profiles/user2/follow")
                .putHeader(CONTENT_TYPE, JSON)
                .putHeader(AUTHORIZATION, getJwt(tc))
                .send(
                        ar -> {
                            if (ar.succeeded()) {
                                tc.assertEquals(HttpResponseStatus.OK.code(), ar.result().statusCode());
                                JsonObject json = ar.result().bodyAsJsonObject().getJsonObject("profile");
                                JsonObject jsonExpected = user2.toProfileJsonFor(user1);
                                jsonExpected.put("following", true);

                                tc.assertEquals(jsonExpected, json);
                                follow.complete();
                            } else {
                                tc.fail(ar.cause());
                            }
                        });

        follow.awaitSuccess();

        Async unfollow = tc.async();

        webClient.delete(PORT, "localhost", "/api/profiles/user2/follow")
                .putHeader(CONTENT_TYPE, JSON)
                .putHeader(AUTHORIZATION, getJwt(tc))
                .send(
                        ar -> {
                            if (ar.succeeded()) {
                                tc.assertEquals(HttpResponseStatus.OK.code(), ar.result().statusCode());
                                JsonObject json = ar.result().bodyAsJsonObject().getJsonObject("profile");
                                JsonObject jsonExpected = user2.toProfileJsonFor(user1);
                                jsonExpected.put("following", false);

                                tc.assertEquals(jsonExpected, json);
                                unfollow.complete();
                            } else {
                                tc.fail(ar.cause());
                            }
                        });

        unfollow.awaitSuccess();
    }

    private static String getJwt(TestContext tc) {
        return UserHandler.TOKEN + " " + tc.get("jwt").toString();
    }

    @After
    public void tearDown(TestContext tc) {
        cleanupUser(tc);
        super.tearDown(tc);
    }
}
