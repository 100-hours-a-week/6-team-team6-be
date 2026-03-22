package ktb.billage.infra.logging;

import io.sentry.SentryOptions;
import io.sentry.protocol.Request;
import io.sentry.protocol.SentryTransaction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Locale;

@Configuration
public class SentryConfig {
    private static final String TRACE_MODE_HEADER = "X-Sentry-Trace-Mode";
    private static final List<String> IGNORED_PATHS = List.of("/actuator/health", "/actuator/prometheus");
    private static final List<String> DISABLED_VALUES = List.of("off", "false", "disable", "disabled", "load-test");
    private static final List<String> FORCED_VALUES = List.of("force", "on", "true", "sample");

    @Bean
    public SentryOptions.TracesSamplerCallback sentryTracesSampler() {
        return samplingContext -> {
            Request request = samplingContext.getCustomSamplingContext().get("request") instanceof Request req ? req : null;
            if (isIgnoredRequest(request)) {
                return 0.0;
            }
            if (request == null || request.getHeaders() == null) {
                return null;
            }

            String traceMode = request.getHeaders().entrySet().stream()
                .filter(entry -> TRACE_MODE_HEADER.equalsIgnoreCase(entry.getKey()))
                .map(entry -> normalize(entry.getValue()))
                .findFirst()
                .orElse(null);

            if (traceMode == null) {
                return null;
            }
            if (DISABLED_VALUES.contains(traceMode)) {
                return 0.0;
            }
            if (FORCED_VALUES.contains(traceMode)) {
                return 1.0;
            }
            return null;
        };
    }

    @Bean
    public SentryOptions.BeforeSendTransactionCallback sentryBeforeSendTransaction() {
        return (transaction, hint) -> isIgnoredTransaction(transaction) ? null : transaction;
    }

    private boolean isIgnoredRequest(Request request) {
        if (request == null || request.getUrl() == null) {
            return false;
        }
        return IGNORED_PATHS.stream().anyMatch(request.getUrl()::contains);
    }

    private boolean isIgnoredTransaction(SentryTransaction transaction) {
        Request request = transaction.getRequest();
        if (isIgnoredRequest(request)) {
            return true;
        }

        String transactionName = transaction.getTransaction();
        if (transactionName == null) {
            return false;
        }
        return IGNORED_PATHS.stream().anyMatch(transactionName::contains);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }
}
