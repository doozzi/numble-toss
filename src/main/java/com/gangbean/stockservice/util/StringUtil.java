package com.gangbean.stockservice.util;

public class StringUtil {

    public static String paddLeftWith(String origin, String delimiter, int length) {
        StringBuilder sb = new StringBuilder();
        int left = length - origin.length();

        return sb.append(String.valueOf(delimiter).repeat(Math.max(0, left)))
            .append(origin)
            .substring(0, length);
    }

    public static String paddLeftWith(String origin, int length) {
        return paddLeftWith(origin, " ", length);
    }
}
