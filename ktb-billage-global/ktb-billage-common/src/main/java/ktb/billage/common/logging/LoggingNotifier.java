package ktb.billage.common.logging;

public interface LoggingNotifier {
    LoggingSpan startChildSpan(String operation, String description);

    void finishSpan(LoggingSpan span, boolean success, Throwable throwable);

    interface LoggingSpan {
    }

    LoggingNotifier NO_OP = new LoggingNotifier() {
        @Override
        public LoggingSpan startChildSpan(String operation, String description) {
            return null;
        }

        @Override
        public void finishSpan(LoggingSpan span, boolean success, Throwable throwable) {
        }
    };
}
