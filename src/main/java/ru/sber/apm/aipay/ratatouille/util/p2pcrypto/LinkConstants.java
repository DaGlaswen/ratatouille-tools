package ru.sber.apm.aipay.ratatouille.util.p2pcrypto;

import lombok.experimental.UtilityClass;

@UtilityClass
public class LinkConstants {

    // API Version
    public static final String API_VERSION = "v0";
    public static final String API_PREFIX = "/" + API_VERSION;

    // Endpoints
    public static final String ENDPOINT_DEPOSITE_ADDRESS = API_PREFIX + "/wallet/{walletId}/deposite-address";
    public static final String ENDPOINT_WITHDRAW = API_PREFIX + "/wallet/{walletId}/withdraw";

    // Header names
    public static final String HEADER_RQUID = "x-kw-rquid"; // TODO точно такой naming? Или просто RqUID?
    public static final String AGENT_USER_ID = "AgentUserID";

    // Coin types
    public static final String COIN_ETH = "ETH";
    public static final String COIN_BTC = "BTC";
    public static final String COIN_USDT = "USDT";
    public static final String COIN_TON = "TON";

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
    public static final int ERROR_CODE_WALLET_ERROR = 12;
    public static final int ERROR_CODE_INSUFFICIENT_FUNDS = 13;
}