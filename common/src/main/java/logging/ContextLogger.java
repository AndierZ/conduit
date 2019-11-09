package logging;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.lang.management.ManagementFactory;

public class ContextLogger {

    public static Logger create() {
        StackTraceElement[] stackTrade = ManagementFactory.getThreadMXBean().getThreadInfo(Thread.currentThread().getId(), 5).getStackTrace();
        String clz = stackTrade[4].getClassName();
        return LoggerFactory.getLogger(clz);
    }
}
