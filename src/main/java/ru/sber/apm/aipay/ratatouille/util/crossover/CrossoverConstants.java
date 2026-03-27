package ru.sber.apm.aipay.ratatouille.util.crossover;

import lombok.experimental.UtilityClass;

/**
 * Константы для работы с Crossover API
 */
@UtilityClass
public class CrossoverConstants {

    // API Version
    public static final String API_VERSION = "v1";
    public static final String API_PREFIX = "/crossover/" + API_VERSION;

    // Endpoints
    public static final String ENDPOINT_MERCHANT = API_PREFIX + "/merchant";
    public static final String ENDPOINT_PRODUCT_LIST = API_PREFIX + "/product/list";
    public static final String ENDPOINT_PRODUCT_DETAIL = API_PREFIX + "/product/{productId}";
    public static final String ENDPOINT_ORDER_CREATE = API_PREFIX + "/order";
    public static final String ENDPOINT_ORDER_LIST = API_PREFIX + "/order/list";
    public static final String ENDPOINT_ORDER_DETAIL = API_PREFIX + "/order/{orderId}";

    public static final String ENDPOINT_SESSION_ID_WEB = "/sdk-gateway/v1/sessionIdWeb";

    // Header names
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_TIMESTAMP = "timestamp";
    public static final String HEADER_RQ_UID = "rqUID";
    public static final String HEADER_LOCAL_SESSION_ID = "localSessionId";

    public static final String HEADER_X_B3_SPAN_ID = "x-b3-spanid";
    public static final String HEADER_X_SYSTEM_ID = "X-System-Id";
    public static final String HEADER_X_B3_TRACE_ID = "x-b3-traceid";
    public static final String HEADER_APP_NAME = "appName";
    public static final String HEADER_USER_TM = "UserTm";

    // Query param names
    public static final String PARAM_EXT_BRANCH_ID = "extBranchId";
    public static final String PARAM_POINT_ID = "pointId";
    public static final String PARAM_SUB_ID = "subId";
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_LIMIT = "limit";
    public static final String PARAM_CATEGORY_ID = "categoryId";

    // Default pagination
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_LIMIT = 20;
    public static final int MAX_LIMIT = 100;

    // Error codes
    public static final int ERROR_CODE_NOT_FOUND = 1;
    public static final int ERROR_CODE_BAD_REQUEST = 2;
    public static final int ERROR_CODE_INTERNAL_ERROR = 3;
    public static final int ERROR_CODE_UNAUTHORIZED = 4;
    public static final int ERROR_CODE_FORBIDDEN = 5;
    public static final int ERROR_CODE_CONFLICT = 6;
    public static final int ERROR_CODE_RATE_LIMIT = 7;
    public static final int ERROR_CODE_SERVICE_UNAVAILABLE = 8;
    public static final int ERROR_CODE_GATEWAY_ERROR = 9;
    public static final int ERROR_CODE_CONNECTION_ERROR = 10;
    public static final int ERROR_CODE_TIMEOUT = 11;
}