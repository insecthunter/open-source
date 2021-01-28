package com.simba.elasticjob.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @Description
 * @Author yuanjx3
 * @Date 2021/1/12 14:56
 * @Version V1.0
 **/
public class StringUtils {
    public static boolean isNotNullOrEmpty(String string) {
        return !isNullOrEmpty(string);
    }
    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }
    public static String lenientFormat(String template, Object... args) {
        template = String.valueOf(template);
        if (args == null) {
            args = new Object[]{"(Object[])null"};
        } else {
            for(int i = 0; i < args.length; ++i) {
                args[i] = lenientToString(args[i]);
            }
        }

        StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);
        int templateStart = 0;

        int i;
        int placeholderStart;
        for(i = 0; i < args.length; templateStart = placeholderStart + 2) {
            placeholderStart = template.indexOf("%s", templateStart);
            if (placeholderStart == -1) {
                break;
            }

            builder.append(template, templateStart, placeholderStart);
            builder.append(args[i++]);
        }

        builder.append(template, templateStart, template.length());
        if (i < args.length) {
            builder.append(" [");
            builder.append(args[i++]);

            while(i < args.length) {
                builder.append(", ");
                builder.append(args[i++]);
            }

            builder.append(']');
        }

        return builder.toString();
    }

    private static String lenientToString(Object o) {
        if (o == null) {
            return "null";
        } else {
            try {
                return o.toString();
            } catch (Exception var3) {
                String objectToString = o.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(o));
                Logger.getLogger("com.google.common.base.Strings").log(Level.WARNING, "Exception during lenientFormat for " + objectToString, var3);
                return "<" + objectToString + " threw " + var3.getClass().getName() + ">";
            }
        }
    }

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis());
    }
}
