//package ru.sber.apm.aipay.ratatouille.config;
//
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Pointcut;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.ai.mcp.annotation.McpTool;
//import org.springframework.core.Ordered;
//import org.springframework.core.annotation.Order;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//import ru.sber.apm.aipay.ratatouille.exception.crossover.CrossoverApiException;
//import tools.jackson.core.JacksonException;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Aspect
//@Component
//@Order(Ordered.HIGHEST_PRECEDENCE)
//public class McpToolExceptionAspect {
//
//    private static final Logger logger = LoggerFactory.getLogger(McpToolExceptionAspect.class);
//
//    /**
//     * Pointcut для всех методов с аннотацией @McpTool
//     */
//    @Pointcut("@annotation(org.springframework.ai.mcp.annotation.McpTool)")
//    public void mcpToolMethod() {}
//
//    /**
//     * Pointcut для всех методов в пакетах service.crossover и service.p2pcrypto
//     */
//    @Pointcut("within(ru.sber.apm.aipay.ratatouille.service.crossover..*) || " +
//              "within(ru.sber.apm.aipay.ratatouille.service.p2pcrypto..*)")
//    public void serviceMethods() {}
//
//    /**
//     * Обработка исключений вокруг MCP Tool методов
//     */
//    @Around("mcpToolMethod() && serviceMethods()")
//    public Object handleMcpToolExceptions(ProceedingJoinPoint joinPoint) throws Throwable {
//        String methodName = joinPoint.getSignature().getName();
//        String className = joinPoint.getTarget().getClass().getSimpleName();
//
//        logger.info("Вызов MCP Tool: {}.{}", className, methodName);
//        logger.debug("Аргументы: {}", maskSensitiveArgs(joinPoint.getArgs()));
//
//        try {
//            Object result = joinPoint.proceed();
//            logger.info("MCP Tool {}.{} выполнен успешно", className, methodName);
//            return result;
//
//        } catch (CrossoverApiException e) {
//            logger.error("Crossover API ошибка в {}.{}: errorCode={}, message={}",
//                    className, methodName, e.getErrorCode(), e.getMessage());
//            throw e; // Пробрасываем дальше для обработки MCP
//
//        } catch (LinkApiException e) {
//            logger.error("LINK API ошибка в {}.{}: errorCode={}, message={}",
//                    className, methodName, e.getErrorCode(), e.getMessage());
//            throw e; // Пробрасываем дальше для обработки MCP
//
//        } catch (IllegalArgumentException e) {
//            logger.warn("Ошибка валидации в {}.{}: {}", className, methodName, e.getMessage());
//            throw CrossoverApiException.badRequest("Ошибка валидации: " + e.getMessage());
//
//        } catch (java.net.http.HttpConnectTimeoutException e) {
//            logger.error("Таймаут соединения в {}.{}: {}", className, methodName, e.getMessage());
//            throw CrossoverApiException.internalError("Таймаут соединения с API", e);
//
//        } catch (java.net.http.HttpTimeoutException e) {
//            logger.error("Таймаут запроса в {}.{}: {}", className, methodName, e.getMessage());
//            throw CrossoverApiException.internalError("Таймаут запроса к API", e);
//
//        } catch (javax.net.ssl.SSLException e) {
//            logger.error("SSL ошибка в {}.{}: {}", className, methodName, e.getMessage());
//            throw CrossoverApiException.sslError(e.getMessage(), e);
//
//        } catch (JacksonException e) {
//            logger.error("Ошибка JSON в {}.{}: {}", className, methodName, e.getMessage());
//            throw CrossoverApiException.badRequest("Ошибка обработки JSON: " + e.getMessage());
//
//        } catch (Exception e) {
//            logger.error("Неожиданная ошибка в {}.{}: {}", className, methodName, e.getMessage(), e);
//            throw CrossoverApiException.internalError("Внутренняя ошибка сервера", e);
//        }
//    }
//
//    /**
//     * Маскировка чувствительных аргументов для логирования
//     */
//    private Map<String, Object> maskSensitiveArgs(Object[] args) {
//        var masked = new HashMap<String, Object>();
//        for (int i = 0; i < args.length; i++) {
//            String argName = "arg" + i;
//            Object arg = args[i];
//
//            if (arg instanceof String) {
//                String str = (String) arg;
//                if (str != null && (str.toLowerCase().contains("key") ||
//                    str.toLowerCase().contains("token") ||
//                    str.toLowerCase().contains("secret") ||
//                    str.toLowerCase().contains("password"))) {
//                    masked.put(argName, "***MASKED***");
//                } else {
//                    masked.put(argName, truncate(str, 50));
//                }
//            } else {
//                masked.put(argName, arg != null ? arg.getClass().getSimpleName() : "null");
//            }
//        }
//        return masked;
//    }
//
//    private String truncate(String str, int maxLen) {
//        if (str == null) return "null";
//        return str.length() > maxLen ? str.substring(0, maxLen) + "..." : str;
//    }
//}