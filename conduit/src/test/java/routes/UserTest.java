package routes;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class UserTest extends BaseUnitTest {

    @Test
    public void testRegisterUser(TestContext tc) {
        Async async = tc.async();

        webClient.post(PORT, "localhost", "/api/users")
                .putHeader(CONTENT_TYPE, JSON)
                .sendJsonObject(new JsonObject()
                        .put("user", user1.toJson()
                        ), ar -> {
                    if (ar.succeeded()) {
                        tc.assertEquals(HttpResponseStatus.CREATED, ar.result().statusCode());
                        JsonObject returnedJson = ar.result().bodyAsJsonObject();
                        tc.assertNotNull(returnedJson);
                        JsonObject returnedUser = returnedJson.getJsonObject("user");
                        tc.assertEquals(user1.getUsername(), returnedUser.getString("username"));
                        tc.assertEquals(user1.getEmail(), returnedUser.getString("email"));
                        tc.assertEquals(user1.getBio(), returnedUser.getString("bio"));
                        tc.assertNull(returnedUser.getString("image"));
                        async.complete();
                    } else {
                        tc.fail(ar.cause());
                    }

                });
    }
}
