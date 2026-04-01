package ru.sber.apm.aipay.ratatouille.util.smartring;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SmartRingConstants {

    // API Version
    public static final String API_PREFIX = "/external/sync";

    // Error codes
    public static final int ERROR_CODE_NOT_FOUND = 1;
    public static final int ERROR_CODE_BAD_REQUEST = 2;
    public static final int ERROR_CODE_INTERNAL_ERROR = 3;
    public static final int ERROR_CODE_UNAUTHORIZED = 4;
    public static final int ERROR_CODE_FORBIDDEN = 5;
    public static final int ERROR_CODE_CONFLICT = 6;
    public static final int ERROR_CODE_RATE_LIMIT = 7;
    public static final int ERROR_CODE_SERVICE_UNAVAILABLE = 8;
    public static final int ERROR_CODE_TIMEOUT = 9;
    public static final int ERROR_CODE_CONNECTION_ERROR = 10;

    // Header names
    public static final String HEADER_AUTHORIZATION = "Authorization";

    // Query param names
    public static final String PARAM_FROM = "from";
    public static final String PARAM_TO = "to";
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_PAGE_SIZE = "pageSize";

    // Sync types (должны совпадать с enum в OpenAPI spec)
    public static final String SYNC_TYPE_HEART_RATE = "heartrate";
    public static final String SYNC_TYPE_SPO2 = "spo2";
    public static final String SYNC_TYPE_STEP = "step";
    public static final String SYNC_TYPE_SLEEP = "sleep";
    public static final String SYNC_TYPE_HRV = "hrv";
    public static final String SYNC_TYPE_STRESS = "stress";
    public static final String SYNC_TYPE_TEMPERATURE = "temperature";

    // Default pagination
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_PAGE_SIZE = 100;
    public static final int MAX_PAGE_SIZE = 1000;
}