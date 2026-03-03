package ktb.billage.common.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ApplicationLoggingAspect {
    private final LoggingNotifier loggingNotifier;

    public ApplicationLoggingAspect(ObjectProvider<LoggingNotifier> loggingNotifierProvider) {
        this.loggingNotifier = loggingNotifierProvider.getIfAvailable(() -> LoggingNotifier.NO_OP);
    }

    @Around("@annotation(org.springframework.transaction.annotation.Transactional) || @within(org.springframework.transaction.annotation.Transactional)")
    public Object logTransactional(ProceedingJoinPoint joinPoint) throws Throwable {
        LoggingNotifier.LoggingSpan childSpan = startChildSpan("app.tx", joinPoint);
        long start = System.nanoTime();
        String signature = shortSignature(joinPoint);
        log.info("[TX][START] {}", signature);
        try {
            Object result = joinPoint.proceed();
            long elapsedMs = elapsedMs(start);
            log.info("[TX][END] {} ({} ms)", signature, elapsedMs);
            finishSpan(childSpan, true, null);
            return result;
        } catch (Throwable t) {
            long elapsedMs = elapsedMs(start);
            log.error("[TX][ERROR] {} ({} ms): {}", signature, elapsedMs, t.toString());
            finishSpan(childSpan, false, t);
            throw t;
        }
    }

    @Around(
        "("
            + "execution(* ktb.billage.application..*(..))"
            + " || execution(* ktb.billage.domain..service..*(..))"
            + " || execution(* ktb.billage.websocket.application..*(..))"
        + ") && !within(ktb.billage.common.logging..*)"
    )
    public Object logMethodCall(ProceedingJoinPoint joinPoint) throws Throwable {
        LoggingNotifier.LoggingSpan childSpan = startChildSpan("app.method", joinPoint);
        long start = System.nanoTime();
        String signature = shortSignature(joinPoint);
        log.debug("[METHOD][START] {}", signature);
        try {
            Object result = joinPoint.proceed();
            long elapsedMs = elapsedMs(start);
            log.debug("[METHOD][END] {} ({} ms)", signature, elapsedMs);
            finishSpan(childSpan, true, null);
            return result;
        } catch (Throwable t) {
            long elapsedMs = elapsedMs(start);
            log.error("[METHOD][ERROR] {} ({} ms): {}", signature, elapsedMs, t.toString());
            finishSpan(childSpan, false, t);
            throw t;
        }
    }

    @Around("execution(* ktb.billage..*Repository.*(..))")
    public Object logRepositoryCall(ProceedingJoinPoint joinPoint) throws Throwable {
        LoggingNotifier.LoggingSpan childSpan = startChildSpan("db.call", joinPoint);
        long start = System.nanoTime();
        String signature = shortSignature(joinPoint);
        log.info("[DB][START] {}", signature);
        try {
            Object result = joinPoint.proceed();
            long elapsedMs = elapsedMs(start);
            log.info("[DB][END] {} ({} ms)", signature, elapsedMs);
            finishSpan(childSpan, true, null);
            return result;
        } catch (Throwable t) {
            long elapsedMs = elapsedMs(start);
            log.error("[DB][ERROR] {} ({} ms): {}", signature, elapsedMs, t.toString());
            finishSpan(childSpan, false, t);
            throw t;
        }
    }

    @Around("execution(* ktb.billage.infra.ai..*(..)) || execution(* ktb.billage.infra.image..*(..))")
    public Object logExternalCall(ProceedingJoinPoint joinPoint) throws Throwable {
        LoggingNotifier.LoggingSpan childSpan = startChildSpan("ext.call", joinPoint);
        long start = System.nanoTime();
        String signature = shortSignature(joinPoint);
        log.info("[EXT][START] {}", signature);
        try {
            Object result = joinPoint.proceed();
            long elapsedMs = elapsedMs(start);
            log.info("[EXT][END] {} ({} ms)", signature, elapsedMs);
            finishSpan(childSpan, true, null);
            return result;
        } catch (Throwable t) {
            long elapsedMs = elapsedMs(start);
            log.error("[EXT][ERROR] {} ({} ms): {}", signature, elapsedMs, t.toString());
            finishSpan(childSpan, false, t);
            throw t;
        }
    }

    private LoggingNotifier.LoggingSpan startChildSpan(String operation, ProceedingJoinPoint joinPoint) {
        return loggingNotifier.startChildSpan(operation, shortSignature(joinPoint));
    }

    private void finishSpan(LoggingNotifier.LoggingSpan childSpan, boolean success, Throwable throwable) {
        loggingNotifier.finishSpan(childSpan, success, throwable);
    }

    private String shortSignature(ProceedingJoinPoint joinPoint) {
        return joinPoint.getSignature().toShortString();
    }

    private long elapsedMs(long startNano) {
        return (System.nanoTime() - startNano) / 1_000_000;
    }
}
