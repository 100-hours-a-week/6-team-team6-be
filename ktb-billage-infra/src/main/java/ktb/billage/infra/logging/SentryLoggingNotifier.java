package ktb.billage.infra.logging;

import io.sentry.ISpan;
import io.sentry.Sentry;
import io.sentry.SpanStatus;
import ktb.billage.common.logging.LoggingNotifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SentryLoggingNotifier implements LoggingNotifier {

    @Override
    public LoggingSpan startChildSpan(String operation, String description) {
        ISpan parentSpan = resolveParentSpan();
        if (parentSpan == null) {
            log.debug("[SENTRY][SKIP] no active parent span for {}", description);
            return null;
        }
        return new SentrySpan(parentSpan.startChild(operation, description));
    }

    @Override
    public void finishSpan(LoggingSpan span, boolean success, Throwable throwable) {
        if (!(span instanceof SentrySpan sentrySpan)) {
            return;
        }
        ISpan delegate = sentrySpan.delegate();
        delegate.setStatus(success ? SpanStatus.OK : SpanStatus.INTERNAL_ERROR);
        if (throwable != null) {
            delegate.setThrowable(throwable);
        }
        delegate.finish();
    }

    private ISpan resolveParentSpan() {
        ISpan parentSpan = Sentry.getSpan();
        if (parentSpan == null) {
            parentSpan = Sentry.getCurrentScopes().getSpan();
        }
        if (parentSpan == null) {
            parentSpan = Sentry.getCurrentScopes().getTransaction();
        }
        return parentSpan;
    }

    private record SentrySpan(ISpan delegate) implements LoggingSpan {
    }
}
