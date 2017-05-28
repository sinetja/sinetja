package sinetja;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

public class Log {
    private static final Logger log = LoggerFactory.getLogger(Log.class);

    public static void debug(String arg0) {
        log.debug(arg0);
    }

    public static void debug(String arg0, Object arg1) {
        log.debug(arg0, arg1);
    }

    public static void debug(String arg0, Object... arg1) {
        log.debug(arg0, arg1);
    }

    public static void debug(String arg0, Throwable arg1) {
        log.debug(arg0, arg1);
    }

    public static void debug(Marker arg0, String arg1) {
        log.debug(arg0, arg1);
    }

    public static void debug(String arg0, Object arg1, Object arg2) {
        log.debug(arg0, arg1, arg2);
    }

    public static void debug(Marker arg0, String arg1, Object arg2) {
        log.debug(arg0, arg1, arg2);
    }

    public static void debug(Marker arg0, String arg1, Object... arg2) {
        log.debug(arg0, arg1, arg2);
    }

    public static void debug(Marker arg0, String arg1, Throwable arg2) {
        log.debug(arg0, arg1, arg2);
    }

    public static void debug(Marker arg0, String arg1, Object arg2, Object arg3) {
        log.debug(arg0, arg1, arg2, arg3);
    }

    public static void error(String arg0) {
        log.error(arg0);
    }

    public static void error(String arg0, Object arg1) {
        log.error(arg0, arg1);
    }

    public static void error(String arg0, Object... arg1) {
        log.error(arg0, arg1);
    }

    public static void error(String arg0, Throwable arg1) {
        log.error(arg0, arg1);
    }

    public static void error(Marker arg0, String arg1) {
        log.error(arg0, arg1);
    }

    public static void error(String arg0, Object arg1, Object arg2) {
        log.error(arg0, arg1, arg2);
    }

    public static void error(Marker arg0, String arg1, Object arg2) {
        log.error(arg0, arg1, arg2);
    }

    public static void error(Marker arg0, String arg1, Object... arg2) {
        log.error(arg0, arg1, arg2);
    }

    public static void error(Marker arg0, String arg1, Throwable arg2) {
        log.error(arg0, arg1, arg2);
    }

    public static void error(Marker arg0, String arg1, Object arg2, Object arg3) {
        log.error(arg0, arg1, arg2, arg3);
    }

    public static String getName() {
        return log.getName();
    }

    public static void info(String arg0) {
        log.info(arg0);
    }

    public static void info(String arg0, Object arg1) {
        log.info(arg0, arg1);
    }

    public static void info(String arg0, Object... arg1) {
        log.info(arg0, arg1);
    }

    public static void info(String arg0, Throwable arg1) {
        log.info(arg0, arg1);
    }

    public static void info(Marker arg0, String arg1) {
        log.info(arg0, arg1);
    }

    public static void info(String arg0, Object arg1, Object arg2) {
        log.info(arg0, arg1, arg2);
    }

    public static void info(Marker arg0, String arg1, Object arg2) {
        log.info(arg0, arg1, arg2);
    }

    public static void info(Marker arg0, String arg1, Object... arg2) {
        log.info(arg0, arg1, arg2);
    }

    public static void info(Marker arg0, String arg1, Throwable arg2) {
        log.info(arg0, arg1, arg2);
    }

    public static void info(Marker arg0, String arg1, Object arg2, Object arg3) {
        log.info(arg0, arg1, arg2, arg3);
    }

    public static boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    public static boolean isDebugEnabled(Marker arg0) {
        return log.isDebugEnabled(arg0);
    }

    public static boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    public static boolean isErrorEnabled(Marker arg0) {
        return log.isErrorEnabled(arg0);
    }

    public static boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    public static boolean isInfoEnabled(Marker arg0) {
        return log.isInfoEnabled(arg0);
    }

    public static boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    public static boolean isTraceEnabled(Marker arg0) {
        return log.isTraceEnabled(arg0);
    }

    public static boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    public static boolean isWarnEnabled(Marker arg0) {
        return log.isWarnEnabled(arg0);
    }

    public static void trace(String arg0) {
        log.trace(arg0);
    }

    public static void trace(String arg0, Object arg1) {
        log.trace(arg0, arg1);
    }

    public static void trace(String arg0, Object... arg1) {
        log.trace(arg0, arg1);
    }

    public static void trace(String arg0, Throwable arg1) {
        log.trace(arg0, arg1);
    }

    public static void trace(Marker arg0, String arg1) {
        log.trace(arg0, arg1);
    }

    public static void trace(String arg0, Object arg1, Object arg2) {
        log.trace(arg0, arg1, arg2);
    }

    public static void trace(Marker arg0, String arg1, Object arg2) {
        log.trace(arg0, arg1, arg2);
    }

    public static void trace(Marker arg0, String arg1, Object... arg2) {
        log.trace(arg0, arg1, arg2);
    }

    public static void trace(Marker arg0, String arg1, Throwable arg2) {
        log.trace(arg0, arg1, arg2);
    }

    public static void trace(Marker arg0, String arg1, Object arg2, Object arg3) {
        log.trace(arg0, arg1, arg2, arg3);
    }

    public static void warn(String arg0) {
        log.warn(arg0);
    }

    public static void warn(String arg0, Object arg1) {
        log.warn(arg0, arg1);
    }

    public static void warn(String arg0, Object... arg1) {
        log.warn(arg0, arg1);
    }

    public static void warn(String arg0, Throwable arg1) {
        log.warn(arg0, arg1);
    }

    public static void warn(Marker arg0, String arg1) {
        log.warn(arg0, arg1);
    }

    public static void warn(String arg0, Object arg1, Object arg2) {
        log.warn(arg0, arg1, arg2);
    }

    public static void warn(Marker arg0, String arg1, Object arg2) {
        log.warn(arg0, arg1, arg2);
    }

    public static void warn(Marker arg0, String arg1, Object... arg2) {
        log.warn(arg0, arg1, arg2);
    }

    public static void warn(Marker arg0, String arg1, Throwable arg2) {
        log.warn(arg0, arg1, arg2);
    }

    public static void warn(Marker arg0, String arg1, Object arg2, Object arg3) {
        log.warn(arg0, arg1, arg2, arg3);
    }
}
