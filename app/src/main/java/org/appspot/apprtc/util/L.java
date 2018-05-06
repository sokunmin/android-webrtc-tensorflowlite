package org.appspot.apprtc.util;

import android.content.Context;
import android.util.Log;

import org.appspot.apprtc.BuildConfig;


public class L {
    private static final int LEVEL = Log.VERBOSE;
    private static final String DIVIDER = "--------------------";

    public static String isNull(Object obj) {
        StringBuilder result = new StringBuilder()
                .append((obj == null)?"NULL":obj.getClass().getSimpleName()+" not NULL");
        return result.toString();
    }

    /**
     * Debug Level
     */

    public static void d() {
        if (LEVEL <= Log.DEBUG && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag("", lineNumber(), method());
                Log.d(logTag, DIVIDER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static void d(String tag) {
        if (LEVEL <= Log.DEBUG && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag(tag, lineNumber(), method());
                Log.d(logTag, DIVIDER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void d(Object obj) {
        if (LEVEL <= Log.DEBUG && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag(obj.getClass().getSimpleName(), lineNumber(), method());
                Log.d(logTag, DIVIDER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void d(Context c) {
        if (LEVEL <= Log.DEBUG && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag(c.getClass().getSimpleName(), lineNumber(), method());
                Log.d(logTag, DIVIDER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void d(Class<?> c) {
        if (LEVEL <= Log.DEBUG && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag(c.getSimpleName(), lineNumber(), method());
                Log.d(logTag, DIVIDER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void d(Class<?> c, String format, Object...args) {
        if (LEVEL <= Log.DEBUG && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag(c.getSimpleName(), lineNumber(), method());
                Log.d(logTag, String.format(format, args));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void d(Context c, String format, Object...args) {
        if (LEVEL <= Log.DEBUG && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag(c.getClass().getSimpleName(), lineNumber(), method());
                Log.d(logTag, String.format(format, args));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void d(String format, Object...args) {
        if (LEVEL <= Log.DEBUG && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag("", lineNumber(), method());
                Log.d(logTag, String.format(format, args));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static void d(int depth, String format, Object...args) {
        if (LEVEL <= Log.DEBUG && BuildConfig.DEBUG) {
            try {
                String logTag = concatTagWithDepth("", lineNumber(), lineNumber(depth), method(depth));
                Log.d(logTag, String.format(format, args));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Info Level
     */

    public static void i() {
        if (LEVEL <= Log.INFO && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag("", lineNumber(), method());
                Log.i(logTag, DIVIDER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void i(String tag) {
        if (LEVEL <= Log.INFO && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag(tag, lineNumber(), method());
                Log.i(logTag, DIVIDER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void i(Object obj) {
        if (LEVEL <= Log.INFO && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag(obj.getClass().getSimpleName(), lineNumber(), method());
                Log.i(logTag, DIVIDER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void i(Context c) {
        if (LEVEL <= Log.INFO && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag(c.getClass().getSimpleName(), lineNumber(), method());
                Log.i(logTag, DIVIDER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void i(Class<?> c) {
        if (LEVEL <= Log.INFO && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag(c.getSimpleName(), lineNumber(), method());
                Log.i(logTag, DIVIDER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void i(Class<?> c, String format, Object...args) {
        if (LEVEL <= Log.INFO && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag(c.getSimpleName(), lineNumber(), method());
                Log.i(logTag, String.format(format, args));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void i(Context c, String format, Object...args) {
        if (LEVEL <= Log.INFO && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag(c.getClass().getSimpleName(), lineNumber(), method());
                Log.i(logTag, String.format(format, args));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static void i(String format, Object...args) {
        if (LEVEL <= Log.INFO && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag("", lineNumber(), method());
                Log.i(logTag, String.format(format, args));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void i(int depth, String format, Object...args) {
        if (LEVEL <= Log.INFO && BuildConfig.DEBUG) {
            try {
                String logTag = concatTagWithDepth("", lineNumber(), lineNumber(depth), method(depth));
                Log.i(logTag, String.format(format, args));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Warning Level
     */

    public static void w() {
        if (LEVEL <= Log.WARN && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag("", lineNumber(), method());
                Log.w(logTag, DIVIDER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void w(String tag) {
        if (LEVEL <= Log.WARN && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag(tag, lineNumber(), method());
                Log.w(logTag, DIVIDER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void w(Object obj) {
        if (LEVEL <= Log.WARN && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag(obj.getClass().getSimpleName(), lineNumber(), method());
                Log.w(logTag, DIVIDER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void w(Context c) {
        if (LEVEL <= Log.WARN && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag(c.getClass().getSimpleName(), lineNumber(), method());
                Log.w(logTag, DIVIDER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void w(Class<?> c) {
        if (LEVEL <= Log.WARN && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag(c.getSimpleName(), lineNumber(), method());
                Log.w(logTag, DIVIDER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void w(Class<?> c, String format, Object...args) {
        if (LEVEL <= Log.WARN && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag(c.getSimpleName(), lineNumber(), method());
                Log.w(logTag, String.format(format, args));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void w(Context c, String format, Object...args) {
        if (LEVEL <= Log.WARN && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag(c.getClass().getSimpleName(), lineNumber(), method());
                Log.w(logTag, String.format(format, args));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void w(String format, Object...args) {
        if (LEVEL <= Log.WARN && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag("", lineNumber(), method());
                Log.w(logTag, String.format(format, args));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void w(int depth, String format, Object...args) {
        if (LEVEL <= Log.WARN && BuildConfig.DEBUG) {
            try {
                String logTag = concatTagWithDepth("", lineNumber(), lineNumber(depth), method(depth));
                Log.w(logTag, String.format(format, args));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Error Level
     */

    public static void e() {
        if (LEVEL <= Log.ERROR && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag("", lineNumber(), method());
                Log.e(logTag, DIVIDER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void e(String tag) {
        if (LEVEL <= Log.ERROR && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag(tag, lineNumber(), method());
                Log.e(logTag, DIVIDER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void e(Object object) {
        if (LEVEL <= Log.ERROR && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag(object.getClass().getSimpleName(), lineNumber(), method());
                Log.e(logTag, DIVIDER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static void e(Context c) {
        if (LEVEL <= Log.ERROR && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag(c.getClass().getSimpleName(), lineNumber(), method());
                Log.e(logTag, DIVIDER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void e(Class<?> c, String format, Object...args) {
        if (LEVEL <= Log.ERROR && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag(c.getSimpleName(), lineNumber(), method());
                Log.e(logTag, String.format(format, args));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void e(Context c, String format, Object...args) {
        if (LEVEL <= Log.ERROR && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag(c.getClass().getSimpleName(), lineNumber(), method());
                Log.e(logTag, String.format(format, args));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void e(String format, Object...args) {
        if (LEVEL <= Log.ERROR && BuildConfig.DEBUG) {
            try {
                String logTag = concatTag("", lineNumber(), method());
                Log.e(logTag, String.format(format, args));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void e(int depth, String format, Object...args) {
        if (LEVEL <= Log.ERROR && BuildConfig.DEBUG) {
            try {
                String logTag = concatTagWithDepth("", lineNumber(), lineNumber(depth), method(depth));
                Log.e(logTag, String.format(format, args));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Private static methods
     0 is getStackTrace(),
     1 is getMethodName(int depth) and
     2 is invoking method.
     */
    private static StringBuilder method(int ...depth) {
        int i = (depth.length > 0) ? depth[0] : 0;
        final StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        return new StringBuilder().append(stack[4+i].getMethodName()).append("()");
    }

    private static StringBuilder lineNumber(int ...depth) {
        int i = (depth.length > 0) ? depth[0] : 0;
        final StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        return new StringBuilder().append(stack[4+i].getLineNumber()).append("");
    }

    private static String concatTag(String tag, StringBuilder lineNumber, StringBuilder method) {
        StringBuilder log = new StringBuilder();
        if (tag == null || tag.equals("")) {
            log.append("[(").append(lineNumber).append(")")
                    .append(method)
                    .append("] => \t");
        } else {
            log.append("[(").append(lineNumber).append(")")
                    .append(tag)
                    .append(" / ")
                    .append(method)
                    .append("] => \t");
        }

        return log.toString();
    }

    private static String concatTagWithDepth(String tag, StringBuilder lineNumber, StringBuilder depth, StringBuilder method) {
        String logTag = new StringBuilder()
                .append("[(").append(lineNumber).append(")")
                .append(" / (").append(depth).append(")")
                .append(method)
                .append("] => \t").toString();
        return logTag;
    }
}