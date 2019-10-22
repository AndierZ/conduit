import LoggingUtils.ContextLogger;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.ext.web.Router;

public class HttpVerticle extends AbstractVerticle {

    private static Logger LOGGER = ContextLogger.create();

    @Override
    public void start(Future<Void> startFuture) {
        LOGGER.info("Starting Http Verticle with config {}", "test");

        Router baseRouter = Router.router(vertx);

        baseRouter.route("/").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.putHeader("content-type", "text/plain").end("Hello Vert.x!");
        });

        vertx.createHttpServer()
                .requestHandler(baseRouter)
                .listen(3000, ar -> {
                    if (ar.succeeded()) {
                        startFuture.complete();
                    } else {
                        startFuture.fail(ar.cause());
                    }
                });
    }

    public static void main(String[] args) throws Exception {
        HttpVerticle v = new HttpVerticle();
        v.start(null);
    }

}
