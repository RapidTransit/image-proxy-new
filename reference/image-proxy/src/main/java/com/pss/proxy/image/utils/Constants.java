package com.pss.proxy.image.utils;

public class Constants {

    public static final int VALID_IMAGE_ORDER = 0;
    public static final int CACHING_NOT_FOUND_ORDER = 100;
    public static final int REWRITE_URL_ORDER = 200;
    public static final int QUERY_ROUTE_ORDER = 300;
    public static final int NO_IMAGE_ROUTE_ORDER = 300;
    public static final int NOT_FOUND_ORDER = 400;
    public static final int ERROR_ORDER = 500;

    public static final String USER_IP_HEADER = "X-User-IP";

    public static final String QUERY = "__url-query";
    public static final String INVOKED_HANDLER = "__invoked_handler";
    public static final String REQUEST_REDIRECTED = "__REQUEST_REDIRECTED";

    public static final String DOMAIN_SOCKET = "domain-socket";
}
