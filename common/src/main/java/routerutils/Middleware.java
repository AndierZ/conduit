package routerutils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@SuppressWarnings("unused")
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.SOURCE)
public @interface Middleware {
}
