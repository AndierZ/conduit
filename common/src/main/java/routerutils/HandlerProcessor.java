package routerutils;

import logging.ContextLogger;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

public final class HandlerProcessor {

    private static Logger LOGGER = ContextLogger.create();

    public static <H, T extends Handler<RoutingContext>> void buildHandler(final Router router, final T preHandler, final H handler, final AuthHandler authHandler) {
        Class<?> clazz = handler.getClass();
        RouteConfig baseRouteConfig = clazz.getAnnotation(RouteConfig.class);
        if (baseRouteConfig != null) {
            String[] baseConsumes = baseRouteConfig.consumes();
            String[] baseProduces = baseRouteConfig.produces();

            String basePath = baseRouteConfig.path().length() != 0 ? baseRouteConfig.path() : "";
            Set<Method> methods = getMethodsAnnotatedWith(clazz, RouteConfig.class);
            methods.forEach(method -> {
                RouteConfig annotation = method.getAnnotation(RouteConfig.class);
                String[] methodConsumes = annotation.consumes();
                String[] methodProduces = annotation.produces();
                String methodPath = annotation.path();
                HttpMethod httpMethod = annotation.method();
                boolean authRequired = annotation.authRequired();

                String path = basePath + methodPath;
                Route route = router.route(httpMethod, path);
                setMediaType(route, methodConsumes.length != 0 ? methodConsumes : baseConsumes , false);
                setMediaType(route, methodProduces.length != 0 ? methodProduces : baseProduces , true);
                if (authRequired) {
                    if (authHandler == null) {
                        throw new IllegalArgumentException("authHandler must be specified when authRequired = true");
                    }
                    route.handler(authHandler);
                }
                createHandler(preHandler, handler, method, route);
            });
        }
    }

    private static <H, T extends Handler<RoutingContext>> void createHandler(final T preHandler, final H handler, final Method method, final Route route) {
        route.handler(preHandler).handler(event -> {
            try {
                method.invoke(handler, event);
            } catch (Exception e) {
                LOGGER.error("Error calling handler {}.{}", e, handler.getClass(), method.getName());
            }
        });
    }

    private static void setMediaType(final Route route, final String[] mediaTypes, final boolean isProduced) {
        if (null != mediaTypes && mediaTypes.length > 0) {
            Arrays.asList(mediaTypes).forEach(contentType -> {
                if (!isProduced) {
                    route.consumes(contentType);
                } else {
                    route.produces(contentType);
                }
            });
        }
    }

    private static Set<Method> getMethodsAnnotatedWith(final Class<?> type,
                                                       final Class<? extends Annotation> annotation) {
        final List<String> methodNames = new ArrayList<>();
        final Set<Method> methods = new HashSet<>();
        Class<?> clazz = type;
        while (clazz != Object.class) {
            final List<Method> allMethods = new ArrayList(Arrays.asList(clazz.getDeclaredMethods()));
            for (final Method method : allMethods) {
                if (method.isAnnotationPresent(annotation)) {
                    if (!methodNames.contains(method.getName())) {
                        methods.add(method);
                        methodNames.add(method.getName());
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return methods;
    }
}