package routes;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.codegen.annotations.Nullable;
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

    @Before
    public void setup(TestContext tc) {
        super.setup(tc);
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(UserService.ADDRESS);
        UserService delegate = builder.build(UserService.class);
        this.userService = new io.vertx.conduit.services.reactivex.UserService(delegate);
    }

    @Test(timeout = 5000)
    public void testRegisterUser(TestContext tc) {
        Async cleanup = tc.async();
        userService.rxDeleteByUsername(user1.getUsername())
                .subscribe((deleteCount, ex) -> {
                    cleanup.complete();
                });

        cleanup.awaitSuccess();

        Async create = tc.async();

        webClient.post(PORT, "localhost", "/api/users")
                .putHeader(CONTENT_TYPE, JSON)
                .putHeader(XREQUESTEDWITH, XMLHTTPREQUEST)
                .sendJsonObject(new JsonObject()
                        .put("user", user1.toJson()
                        ), ar -> {
                    if (ar.succeeded()) {
                        tc.assertEquals(HttpResponseStatus.CREATED.code(), ar.result().statusCode());
                        JsonObject returnedUser = ar.result().bodyAsJsonObject();
                        tc.assertEquals(user1.getUsername(), returnedUser.getString("username"));
                        tc.assertEquals(user1.getEmail(), returnedUser.getString("email"));
                        tc.assertEquals(user1.getBio(), returnedUser.getString("bio"));
                        tc.assertNull(returnedUser.getString("image"));
                        create.complete();
                    } else {
                        tc.fail(ar.cause());
                    }
                });

        create.awaitSuccess();

        Async createDupe = tc.async();

        webClient.post(PORT, "localhost", "/api/users")
                .putHeader(CONTENT_TYPE, JSON)
                .putHeader(XREQUESTEDWITH, XMLHTTPREQUEST)
                .sendJsonObject(new JsonObject()
                        .put("user", user1.toJson()
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

        Async delete = tc.async();
        userService.rxDeleteByUsername(user1.getUsername())
                   .subscribe((deleteCount, ex) -> {
                       tc.assertNull(ex);
                       tc.assertEquals(deleteCount, 1);
                       delete.complete();
                   });

    }

    @After
    public void tearDown(TestContext tc) {
        super.tearDown(tc);
    }
}
