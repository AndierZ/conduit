package routerutils;

import io.vertx.core.http.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RouteConfig {
	String path();

	HttpMethod method() default HttpMethod.GET;

	String[] consumes() default {};

	String[] produces() default {};

	boolean authRequired() default true;
}